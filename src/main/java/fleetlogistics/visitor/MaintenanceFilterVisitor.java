
package fleetlogistics.visitor;

import fleetlogistics.model.*;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceFilterVisitor
        implements Visitor<List<Vehicle>> {

    @Override
    public List<Vehicle> visitFleet(Fleet fleet) {
        return collect(fleet.getChildren());
    }

    @Override
    public List<Vehicle> visitDepot(Depot depot) {
        return collect(depot.getChildren());
    }

    private List<Vehicle> collect(
            Iterable<FleetElement> elements) {

        List<Vehicle> result = new ArrayList<>();

        for (FleetElement element : elements) {
            result.addAll(element.accept(this));
        }

        return result;
    }

    private List<Vehicle> check(Vehicle vehicle) {

        if (vehicle.getMileage() >= 1000) {
            return List.of(vehicle);
        }

        return List.of();
    }

    @Override
    public List<Vehicle> visitTruck(Truck truck) {
        return check(truck);
    }

    @Override
    public List<Vehicle> visitVan(Van van) {
        return check(van);
    }

    @Override
    public List<Vehicle> visitDrone(Drone drone) {
        return check(drone);
    }

    @Override
    public List<Vehicle> visitElectricTruck(ElectricTruck truck) {
        return check(truck);
    }
}
