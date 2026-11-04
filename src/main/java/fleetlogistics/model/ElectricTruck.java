package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

public class ElectricTruck extends Vehicle {

    public ElectricTruck(
            String id,
            double mileage,
            double fuelLiters) {

        super(id, mileage, fuelLiters);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitElectricTruck(this);
    }
}