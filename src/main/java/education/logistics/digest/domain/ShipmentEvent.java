package education.logistics.digest.domain;

import java.time.Instant;
import java.util.Optional;

public record ShipmentEvent(
        String shipmentId,
        EventKind kind,
        Instant occurredAt,
        Optional<ProofOfDelivery> proof,
        Optional<DeliveryException> exception) {

    public enum EventKind { IN_TRANSIT, DELIVERED, EXCEPTION }

    public record ProofOfDelivery(String documentName, String signedBy) {}

    public record DeliveryException(String category, String actionOwner) {}
}
