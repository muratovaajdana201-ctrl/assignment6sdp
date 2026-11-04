package fleetlogistics.visitor;

import fleetlogistics.model.*;

public interface Visitor<R> {

    R visitFleet(Fleet fleet);

    R visitDepot(Depot depot);

    R visitTruck(Truck truck);

    R visitVan(Van van);

    R visitDrone(Drone drone);

    R visitElectricTruck(ElectricTruck truck);
}