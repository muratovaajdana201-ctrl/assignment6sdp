
package fleetlogistics.listener;

import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;

public class GeofenceAlertListener implements Observer<FleetEvent> {

    @Override
    public void onEvent(FleetEvent event) {
        System.out.println(
                "GEOFENCE ALERT: Vehicle " + event.vehicleId()
                        + " " + event.data().get("action")
                        + " zone " + event.data().get("zone")
        );
    }
}
