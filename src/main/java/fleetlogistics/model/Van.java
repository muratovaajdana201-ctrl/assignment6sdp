package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

public class Van extends Vehicle {

    public Van(String id, double mileage, double fuelLiters) {
        super(id, mileage, fuelLiters);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVan(this);
    }
}