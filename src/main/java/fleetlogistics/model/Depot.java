package fleetlogistics.model;

import fleetlogistics.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public class Depot implements FleetElement {

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
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDepot(this);
    }
}