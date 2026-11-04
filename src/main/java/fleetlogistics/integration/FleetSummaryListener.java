package fleetlogistics.integration;
import fleetlogistics.observer.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
public class FleetSummaryListener
        implements Observer<FleetEvent> {
    private final EventSubject subject;
    private final LongAdder receivedEvents = new LongAdder();

    public FleetSummaryListener(EventSubject subject) {
        this.subject = subject;
    }

    @Override
    public void onEvent(FleetEvent event) {

        receivedEvents.increment();

        if (event.type() == EventType.FLEET_SUMMARY) {
            return;
        }

        long count = receivedEvents.sum();

        FleetEvent summary = new FleetEvent(
                EventType.FLEET_SUMMARY,
                "FLEET",
                Map.of("totalEvents", String.valueOf(count))
        );

        subject.publish(summary);
    }

    public long getReceivedEvents() {
        return receivedEvents.sum();
    }
}