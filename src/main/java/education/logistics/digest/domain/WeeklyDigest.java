package education.logistics.digest.domain;

import java.util.List;

public record WeeklyDigest(List<ShipmentEvent> completed, List<ShipmentEvent> needsAttention) {
    public int includedCount() {
        return completed.size() + needsAttention.size();
    }
}
