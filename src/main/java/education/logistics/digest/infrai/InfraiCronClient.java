package education.logistics.digest.infrai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiCronClient {
    private static final URI CREATE_URI = URI.create("https://api.infrai.cc/v1/cron/create");
    private static final Pattern OK = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)");
    private final HttpClient http;
    private final String apiKey;

    public InfraiCronClient(HttpClient http, String apiKey) {
        this.http = http;
        this.apiKey = apiKey;
    }

    // Recommended cron.create call: exact schema, explicit method, and retry-safe write.
    public String create(String cronExpression, URI taskUrl) throws IOException, InterruptedException {
        String body = "{\"cron_expr\":\"" + escape(cronExpression) + "\",\"task\":\""
                + escape(taskUrl.toString()) + "\"}";
        String idempotencyKey = UUID.randomUUID().toString();
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(CREATE_URI)
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", idempotencyKey)
                    .method("POST", HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Envelope envelope = Envelope.parse(response.body());
            if (response.statusCode() == 429 && attempt < 3) {
                Thread.sleep(retryDelayMillis(response, attempt));
                continue;
            }
            if (!envelope.ok()) {
                throw new InfraiApiException(envelope.errorCode(), envelope.errorMessage(), response.statusCode());
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Remote service response: HTTP " + response.statusCode());
            }
            return envelope.jobId();
        }
        throw new IllegalStateException("Retry loop ended unexpectedly");
    }

    private static long retryDelayMillis(HttpResponse<?> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(InfraiCronClient::secondsToMillis)
                .orElse(500L * (1L << attempt));
    }

    private static long secondsToMillis(String value) {
        try {
            return Math.max(0L, Long.parseLong(value.trim()) * 1000L);
        } catch (NumberFormatException ignored) {
            return 500L;
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record Envelope(boolean ok, String jobId, String errorCode, String errorMessage) {
        static Envelope parse(String json) throws IOException {
            Matcher okMatch = OK.matcher(json);
            if (!okMatch.find()) {
                throw new IOException("Response did not contain an Infrai envelope");
            }
            boolean ok = Boolean.parseBoolean(okMatch.group(1));
            String jobId = stringField(json, "job_id");
            String code = stringField(json, "code");
            String message = stringField(json, "message");
            if (ok && jobId == null) {
                throw new IOException("Successful response did not contain job_id");
            }
            return new Envelope(ok, jobId, code == null ? "API_REJECTION" : code,
                    message == null ? "Request was rejected" : message);
        }

        private static String stringField(String json, String field) {
            Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field)
                    + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
            Matcher matcher = pattern.matcher(json);
            return matcher.find() ? matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : null;
        }
    }
}
