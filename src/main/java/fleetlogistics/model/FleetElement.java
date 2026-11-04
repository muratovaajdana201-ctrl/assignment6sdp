package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

public interface FleetElement {
    <R> R accept(Visitor<R> visitor);
}