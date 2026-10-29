package fleetlogistics;

import java.util.ArrayList;
import java.util.List;

import static fleetlogistics.FleetModel.*;

public final class Visitors {

    private Visitors() {
    }

    public static class FuelCostVisitor implements FleetVisitor {
        private double totalCost;

        @Override
        public void visitFleet(Fleet fleet) {
            for (FleetElement child : fleet.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitDepot(Depot depot) {
            for (FleetElement child : depot.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitTruck(Truck truck) {
            totalCost += truck.getFuelLiters() * 1.80;
        }

        @Override
        public void visitVan(Van van) {
            totalCost += van.getFuelLiters() * 1.80;
        }

        @Override
        public void visitDrone(Drone drone) {
            totalCost += drone.getFuelLiters() * 1.20;
        }

        public double getTotalCost() {
            return totalCost;
        }
    }

    public static class StatisticsVisitor implements FleetVisitor {
        private int vehicleCount;
        private double totalMileage;

        @Override
        public void visitFleet(Fleet fleet) {
            for (FleetElement child : fleet.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitDepot(Depot depot) {
            for (FleetElement child : depot.getChildren()) {
                child.accept(this);
            }
        }

        private void count(Vehicle vehicle) {
            vehicleCount++;
            totalMileage += vehicle.getMileage();
        }

        @Override
        public void visitTruck(Truck truck) {
            count(truck);
        }

        @Override
        public void visitVan(Van van) {
            count(van);
        }

        @Override
        public void visitDrone(Drone drone) {
            count(drone);
        }

        public int getVehicleCount() {
            return vehicleCount;
        }

        public double getTotalMileage() {
            return totalMileage;
        }
    }

    public static class MaintenanceFilterVisitor
            implements FleetVisitor {

        private final List<String> vehiclesDue = new ArrayList<>();

        @Override
        public void visitFleet(Fleet fleet) {
            for (FleetElement child : fleet.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitDepot(Depot depot) {
            for (FleetElement child : depot.getChildren()) {
                child.accept(this);
            }
        }

        private void check(Vehicle vehicle) {
            if (vehicle.getMileage() >= 1000) {
                vehiclesDue.add(vehicle.getId());
            }
        }

        @Override
        public void visitTruck(Truck truck) {
            check(truck);
        }

        @Override
        public void visitVan(Van van) {
            check(van);
        }

        @Override
        public void visitDrone(Drone drone) {
            check(drone);
        }

        public List<String> getVehiclesDue() {
            return List.copyOf(vehiclesDue);
        }
    }

    public static class JsonExportVisitor implements FleetVisitor {
        private final List<String> records = new ArrayList<>();

        @Override
        public void visitFleet(Fleet fleet) {
            records.add("{\"fleet\":\"" + fleet.getName() + "\"}");
            for (FleetElement child : fleet.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitDepot(Depot depot) {
            records.add("{\"depot\":\"" + depot.getName() + "\"}");
            for (FleetElement child : depot.getChildren()) {
                child.accept(this);
            }
        }

        private void addVehicle(Vehicle vehicle, String type) {
            records.add("{\"type\":\"" + type
                    + "\",\"id\":\"" + vehicle.getId()
                    + "\",\"mileage\":" + vehicle.getMileage()
                    + ",\"fuelLiters\":" + vehicle.getFuelLiters()
                    + "}");
        }

        @Override
        public void visitTruck(Truck truck) {
            addVehicle(truck, "Truck");
        }

        @Override
        public void visitVan(Van van) {
            addVehicle(van, "Van");
        }

        @Override
        public void visitDrone(Drone drone) {
            addVehicle(drone, "Drone");
        }

        public String getJsonLines() {
            return String.join(System.lineSeparator(), records);
        }
    }
    public static class Co2ReportVisitor implements FleetVisitor {
        private double totalCo2;

        @Override
        public void visitFleet(Fleet fleet) {
            for (FleetElement child : fleet.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitDepot(Depot depot) {
            for (FleetElement child : depot.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visitTruck(Truck truck) {
            totalCo2 += truck.getFuelLiters() * 2.68;
        }

        @Override
        public void visitVan(Van van) {
            totalCo2 += van.getFuelLiters() * 2.68;
        }

        @Override
        public void visitDrone(Drone drone) {
            totalCo2 += drone.getFuelLiters() * 1.10;
        }

        public double getTotalCo2() {
            return totalCo2;
        }
    }
}