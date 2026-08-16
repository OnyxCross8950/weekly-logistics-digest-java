package education.logistics.digest;

import education.logistics.digest.config.DigestConfig;
import education.logistics.digest.domain.ShipmentEvent;
import education.logistics.digest.domain.ShipmentEvent.DeliveryException;
import education.logistics.digest.domain.ShipmentEvent.ProofOfDelivery;
import education.logistics.digest.domain.WeeklyDigest;
import education.logistics.digest.infrai.InfraiCronClient;
import education.logistics.digest.service.LogisticsDigestService;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class LogisticsDigestApplication {
    public static void main(String[] args) throws Exception {
        LogisticsDigestService service = new LogisticsDigestService();
        WeeklyDigest preview = service.prepare(sampleLesson());
        System.out.printf("Digest preview: %d completed, %d need attention%n",
                preview.completed().size(), preview.needsAttention().size());

        if (args.length > 0 && args[0].equals("register")) {
            DigestConfig config = DigestConfig.fromEnvironment();
            InfraiCronClient client = new InfraiCronClient(HttpClient.newHttpClient(), config.apiKey());
            String jobId = client.create(config.cronExpression(), config.taskUrl());
            System.out.println("Weekly digest scheduled: " + jobId);
        }
    }

    private static List<ShipmentEvent> sampleLesson() {
        Instant monday = Instant.parse("2026-08-10T08:00:00Z");
        return List.of(
                new ShipmentEvent("SHP-101", ShipmentEvent.EventKind.DELIVERED, monday,
                        Optional.of(new ProofOfDelivery("SHP-101-signed.pdf", "A. Chen")), Optional.empty()),
                new ShipmentEvent("SHP-102", ShipmentEvent.EventKind.EXCEPTION, monday,
                        Optional.empty(), Optional.of(new DeliveryException("address review", "dispatch desk"))),
                new ShipmentEvent("SHP-103", ShipmentEvent.EventKind.IN_TRANSIT, monday,
                        Optional.empty(), Optional.empty()));
    }
}
