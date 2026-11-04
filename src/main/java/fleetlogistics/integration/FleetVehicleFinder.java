
package fleetlogistics.integration;

import fleetlogistics.model.*;

public class FleetVehicleFinder {

    public Vehicle findById(Fleet fleet, String vehicleId) {
        for (FleetElement element : fleet.getChildren()) {

            Vehicle result = findInElement(element, vehicleId);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private Vehicle findInElement(
            FleetElement element, String vehicleId) {

        if (element instanceof Vehicle vehicle) {
            return vehicle.getId().equals(vehicleId)
                    ? vehicle : null;
        }

        if (element instanceof Depot depot) {
            for (FleetElement child : depot.getChildren()) {
                Vehicle result = findInElement(child, vehicleId);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
