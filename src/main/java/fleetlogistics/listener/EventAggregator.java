
package fleetlogistics.listener;

import fleetlogistics.observer.EventType;
import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;
import fleetlogistics.observer.Subject;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EventAggregator implements Observer<FleetEvent> {

    private final Subject<FleetEvent> outputSubject;
    private final AtomicInteger receivedCount = new AtomicInteger();

    public EventAggregator(Subject<FleetEvent> outputSubject) {
        this.outputSubject = outputSubject;
    }

    @Override
    public void onEvent(FleetEvent event) {

        int count = receivedCount.incrementAndGet();

        FleetEvent summary = new FleetEvent(
                EventType.FLEET_SUMMARY,
                "FLEET",
                Map.of(
                        "lastEvent", event.type().name(),
                        "totalEvents", String.valueOf(count)
                )
        );

        outputSubject.publish(summary);
    }

    public int getReceivedCount() {
        return receivedCount.get();
    }
}
