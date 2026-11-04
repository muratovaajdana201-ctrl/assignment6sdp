
package fleetlogistics.listener;

import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;

public class GpsMapListener implements Observer<FleetEvent> {

    @Override
    public void onEvent(FleetEvent event) {
        System.out.println(
                "MAP: Vehicle " + event.vehicleId()
                        + " location updated: " + event.data()
        );
    }
}
