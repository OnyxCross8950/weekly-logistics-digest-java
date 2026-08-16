package education.logistics.digest.service;

import education.logistics.digest.domain.ShipmentEvent;
import education.logistics.digest.domain.WeeklyDigest;
import java.util.List;

public final class LogisticsDigestService {
    public WeeklyDigest prepare(List<ShipmentEvent> events) {
        List<ShipmentEvent> completed = events.stream()
                .filter(event -> event.kind() == ShipmentEvent.EventKind.DELIVERED)
                .filter(event -> event.proof().isPresent())
                .toList();
        List<ShipmentEvent> needsAttention = events.stream()
                .filter(event -> event.kind() == ShipmentEvent.EventKind.EXCEPTION)
                .filter(event -> event.exception().isPresent())
                .toList();
        return new WeeklyDigest(completed, needsAttention);
    }
}
