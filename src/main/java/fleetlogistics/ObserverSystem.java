package fleetlogistics;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObserverSystem {

    private ObserverSystem() {
    }

    public enum EventType {
        GPS_UPDATE,
        ETA_UPDATE,
        GEOFENCE_ALERT,
        FUEL_LOW,
        FLEET_SUMMARY
    }

    public record FleetEvent(
            EventType type,
            String vehicleId,
            Instant timestamp,
            Map<String, String> data
    ) {
        public FleetEvent {
            if (type == null || timestamp == null || data == null) {
                throw new IllegalArgumentException(
                        "Event fields cannot be null");
            }
            data = Map.copyOf(data);
        }
    }

    @FunctionalInterface
    public interface Observer<T> {
        void onEvent(T event);
    }

    public static class EventSubject {
        private final Map<EventType,
                CopyOnWriteArrayList<Observer<FleetEvent>>> observers =
                new ConcurrentHashMap<>();

        public void subscribe(
                EventType type,
                Observer<FleetEvent> observer) {

            observers.computeIfAbsent(
                    type, key -> new CopyOnWriteArrayList<>()
            ).addIfAbsent(observer);
        }

        public void unsubscribe(
                EventType type,
                Observer<FleetEvent> observer) {

            List<Observer<FleetEvent>> list = observers.get(type);
            if (list != null) {
                list.remove(observer);
            }
        }

        public void publish(FleetEvent event) {
            List<Observer<FleetEvent>> list =
                    observers.get(event.type());

            if (list == null) {
                return;
            }

            for (Observer<FleetEvent> observer : list) {
                observer.onEvent(event);
            }
        }

        public int subscriberCount(EventType type) {
            List<Observer<FleetEvent>> list = observers.get(type);
            return list == null ? 0 : list.size();
        }
    }

    public static FleetEvent createEvent(
            EventType type,
            String vehicleId,
            Map<String, String> data) {

        return new FleetEvent(type, vehicleId, Instant.now(), data);
    }

    public static class EventAggregator
            implements Observer<FleetEvent> {

        private final EventSubject subject;
        private final AtomicInteger received = new AtomicInteger();

        public EventAggregator(EventSubject subject) {
            this.subject = subject;
        }

        @Override
        public void onEvent(FleetEvent event) {
            int count = received.incrementAndGet();

            if (count % 10 == 0
                    && event.type() != EventType.FLEET_SUMMARY) {
                subject.publish(createEvent(
                        EventType.FLEET_SUMMARY,
                        "FLEET",
                        Map.of("eventsReceived", String.valueOf(count))
                ));
            }
        }

        public int getReceivedCount() {
            return received.get();
        }
    }
}