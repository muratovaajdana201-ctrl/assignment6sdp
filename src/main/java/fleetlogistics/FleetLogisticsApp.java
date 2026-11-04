
package fleetlogistics;

import fleetlogistics.model.*;
import fleetlogistics.visitor.*;
import fleetlogistics.observer.*;
import fleetlogistics.listener.*;
import fleetlogistics.service.*;
import fleetlogistics.integration.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FleetLogisticsApp {

    public static void main(String[] args)
            throws InterruptedException {

        // 1. Create the fleet structure
        Fleet fleet = new Fleet("Main Fleet");
        Depot depot = new Depot("Astana Depot");

        depot.add(new Truck("TR-101", 1200, 100));
        depot.add(new Van("VN-202", 500, 40));
        depot.add(new Drone("DR-303", 900, 10));
        depot.add(new ElectricTruck("ET-404", 1500, 0));

        fleet.add(depot);

        // 2. Run visitors
        Statistics statistics =
                fleet.accept(new StatisticsVisitor());

        double fuelCost =
                fleet.accept(new FuelCostVisitor());

        double co2 =
                fleet.accept(new Co2ReportVisitor());

        List<Vehicle> maintenance =
                fleet.accept(new MaintenanceFilterVisitor());

        String json =
                fleet.accept(new JsonExportVisitor());

        System.out.println("=== FLEET LOGISTICS REPORT ===");
        System.out.println("Fleet: " + fleet.getName());
        System.out.println("Vehicles: "
                + statistics.vehicleCount());
        System.out.println("Total mileage: "
                + statistics.totalMileage());

        System.out.printf("Fuel cost: %.2f%n", fuelCost);
        System.out.printf("CO2 emissions: %.2f kg%n", co2);

        System.out.println("Maintenance required:");
        for (Vehicle vehicle : maintenance) {
            System.out.println(" - " + vehicle.getId());
        }

        System.out.println("JSON export:");
        System.out.println(json);

        // 3. Create two event subjects
        EventSubject mainSubject = new EventSubject();
        EventSubject summarySubject = new EventSubject();

        // 4. Create listeners
        GpsMapListener gpsListener = new GpsMapListener();
        EtaUpdateListener etaListener = new EtaUpdateListener();
        GeofenceAlertListener geofenceListener =
                new GeofenceAlertListener();
        FuelAlertListener fuelListener =
                new FuelAlertListener();
        MaintenanceNotificationListener maintenanceListener =
                new MaintenanceNotificationListener();

        FleetEventLogger logger = new FleetEventLogger();

        // 5. Subscribe listeners by event type
        mainSubject.subscribe(
                EventType.GPS_UPDATE, gpsListener);

        mainSubject.subscribe(
                EventType.ETA_UPDATE, etaListener);

        mainSubject.subscribe(
                EventType.GEOFENCE_ALERT, geofenceListener);

        mainSubject.subscribe(
                EventType.FUEL_LOW, fuelListener);

        mainSubject.subscribe(
                EventType.MAINTENANCE_REQUIRED,
                maintenanceListener);

        mainSubject.subscribe(
                EventType.MAINTENANCE_REQUIRED, logger);

        // 6. Observer chain
        EventAggregator aggregator =
                new EventAggregator(summarySubject);

        mainSubject.subscribe(
                EventType.GPS_UPDATE, aggregator);
        mainSubject.subscribe(
                EventType.ETA_UPDATE, aggregator);
        mainSubject.subscribe(
                EventType.GEOFENCE_ALERT, aggregator);
        mainSubject.subscribe(
                EventType.FUEL_LOW, aggregator);

        summarySubject.subscribe(
                EventType.FLEET_SUMMARY, logger);

        // 7. Observer and Visitor integration
        FleetVehicleFinder finder = new FleetVehicleFinder();

        MaintenanceIntegrationListener integration =
                new MaintenanceIntegrationListener(
                        finder,
                        fleet,
                        mainSubject,
                        new MaintenanceFilterVisitor()
                );

        mainSubject.subscribe(
                EventType.GEOFENCE_ALERT, integration);

        // 8. Create event producers
        GpsTracker gpsTracker = new GpsTracker(mainSubject);

        VehicleTelemetrySource telemetry =
                new VehicleTelemetrySource(mainSubject);

        // 9. Configure producer thread count
        int threadCount = 3;

        if (args.length > 0) {
            threadCount = Integer.parseInt(args[0]);
        }

        if (threadCount < 1) {
            throw new IllegalArgumentException(
                    "Thread count must be positive");
        }

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        // 10. GPS producer
        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                gpsTracker.updateLocation(
                        "TR-101",
                        51.1694 + i * 0.001,
                        71.4491
                );
            }
        });

        // 11. ETA producer
        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                telemetry.updateEta(
                        "VN-202",
                        30 + i
                );
            }
        });

        // 12. Alert producer
        executor.submit(() -> {
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    telemetry.reportGeofence(
                            "TR-101",
                            "Astana Center",
                            true
                    );
                } else {
                    telemetry.reportFuelLevel(
                            "VN-202",
                            10.0
                    );
                }
            }
        });

        // 13. Graceful shutdown
        executor.shutdown();

        if (!executor.awaitTermination(
                30, TimeUnit.SECONDS)) {

            executor.shutdownNow();

            if (!executor.awaitTermination(
                    10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Executor did not terminate");
            }

            throw new IllegalStateException(
                    "Producer tasks exceeded the timeout");
        }

        // 14. Print final results
        System.out.println();
        System.out.println("=== EVENT SUMMARY ===");
        System.out.println("Events processed: "
                + aggregator.getReceivedCount());

        System.out.println("Application finished.");
    }
}
