
package fleetlogistics.service;

import fleetlogistics.observer.EventType;
import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Subject;

import java.util.Map;

public class VehicleTelemetrySource {

    private final Subject<FleetEvent> subject;

    public VehicleTelemetrySource(Subject<FleetEvent> subject) {
        this.subject = subject;
    }

    public void updateEta(String vehicleId, int minutes) {

        FleetEvent event = new FleetEvent(
                EventType.ETA_UPDATE,
                vehicleId,
                Map.of("minutes", String.valueOf(minutes))
        );

        subject.publish(event);
    }

    public void reportGeofence(
            String vehicleId,
            String zone,
            boolean entered) {

        FleetEvent event = new FleetEvent(
                EventType.GEOFENCE_ALERT,
                vehicleId,
                Map.of(
                        "zone", zone,
                        "action", entered ? "entered" : "exited"
                )
        );

        subject.publish(event);
    }

    public void reportFuelLevel(
            String vehicleId,
            double percentage) {

        if (percentage < 20) {
            FleetEvent event = new FleetEvent(
                    EventType.FUEL_LOW,
                    vehicleId,
                    Map.of(
                            "fuelPercentage",
                            String.valueOf(percentage)
                    )
            );

            subject.publish(event);
        }
    }
}
