package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

public class Drone extends Vehicle {

    public Drone(String id, double mileage, double fuelLiters) {
        super(id, mileage, fuelLiters);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDrone(this);
    }
}