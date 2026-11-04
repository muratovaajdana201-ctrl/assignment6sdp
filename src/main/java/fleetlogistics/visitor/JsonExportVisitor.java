package fleetlogistics.visitor;

import fleetlogistics.model.*;

public class JsonExportVisitor implements Visitor<String> {

    @Override
    public String visitFleet(Fleet fleet) {
        StringBuilder json = new StringBuilder();

        json.append("{\"fleet\":\"")
                .append(escape(fleet.getName()))
                .append("\",\"depots\":[");

        appendChildren(json, fleet.getChildren());

        json.append("]}");
        return json.toString();
    }

    @Override
    public String visitDepot(Depot depot) {
        StringBuilder json = new StringBuilder();

        json.append("{\"depot\":\"")
                .append(escape(depot.getName()))
                .append("\",\"vehicles\":[");

        appendChildren(json, depot.getChildren());

        json.append("]}");
        return json.toString();
    }

    private void appendChildren(
            StringBuilder json,
            Iterable<FleetElement> children) {

        boolean first = true;

        for (FleetElement child : children) {
            if (!first) {
                json.append(",");
            }

            json.append(child.accept(this));
            first = false;
        }
    }

    private String vehicleJson(Vehicle vehicle, String type) {
        return "{\"type\":\"" + type
                + "\",\"id\":\"" + escape(vehicle.getId())
                + "\",\"mileage\":" + vehicle.getMileage()
                + ",\"fuelLiters\":" + vehicle.getFuelLiters()
                + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    @Override
    public String visitTruck(Truck truck) {
        return vehicleJson(truck, "Truck");
    }

    @Override
    public String visitVan(Van van) {
        return vehicleJson(van, "Van");
    }

    @Override
    public String visitDrone(Drone drone) {
        return vehicleJson(drone, "Drone");
    }

    @Override
    public String visitElectricTruck(ElectricTruck truck) {
        return vehicleJson(truck, "ElectricTruck");
    }
}