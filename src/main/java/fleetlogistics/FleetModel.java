package fleetlogistics;

import java.util.ArrayList;
import java.util.List;

public final class FleetModel {

    private FleetModel() {
    }

    public interface FleetElement {
        void accept(FleetVisitor visitor);
    }

    public interface FleetVisitor {
        void visitFleet(Fleet fleet);
        void visitDepot(Depot depot);
        void visitTruck(Truck truck);
        void visitVan(Van van);
        void visitDrone(Drone drone);
    }

    public static class Fleet implements FleetElement {
        private final String name;
        private final List<FleetElement> children = new ArrayList<>();

        public Fleet(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void add(FleetElement element) {
            children.add(element);
        }

        public List<FleetElement> getChildren() {
            return List.copyOf(children);
        }

        @Override
        public void accept(FleetVisitor visitor) {
            visitor.visitFleet(this);
        }
    }

    public static class Depot implements FleetElement {
        private final String name;
        private final List<FleetElement> children = new ArrayList<>();

        public Depot(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void add(FleetElement element) {
            children.add(element);
        }

        public List<FleetElement> getChildren() {
            return List.copyOf(children);
        }

        @Override
        public void accept(FleetVisitor visitor) {
            visitor.visitDepot(this);
        }
    }

    public abstract static class Vehicle implements FleetElement {
        private final String id;
        private final double mileage;
        private final double fuelLiters;

        protected Vehicle(String id, double mileage, double fuelLiters) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Vehicle ID is required");
            }
            if (mileage < 0 || fuelLiters < 0) {
                throw new IllegalArgumentException(
                        "Mileage and fuel cannot be negative");
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
    }

    public static class Truck extends Vehicle {
        public Truck(String id, double mileage, double fuelLiters) {
            super(id, mileage, fuelLiters);
        }

        @Override
        public void accept(FleetVisitor visitor) {
            visitor.visitTruck(this);
        }
    }

    public static class Van extends Vehicle {
        public Van(String id, double mileage, double fuelLiters) {
            super(id, mileage, fuelLiters);
        }

        @Override
        public void accept(FleetVisitor visitor) {
            visitor.visitVan(this);
        }
    }

    public static class Drone extends Vehicle {
        public Drone(String id, double mileage, double fuelLiters) {
            super(id, mileage, fuelLiters);
        }

        @Override
        public void accept(FleetVisitor visitor) {
            visitor.visitDrone(this);
        }
    }
}