package fleetlogistics.visitor;

import fleetlogistics.model.*;

public class FuelCostVisitor implements Visitor<Double> {

    @Override
    public Double visitFleet(Fleet fleet) {
        double total = 0;

        for (FleetElement child : fleet.getChildren()) {
            total += child.accept(this);
        }

        return total;
    }

    @Override
    public Double visitDepot(Depot depot) {
        double total = 0;

        for (FleetElement child : depot.getChildren()) {
            total += child.accept(this);
        }

        return total;
    }

    @Override
    public Double visitTruck(Truck truck) {
        return truck.getFuelLiters() * 1.80;
    }

    @Override
    public Double visitVan(Van van) {
        return van.getFuelLiters() * 1.80;
    }

    @Override
    public Double visitDrone(Drone drone) {
        return drone.getFuelLiters() * 1.20;
    }

    @Override
    public Double visitElectricTruck(ElectricTruck truck) {
        return 0.0;
    }
}