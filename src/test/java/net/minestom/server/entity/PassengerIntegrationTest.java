package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PassengerIntegrationTest {

    @Test
    public void passenger(Env env) {
        var instance = env.createFlatInstance();
        var vehicle = new Entity(EntityType.ZOMBIE);
        var passenger = new Entity(EntityType.ZOMBIE);

        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        passenger.setInstance(instance, new Pos(0, 40, 0)).join();

        assertEquals(0, vehicle.getPassengers().size());
        assertNull(passenger.getVehicle());

        vehicle.addPassenger(passenger);
        assertEquals(1, vehicle.getPassengers().size());
        assertEquals(vehicle, passenger.getVehicle());
    }

    @Test
    public void passengerTeleport(Env env) {
        var instance = env.createFlatInstance();
        var vehicle = new Entity(EntityType.ZOMBIE);
        var passenger = new Entity(EntityType.ZOMBIE);

        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        passenger.setInstance(instance, new Pos(0, 40, 5000)).join();

        assertEquals(0, vehicle.getPassengers().size());
        assertNull(passenger.getVehicle());

        vehicle.addPassenger(passenger);
        assertEquals(1, vehicle.getPassengers().size());
        assertEquals(vehicle, passenger.getVehicle());

        assertTrue(passenger.getDistance(vehicle) < 2);
    }

}
