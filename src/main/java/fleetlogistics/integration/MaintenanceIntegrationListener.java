
package fleetlogistics.integration;

import fleetlogistics.model.*;
import fleetlogistics.observer.*;
import fleetlogistics.visitor.MaintenanceFilterVisitor;

import java.util.List;
import java.util.Map;

public class MaintenanceIntegrationListener
        implements Observer<FleetEvent> {

    private final FleetVehicleFinder finder;
    private final Fleet fleet;
    private final Subject<FleetEvent> subject;
    private final MaintenanceFilterVisitor maintenanceVisitor;

    public MaintenanceIntegrationListener(
            FleetVehicleFinder finder,
            Fleet fleet,
            Subject<FleetEvent> subject,
            MaintenanceFilterVisitor maintenanceVisitor) {

        this.finder = finder;
        this.fleet = fleet;
        this.subject = subject;
        this.maintenanceVisitor = maintenanceVisitor;
    }

    @Override
    public void onEvent(FleetEvent event) {

        if (event.type() != EventType.GEOFENCE_ALERT) {
            return;
        }

        Vehicle vehicle = finder.findById(
                fleet,
                event.vehicleId()
        );

        if (vehicle == null) {
            return;
        }

        List<Vehicle> vehiclesDue =
                vehicle.accept(maintenanceVisitor);

        for (Vehicle due : vehiclesDue) {

            FleetEvent maintenanceEvent = new FleetEvent(
                    EventType.MAINTENANCE_REQUIRED,
                    due.getId(),
                    Map.of(
                            "mileage",
                            String.valueOf(due.getMileage()),
                            "status",
                            "Inspection required"
                    )
            );

            subject.publish(maintenanceEvent);
        }
    }
}
