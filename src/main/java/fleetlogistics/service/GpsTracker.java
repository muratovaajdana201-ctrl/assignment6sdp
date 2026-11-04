
package fleetlogistics.service;

import fleetlogistics.observer.EventType;
import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Subject;

import java.util.Map;

public class GpsTracker {

    private final Subject<FleetEvent> subject;

    public GpsTracker(Subject<FleetEvent> subject) {
        this.subject = subject;
    }

    public void updateLocation(
            String vehicleId,
            double latitude,
            double longitude) {

        FleetEvent event = new FleetEvent(
                EventType.GPS_UPDATE,
                vehicleId,
                Map.of(
                        "latitude", String.valueOf(latitude),
                        "longitude", String.valueOf(longitude)
                )
        );

        subject.publish(event);
    }
}
