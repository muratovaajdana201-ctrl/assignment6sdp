package fleetlogistics.model;
import fleetlogistics.visitor.Visitor;

public abstract class Vehicle implements FleetElement {

    private final String id;
    private final double mileage;
    private final double fuelLiters;

    protected Vehicle(
            String id,
            double mileage,
            double fuelLiters) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID is required");
        }

        if (!Double.isFinite(mileage)
                || !Double.isFinite(fuelLiters)
                || mileage < 0
                || fuelLiters < 0) {

            throw new IllegalArgumentException(
                    "Mileage and fuel must be non-negative");
        }

        this.id = id;
        this.mileage = mileage;
        this.fuelLiters = fuelLiters;
    }

    public String getId() {
        return id;
    }

    public double getMileage() {
        return mileage;
    }

    public double getFuelLiters() {
        return fuelLiters;
    }
    @Override
    public abstract <R> R accept(Visitor<R> visitor);
}