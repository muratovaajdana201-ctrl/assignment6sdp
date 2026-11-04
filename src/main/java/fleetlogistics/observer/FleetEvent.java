
package fleetlogistics.observer;

import java.util.Map;

public record FleetEvent(
        EventType type,
        String vehicleId,
        Map<String, String> data
) {
    public FleetEvent {
        if (type == null) {
            throw new IllegalArgumentException("Event type is required");
        }

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID is required");
        }

        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
