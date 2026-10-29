package fleetlogistics;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static fleetlogistics.FleetModel.*;
import static fleetlogistics.ObserverSystem.*;
import static fleetlogistics.Visitors.*;

public class FleetLogisticsApp {

    public static void main(String[] args) throws InterruptedException {
        Fleet fleet = new Fleet("Main Fleet");
        Depot depot = new Depot("Astana Depot");

        depot.add(new Truck("TR-101", 1200, 100));
        depot.add(new Van("VN-202", 500, 40));
        depot.add(new Drone("DR-303", 900, 10));
        depot.add(new ElectricTruck("ET-404", 1500, 0));

        fleet.add(depot);

        StatisticsVisitor statistics = new StatisticsVisitor();
        fleet.accept(statistics);

        FuelCostVisitor fuelCost = new FuelCostVisitor();
        fleet.accept(fuelCost);

        MaintenanceFilterVisitor maintenance =
                new MaintenanceFilterVisitor();
        fleet.accept(maintenance);

        Co2ReportVisitor co2Report = new Co2ReportVisitor();
        fleet.accept(co2Report);

        JsonExportVisitor json = new JsonExportVisitor();
        fleet.accept(json);

        System.out.println("=== Fleet Logistics Report ===");
        System.out.println("Fleet: " + fleet.getName());
        System.out.println("Vehicles: " + statistics.getVehicleCount());
        System.out.println("Total mileage: "
                + statistics.getTotalMileage());
        System.out.printf("Fuel cost: %.2f%n", fuelCost.getTotalCost());
        System.out.printf("CO2 kg: %.2f%n", co2Report.getTotalCo2());
        System.out.println("Maintenance due: "
                + maintenance.getVehiclesDue());
        System.out.println("JSON lines:");
        System.out.println(json.getJsonLines());

        EventSubject subject = new EventSubject();

        Observer<FleetEvent> logger =
                event -> System.out.println(
                        "Event: " + event.type()
                                + " | Vehicle: " + event.vehicleId()
                                + " | Data: " + event.data());

        subject.subscribe(EventType.GPS_UPDATE, logger);
        subject.subscribe(EventType.ETA_UPDATE, logger);
        subject.subscribe(EventType.GEOFENCE_ALERT, logger);
        subject.subscribe(EventType.FUEL_LOW, logger);
        subject.subscribe(EventType.FLEET_SUMMARY, logger);

        EventAggregator aggregator = new EventAggregator(subject);
        subject.subscribe(EventType.GPS_UPDATE, aggregator);
        subject.subscribe(EventType.ETA_UPDATE, aggregator);
        subject.subscribe(EventType.GEOFENCE_ALERT, aggregator);
        subject.subscribe(EventType.FUEL_LOW, aggregator);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                subject.publish(createEvent(
                        EventType.GPS_UPDATE,
                        "TR-101",
                        Map.of("lat", "51.1694", "lon", "71.4491")
                ));
            }
        });

        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                subject.publish(createEvent(
                        EventType.ETA_UPDATE,
                        "VN-202",
                        Map.of("minutes", String.valueOf(30 + i))
                ));
            }
        });

        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                EventType type = i % 2 == 0
                        ? EventType.GEOFENCE_ALERT
                        : EventType.FUEL_LOW;

                subject.publish(createEvent(
                        type,
                        "DR-303",
                        Map.of("status", "alert-" + i)
                ));
            }
        });

        executor.shutdown();

        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        subject.unsubscribe(EventType.GPS_UPDATE, aggregator);
        subject.unsubscribe(EventType.ETA_UPDATE, aggregator);
        subject.unsubscribe(EventType.GEOFENCE_ALERT, aggregator);
        subject.unsubscribe(EventType.FUEL_LOW, aggregator);

        System.out.println("Events processed by aggregator: "
                + aggregator.getReceivedCount());
        System.out.println("Application finished.");
    }
}