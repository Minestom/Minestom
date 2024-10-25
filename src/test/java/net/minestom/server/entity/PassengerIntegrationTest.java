package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.SetPassengersPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
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

    @Test
    public void passengerPacketOrder(Env env) {
        var instance = env.createFlatInstance();
        var vehicle = new Entity(EntityType.ZOMBIE);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        // Add 3 passengers to vehicle to test Entity#updateNewViewer recursion
        var passenger1 = new Entity(EntityType.ZOMBIE);
        var passenger2 = new Entity(EntityType.ZOMBIE);
        var passenger3 = new Entity(EntityType.ZOMBIE);
        vehicle.addPassenger(passenger1);
        passenger1.addPassenger(passenger2);
        passenger2.addPassenger(passenger3);

        var connection = env.createConnection();
        var spawnTracker = connection.trackIncoming(SpawnEntityPacket.class);
        var passengerTracker = connection.trackIncoming(SetPassengersPacket.class);

        connection.connect(instance, new Pos(0, 40, 0));

        int startingId = passenger3.getEntityId();
        passengerTracker.assertCount(3);
        var passengerPackets = passengerTracker.collect();
        for (int i = 0; i < passengerPackets.size(); i++) {
            // Passenger packet order will be sent backwards down the chain of passenger vehicles
            assertEquals(startingId - i, passengerPackets.get(i).passengersId().get(0));
        }

        // Ensure spawn packets are never sent more than once per entity
        startingId = vehicle.getEntityId();
        spawnTracker.assertCount(4);
        var spawnPackets = spawnTracker.collect();
        for (int i = 0; i < spawnPackets.size(); i++) {
            // If the passenger spawn packets are sent in order we know that
            // Entity#updateNewViewer ran as it should
            assertEquals(startingId + i, spawnPackets.get(i).entityId());
        }
    }
}
