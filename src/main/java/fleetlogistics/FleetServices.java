package fleetlogistics;

import java.util.Map;

import static fleetlogistics.ObserverSystem.*;

public final class FleetServices {

    private FleetServices() {
    }

    public static class GpsTracker {
        private final EventSubject subject;

        public GpsTracker(EventSubject subject) {
            this.subject = subject;
        }

        public void updateLocation(
                String vehicleId, double latitude, double longitude) {
            subject.publish(createEvent(
                    EventType.GPS_UPDATE,
                    vehicleId,
                    Map.of(
                            "latitude", String.valueOf(latitude),
                            "longitude", String.valueOf(longitude)
                    )
            ));
        }
    }

    public static class EtaService {
        private final EventSubject subject;

        public EtaService(EventSubject subject) {
            this.subject = subject;
        }

        public void updateEta(String vehicleId, int minutes) {
            subject.publish(createEvent(
                    EventType.ETA_UPDATE,
                    vehicleId,
                    Map.of("minutes", String.valueOf(minutes))
            ));
        }
    }

    public static class GeofenceMonitor {
        private final EventSubject subject;

        public GeofenceMonitor(EventSubject subject) {
            this.subject = subject;
        }

        public void reportBoundaryCrossing(
                String vehicleId, String zone, boolean entered) {
            subject.publish(createEvent(
                    EventType.GEOFENCE_ALERT,
                    vehicleId,
                    Map.of(
                            "zone", zone,
                            "action", entered ? "entered" : "exited"
                    )
            ));
        }
    }

    public static class FuelMonitor {
        private final EventSubject subject;

        public FuelMonitor(EventSubject subject) {
            this.subject = subject;
        }

        public void reportFuelLevel(
                String vehicleId, double percentage) {
            if (percentage < 20) {
                subject.publish(createEvent(
                        EventType.FUEL_LOW,
                        vehicleId,
                        Map.of("fuelPercentage", String.valueOf(percentage))
                ));
            }
        }
    }
}