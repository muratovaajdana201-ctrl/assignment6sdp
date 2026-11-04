
package fleetlogistics;

import fleetlogistics.model.*;
import fleetlogistics.visitor.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {

    private Fleet createFleet() {
        Fleet fleet = new Fleet("Main Fleet");
        Depot depot = new Depot("Astana Depot");

        depot.add(new Truck("TR-101", 1200, 100));
        depot.add(new Van("VN-202", 500, 40));
        depot.add(new Drone("DR-303", 900, 10));
        depot.add(new ElectricTruck("ET-404", 1500, 0));

        fleet.add(depot);
        return fleet;
    }

    @Test
    void fuelCostIsCalculatedCorrectly() {
        double result = createFleet().accept(
                new FuelCostVisitor());

        // 100 * 1.80 + 40 * 1.80 + 10 * 1.20
        assertEquals(264.0, result, 0.001);
    }

    @Test
    void co2IsCalculatedCorrectly() {
        double result = createFleet().accept(
                new Co2ReportVisitor());

        // 100 * 2.68 + 40 * 2.68 + 10 * 1.10
        assertEquals(386.2, result, 0.001);
    }

    @Test
    void statisticsCountAllVehicles() {
        Statistics result = createFleet().accept(
                new StatisticsVisitor());

        assertEquals(4, result.vehicleCount());
    }

    @Test
    void statisticsCalculateMileage() {
        Statistics result = createFleet().accept(
                new StatisticsVisitor());

        assertEquals(4100.0,
                result.totalMileage(), 0.001);
    }

    @Test
    void maintenanceReturnsCorrectVehicles() {
        List<Vehicle> result = createFleet().accept(
                new MaintenanceFilterVisitor());

        assertEquals(2, result.size());
        assertEquals("TR-101", result.get(0).getId());
        assertEquals("ET-404", result.get(1).getId());
    }

    @Test
    void electricTruckHasZeroFuelCost() {
        ElectricTruck truck =
                new ElectricTruck("ET-1", 1500, 0);

        assertEquals(0.0,
                truck.accept(new FuelCostVisitor()), 0.001);
    }

    @Test
    void electricTruckHasZeroCo2() {
        ElectricTruck truck =
                new ElectricTruck("ET-1", 1500, 0);

        assertEquals(0.0,
                truck.accept(new Co2ReportVisitor()), 0.001);
    }

    @Test
    void jsonContainsFleetAndVehicles() {
        String json = createFleet().accept(
                new JsonExportVisitor());

        assertTrue(json.contains("\"fleet\":\"Main Fleet\""));
        assertTrue(json.contains("\"depot\":\"Astana Depot\""));
        assertTrue(json.contains("\"id\":\"TR-101\""));
        assertTrue(json.contains("\"id\":\"ET-404\""));
    }

    @Test
    void jsonHasNestedStructure() {
        String json = createFleet().accept(
                new JsonExportVisitor());

        assertTrue(json.contains("\"depots\":[{"));
        assertTrue(json.contains("\"vehicles\":[{"));
        assertTrue(json.endsWith("]}]}"));
    }

    @Test
    void negativeMileageIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Truck("TR-1", -100, 10));
    }

    @Test
    void emptyVehicleIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Van("", 100, 10));
    }
}
