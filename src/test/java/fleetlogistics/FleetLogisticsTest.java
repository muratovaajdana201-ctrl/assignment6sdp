package fleetlogistics;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

import static fleetlogistics.FleetModel.*;
import static fleetlogistics.ObserverSystem.*;
import static fleetlogistics.Visitors.*;

class FleetLogisticsTest {

    private Fleet createSampleFleet() {
        Fleet fleet = new Fleet("Test Fleet");
        Depot depot = new Depot("Test Depot");

        depot.add(new Truck("TR-1", 1200, 100));
        depot.add(new Van("VN-1", 500, 40));
        depot.add(new Drone("DR-1", 900, 10));
        depot.add(new ElectricTruck("ET-1", 1500, 0));

        fleet.add(depot);
        return fleet;
    }

    @Test
    void fleetStoresItsName() {
        assertEquals("Test Fleet", createSampleFleet().getName());
    }

    @Test
    void depotStoresItsName() {
        assertEquals("Test Depot", new Depot("Test Depot").getName());
    }

    @Test
    void compositeContainsDepotAndVehicles() {
        Fleet fleet = createSampleFleet();

        assertEquals(1, fleet.getChildren().size());
        Depot depot = (Depot) fleet.getChildren().get(0);
        assertEquals(4, depot.getChildren().size());
    }

    @Test
    void childrenListCannotBeModifiedExternally() {
        Fleet fleet = createSampleFleet();

        assertThrows(
                UnsupportedOperationException.class,
                () -> fleet.getChildren().clear()
        );
    }

    @Test
    void vehicleRejectsNegativeMileage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Truck("TR-X", -1, 10)
        );
    }

    @Test
    void vehicleRejectsNegativeFuel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Van("VN-X", 10, -1)
        );
    }

    @Test
    void vehicleRequiresAnId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Truck(" ", 10, 10)
        );
    }

    @Test
    void statisticsCountsAllVehicles() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        createSampleFleet().accept(visitor);

        assertEquals(4, visitor.getVehicleCount());
    }

    @Test
    void statisticsCalculatesTotalMileage() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        createSampleFleet().accept(visitor);

        assertEquals(4100, visitor.getTotalMileage(), 0.001);
    }

    @Test
    void fuelCostCalculatesTotalCost() {
        FuelCostVisitor visitor = new FuelCostVisitor();
        createSampleFleet().accept(visitor);

        // 100 * 1.80 + 40 * 1.80 + 10 * 1.20 + 0
        assertEquals(264.0, visitor.getTotalCost(), 0.001);
    }

    @Test
    void maintenanceVisitorFindsVehiclesDue() {
        MaintenanceFilterVisitor visitor =
                new MaintenanceFilterVisitor();

        createSampleFleet().accept(visitor);

        assertTrue(visitor.getVehiclesDue().contains("TR-1"));
        assertTrue(visitor.getVehiclesDue().contains("ET-1"));
        assertFalse(visitor.getVehiclesDue().contains("VN-1"));
    }

    @Test
    void co2VisitorCalculatesEmissions() {
        Co2ReportVisitor visitor = new Co2ReportVisitor();
        createSampleFleet().accept(visitor);

        // 100 * 2.68 + 40 * 2.68 + 10 * 1.10
        assertEquals(386.2, visitor.getTotalCo2(), 0.001);
    }

    @Test
    void jsonVisitorExportsVehicleIds() {
        JsonExportVisitor visitor = new JsonExportVisitor();
        createSampleFleet().accept(visitor);

        assertTrue(visitor.getJsonLines().contains("TR-1"));
        assertTrue(visitor.getJsonLines().contains("VN-1"));
        assertTrue(visitor.getJsonLines().contains("DR-1"));
        assertTrue(visitor.getJsonLines().contains("ET-1"));
    }

    @Test
    void subscribeReceivesEvent() {
        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();

        subject.subscribe(
                EventType.GPS_UPDATE,
                event -> received.incrementAndGet()
        );

        subject.publish(createEvent(
                EventType.GPS_UPDATE,
                "TR-1",
                Map.of("lat", "51.1")
        ));

        assertEquals(1, received.get());
    }

    @Test
    void subscriberReceivesOnlyItsEventType() {
        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();

        subject.subscribe(
                EventType.GPS_UPDATE,
                event -> received.incrementAndGet()
        );

        subject.publish(createEvent(
                EventType.FUEL_LOW,
                "TR-1",
                Map.of("level", "low")
        ));

        assertEquals(0, received.get());
    }

    @Test
    void unsubscribeStopsFutureEvents() {
        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();
        Observer<FleetEvent> observer =
                event -> received.incrementAndGet();

        subject.subscribe(EventType.GPS_UPDATE, observer);
        subject.unsubscribe(EventType.GPS_UPDATE, observer);

        subject.publish(createEvent(
                EventType.GPS_UPDATE,
                "TR-1",
                Map.of()
        ));

        assertEquals(0, received.get());
    }

    @Test
    void observerCanUnsubscribeDuringEventHandling() {
        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();

        @SuppressWarnings("unchecked")
        Observer<FleetEvent>[] holder = new Observer[1];

        holder[0] = event -> {
            received.incrementAndGet();
            subject.unsubscribe(EventType.GPS_UPDATE, holder[0]);
        };

        subject.subscribe(EventType.GPS_UPDATE, holder[0]);

        subject.publish(createEvent(
                EventType.GPS_UPDATE, "TR-1", Map.of()));
        subject.publish(createEvent(
                EventType.GPS_UPDATE, "TR-1", Map.of()));

        assertEquals(1, received.get());
    }

    @Test
    void eventDataIsImmutable() {
        FleetEvent event = createEvent(
                EventType.GPS_UPDATE,
                "TR-1",
                Map.of("lat", "51.1")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> event.data().put("lon", "71.4")
        );
    }

    @Test
    void aggregatorCountsIncomingEvents() {
        EventSubject subject = new EventSubject();
        EventAggregator aggregator = new EventAggregator(subject);

        subject.subscribe(EventType.GPS_UPDATE, aggregator);

        for (int i = 0; i < 5; i++) {
            subject.publish(createEvent(
                    EventType.GPS_UPDATE,
                    "TR-1",
                    Map.of()
            ));
        }

        assertEquals(5, aggregator.getReceivedCount());
    }

    @Test
    void aggregatorPublishesSummaryEveryTenEvents() {
        EventSubject subject = new EventSubject();
        EventAggregator aggregator = new EventAggregator(subject);
        AtomicInteger summaries = new AtomicInteger();

        subject.subscribe(EventType.GPS_UPDATE, aggregator);
        subject.subscribe(
                EventType.FLEET_SUMMARY,
                event -> summaries.incrementAndGet()
        );

        for (int i = 0; i < 10; i++) {
            subject.publish(createEvent(
                    EventType.GPS_UPDATE,
                    "TR-1",
                    Map.of()
            ));
        }

        assertEquals(1, summaries.get());
    }

    @Test
    void concurrentPublishingHandlesFourThousandEvents()
            throws InterruptedException {

        EventSubject subject = new EventSubject();
        AtomicInteger received = new AtomicInteger();

        subject.subscribe(
                EventType.GPS_UPDATE,
                event -> received.incrementAndGet()
        );

        int threads = 4;
        int eventsPerThread = 1000;

        ExecutorService executor =
                Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    start.await();

                    for (int i = 0; i < eventsPerThread; i++) {
                        subject.publish(createEvent(
                                EventType.GPS_UPDATE,
                                "TR-1",
                                Map.of("source", "stress-test")
                        ));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        start.countDown();

        assertTrue(finished.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(4000, received.get());
    }
}