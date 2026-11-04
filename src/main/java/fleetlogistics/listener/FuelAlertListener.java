
package fleetlogistics.listener;

import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;

public class FuelAlertListener implements Observer<FleetEvent> {

    @Override
    public void onEvent(FleetEvent event) {
        System.out.println(
                "FUEL WARNING: Vehicle " + event.vehicleId()
                        + " has only "
                        + event.data().get("fuelPercentage") + "% fuel"
        );
    }
}
