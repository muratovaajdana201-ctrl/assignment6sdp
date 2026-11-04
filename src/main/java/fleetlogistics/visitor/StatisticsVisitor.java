package fleetlogistics.visitor;

import fleetlogistics.model.*;

public class StatisticsVisitor
        implements Visitor<Statistics> {

    @Override
    public Statistics visitFleet(Fleet fleet) {
        return calculate(fleet.getChildren());
    }

    @Override
    public Statistics visitDepot(Depot depot) {
        return calculate(depot.getChildren());
    }

    private Statistics calculate(
            Iterable<FleetElement> elements) {

        int count = 0;
        double mileage = 0;

        for (FleetElement element : elements) {
            Statistics result = element.accept(this);

            count += result.vehicleCount();
            mileage += result.totalMileage();
        }

        return new Statistics(count, mileage);
    }

    private Statistics vehicleResult(Vehicle vehicle) {
        return new Statistics(1, vehicle.getMileage());
    }

    @Override
    public Statistics visitTruck(Truck truck) {
        return vehicleResult(truck);
    }

    @Override
    public Statistics visitVan(Van van) {
        return vehicleResult(van);
    }

    @Override
    public Statistics visitDrone(Drone drone) {
        return vehicleResult(drone);
    }

    @Override
    public Statistics visitElectricTruck(ElectricTruck truck) {
        return vehicleResult(truck);
    }
}