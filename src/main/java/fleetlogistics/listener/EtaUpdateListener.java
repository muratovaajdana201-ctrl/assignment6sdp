
package fleetlogistics.listener;

import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;

public class EtaUpdateListener implements Observer<FleetEvent> {

    @Override
    public void onEvent(FleetEvent event) {
        System.out.println(
                "ETA: Vehicle " + event.vehicleId()
                        + " will arrive in "
                        + event.data().get("minutes") + " minutes"
        );
    }
}
