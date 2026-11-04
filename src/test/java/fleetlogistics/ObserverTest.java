
package fleetlogistics;

import fleetlogistics.observer.*;
import fleetlogistics.listener.EventAggregator;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    private FleetEvent event(EventType type) {
        return new FleetEvent(type, "TR-101", Map.of());
    }

    @Test
    void subscribedListenerReceivesEvent() {
        EventSubject subject = new EventSubject();
        AtomicInteger count = new AtomicInteger();

        subject.subscribe(EventType.GPS_UPDATE,
                e -> count.incrementAndGet());

        subject.publish(event(EventType.GPS_UPDATE));

        assertEquals(1, count.get());
    }

    @Test
    void differentEventTypeIsIgnored() {
        EventSubject subject = new EventSubject();
        AtomicInteger count = new AtomicInteger();

        subject.subscribe(EventType.GPS_UPDATE,
                e -> count.incrementAndGet());

        subject.publish(event(EventType.ETA_UPDATE));

        assertEquals(0, count.get());
    }

    @Test
    void unsubscribeStopsNotifications() {
        EventSubject subject = new EventSubject();
        AtomicInteger count = new AtomicInteger();

        Observer<FleetEvent> listener =
                e -> count.incrementAndGet();

        subject.subscribe(EventType.GPS_UPDATE, listener);
        subject.unsubscribe(EventType.GPS_UPDATE, listener);
        subject.publish(event(EventType.GPS_UPDATE));

        assertEquals(0, count.get());
    }

    @Test
    void listenerCanUnsubscribeInsideHandler() {
        EventSubject subject = new EventSubject();
        AtomicInteger count = new AtomicInteger();

        @SuppressWarnings("unchecked")
        Observer<FleetEvent>[] holder = new Observer[1];

        holder[0] = e -> {
            count.incrementAndGet();
            subject.unsubscribe(EventType.GPS_UPDATE, holder[0]);
        };

        subject.subscribe(EventType.GPS_UPDATE, holder[0]);

        subject.publish(event(EventType.GPS_UPDATE));
        subject.publish(event(EventType.GPS_UPDATE));

        assertEquals(1, count.get());
    }

    @Test
    void listenerCanSubscribeInsideHandler() {
        EventSubject subject = new EventSubject();
        AtomicInteger secondCount = new AtomicInteger();

        Observer<FleetEvent> second =
                e -> secondCount.incrementAndGet();

        subject.subscribe(EventType.GPS_UPDATE,
                e -> subject.subscribe(EventType.GPS_UPDATE, second));

        subject.publish(event(EventType.GPS_UPDATE));
        subject.publish(event(EventType.GPS_UPDATE));

        assertEquals(1, secondCount.get());
    }

    @Test
    void duplicateSubscriptionIsNotAdded() {
        EventSubject subject = new EventSubject();
        AtomicInteger count = new AtomicInteger();

        Observer<FleetEvent> listener =
                e -> count.incrementAndGet();

        subject.subscribe(EventType.GPS_UPDATE, listener);
        subject.subscribe(EventType.GPS_UPDATE, listener);

        subject.publish(event(EventType.GPS_UPDATE));

        assertEquals(1, count.get());
    }

    @Test
    void eventDataIsImmutable() {
        FleetEvent event = new FleetEvent(
                EventType.GPS_UPDATE,
                "TR-101",
                Map.of("latitude", "51.1")
        );

        assertThrows(UnsupportedOperationException.class,
                () -> event.data().put("latitude", "52.0"));
    }

    @Test
    void aggregatorPublishesSummary() {
        EventSubject input = new EventSubject();
        EventSubject output = new EventSubject();

        EventAggregator aggregator = new EventAggregator(output);
        AtomicInteger summaries = new AtomicInteger();

        input.subscribe(EventType.GPS_UPDATE, aggregator);

        output.subscribe(EventType.FLEET_SUMMARY,
                e -> summaries.incrementAndGet());

        input.publish(event(EventType.GPS_UPDATE));

        assertEquals(1, aggregator.getReceivedCount());
        assertEquals(1, summaries.get());
    }

    @Test
    void stressTestFourThreadsFourThousandEvents()
            throws Exception {

        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();

        subject.subscribe(EventType.GPS_UPDATE,
                e -> received.incrementAndGet());

        ExecutorService executor =
                Executors.newFixedThreadPool(4);

        try {
            Future<?>[] tasks = new Future<?>[4];

            for (int t = 0; t < 4; t++) {
                tasks[t] = executor.submit(() -> {
                    for (int i = 0; i < 1000; i++) {
                        subject.publish(
                                event(EventType.GPS_UPDATE));
                    }
                });
            }

            for (Future<?> task : tasks) {
                task.get(30, TimeUnit.SECONDS);
            }

            assertEquals(4000, received.get());

        } finally {
            executor.shutdown();

            if (!executor.awaitTermination(
                    30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}
