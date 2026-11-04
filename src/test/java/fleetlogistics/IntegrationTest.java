
package fleetlogistics;

import fleetlogistics.model.*;
import fleetlogistics.observer.*;
import fleetlogistics.integration.*;
import fleetlogistics.visitor.MaintenanceFilterVisitor;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    private Fleet createFleet() {
        Fleet fleet = new Fleet("Main Fleet");
        Depot depot = new Depot("Astana Depot");

        depot.add(new Truck("TR-101", 1200, 100));
        depot.add(new Van("VN-202", 500, 40));

        fleet.add(depot);
        return fleet;
    }

    @Test
    void finderReturnsCorrectVehicle() {
        FleetVehicleFinder finder = new FleetVehicleFinder();

        Vehicle result =
                finder.findById(createFleet(), "TR-101");

        assertNotNull(result);
        assertEquals("TR-101", result.getId());
    }

    @Test
    void finderReturnsNullForUnknownVehicle() {
        FleetVehicleFinder finder = new FleetVehicleFinder();

        assertNull(finder.findById(
                createFleet(), "UNKNOWN"));
    }

    @Test
    void geofenceAlertTriggersMaintenanceEvent() {
        Fleet fleet = createFleet();
        EventSubject subject = new EventSubject();

        MaintenanceIntegrationListener integration =
                new MaintenanceIntegrationListener(
                        new FleetVehicleFinder(),
                        fleet,
                        subject,
                        new MaintenanceFilterVisitor()
                );

        AtomicInteger count = new AtomicInteger();
        AtomicReference<FleetEvent> received =
                new AtomicReference<>();

        subject.subscribe(
                EventType.GEOFENCE_ALERT, integration);

        subject.subscribe(
                EventType.MAINTENANCE_REQUIRED, e -> {
                    count.incrementAndGet();
                    received.set(e);
                });

        subject.publish(new FleetEvent(
                EventType.GEOFENCE_ALERT,
                "TR-101",
                Map.of("zone", "Astana Center")
        ));

        assertEquals(1, count.get());
        assertEquals("TR-101", received.get().vehicleId());
        assertEquals("Inspection required",
                received.get().data().get("status"));
    }

    @Test
    void lowMileageDoesNotTriggerMaintenance() {
        Fleet fleet = createFleet();
        EventSubject subject = new EventSubject();

        MaintenanceIntegrationListener integration =
                new MaintenanceIntegrationListener(
                        new FleetVehicleFinder(),
                        fleet,
                        subject,
                        new MaintenanceFilterVisitor()
                );

        AtomicInteger count = new AtomicInteger();

        subject.subscribe(
                EventType.GEOFENCE_ALERT, integration);

        subject.subscribe(
                EventType.MAINTENANCE_REQUIRED,
                e -> count.incrementAndGet());

        subject.publish(new FleetEvent(
                EventType.GEOFENCE_ALERT,
                "VN-202",
                Map.of("zone", "Astana Center")
        ));

        assertEquals(0, count.get());
    }
}
