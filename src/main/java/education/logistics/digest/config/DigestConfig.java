package education.logistics.digest.config;

import java.net.URI;

public record DigestConfig(String apiKey, URI taskUrl, String cronExpression) {
    public static DigestConfig fromEnvironment() {
        String key = required("INFRAI_API_KEY");
        URI task = URI.create(required("DIGEST_TASK_URL"));
        String cron = System.getenv().getOrDefault("DIGEST_CRON", "0 8 * * 1");
        return new DigestConfig(key, task, cron);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " before registering the digest schedule");
        }
        return value;
    }
}
