
package fleetlogistics.observer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventSubject implements Subject<FleetEvent> {

    private final ConcurrentHashMap<
            EventType,
            CopyOnWriteArrayList<Observer<FleetEvent>>
            > listeners = new ConcurrentHashMap<>();

    @Override
    public void subscribe(
            EventType type,
            Observer<FleetEvent> observer) {

        if (type == null || observer == null) {
            throw new IllegalArgumentException(
                    "Type and observer are required");
        }

        listeners.computeIfAbsent(
                type,
                key -> new CopyOnWriteArrayList<>()
        ).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(
            EventType type,
            Observer<FleetEvent> observer) {

        if (type == null || observer == null) {
            return;
        }

        CopyOnWriteArrayList<Observer<FleetEvent>> subscribers =
                listeners.get(type);

        if (subscribers != null) {
            subscribers.remove(observer);
        }
    }

    @Override
    public void publish(FleetEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Event cannot be null");
        }

        CopyOnWriteArrayList<Observer<FleetEvent>> subscribers =
                listeners.get(event.type());

        if (subscribers == null) {
            return;
        }

        for (Observer<FleetEvent> observer : subscribers) {
            observer.onEvent(event);
        }
    }
}
