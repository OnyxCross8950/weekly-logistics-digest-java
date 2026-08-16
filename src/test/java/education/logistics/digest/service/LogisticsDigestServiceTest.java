package education.logistics.digest.service;

import education.logistics.digest.domain.ShipmentEvent;
import education.logistics.digest.domain.ShipmentEvent.DeliveryException;
import education.logistics.digest.domain.ShipmentEvent.ProofOfDelivery;
import education.logistics.digest.domain.WeeklyDigest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class LogisticsDigestServiceTest {
    public static void main(String[] args) {
        Instant now = Instant.parse("2026-08-10T08:00:00Z");
        List<ShipmentEvent> events = List.of(
                new ShipmentEvent("done", ShipmentEvent.EventKind.DELIVERED, now,
                        Optional.of(new ProofOfDelivery("signed.pdf", "Learner One")), Optional.empty()),
                new ShipmentEvent("missing-proof", ShipmentEvent.EventKind.DELIVERED, now,
                        Optional.empty(), Optional.empty()),
                new ShipmentEvent("late", ShipmentEvent.EventKind.EXCEPTION, now,
                        Optional.empty(), Optional.of(new DeliveryException("late handoff", "carrier coach"))),
                new ShipmentEvent("moving", ShipmentEvent.EventKind.IN_TRANSIT, now,
                        Optional.empty(), Optional.empty()));

        WeeklyDigest digest = new LogisticsDigestService().prepare(events);
        require(digest.includedCount() == 2, "digest should contain two actionable shipments");
        require(digest.completed().get(0).shipmentId().equals("done"), "signed delivery should be completed");
        require(digest.needsAttention().get(0).shipmentId().equals("late"), "exception should need attention");
        System.out.println("PASS: signed delivery and exception selected; incomplete records excluded");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
