package fleetlogistics.integration;
import fleetlogistics.observer.*;
public class FleetEventLogger
        implements Observer<FleetEvent> {
    @Override
    public void onEvent(FleetEvent event) {

        System.out.println("=== FLEET EVENT ===");

        System.out.println(
                "Type: " + event.type()
        );

        System.out.println(
                "Vehicle ID: " + event.vehicleId()
        );

        System.out.println(
                "Data: " + event.data()
        );

        System.out.println("===================");
    }
}