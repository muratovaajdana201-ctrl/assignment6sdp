package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

public class Truck extends Vehicle {

    public Truck(String id, double mileage, double fuelLiters) {
        super(id, mileage, fuelLiters);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTruck(this);
    }
}