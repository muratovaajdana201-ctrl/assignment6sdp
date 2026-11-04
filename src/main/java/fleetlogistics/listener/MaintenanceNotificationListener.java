
package fleetlogistics.listener;

import fleetlogistics.observer.FleetEvent;
import fleetlogistics.observer.Observer;

public class MaintenanceNotificationListener
        implements Observer<FleetEvent> {

    @Override
    public void onEvent(FleetEvent event) {
        System.out.println(
                "MAINTENANCE: Vehicle "
                        + event.vehicleId()
                        + " requires inspection"
        );
    }
}
