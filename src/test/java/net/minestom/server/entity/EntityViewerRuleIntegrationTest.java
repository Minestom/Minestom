package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityViewerRuleIntegrationTest {

    @Test
    public void viewableRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewableRule(p -> p.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewableRule(player -> false);

        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewableRuleUpdate(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));

        AtomicBoolean enabled = new AtomicBoolean(false);
        p1.updateViewableRule(p -> enabled.get());

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled.set(true);
        p1.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewableRuleDouble(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        AtomicBoolean enabled1 = new AtomicBoolean(false);
        AtomicBoolean enabled2 = new AtomicBoolean(false);

        p1.updateViewableRule(p -> enabled1.get());
        p2.updateViewableRule(p -> enabled2.get());
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled1.set(true);
        p1.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled2.set(true);
        p2.updateViewableRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled1.set(false);
        p1.updateViewableRule();
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewerRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewerRule(e -> e.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewerRule(player -> false);

        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
    }

    @Test
    public void viewerRuleUpdate(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        AtomicBoolean enabled = new AtomicBoolean(false);
        p1.updateViewerRule(e -> enabled.get());

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled.set(true);
        p1.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewerRuleDouble(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        AtomicBoolean enabled1 = new AtomicBoolean(false);
        AtomicBoolean enabled2 = new AtomicBoolean(false);

        p1.updateViewerRule(e -> enabled1.get());
        p2.updateViewerRule(e -> enabled2.get());
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        enabled1.set(true);
        p1.updateViewerRule();
        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled2.set(true);
        p2.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        enabled1.set(false);
        p1.updateViewerRule();
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
    }

    @Test
    public void passengerRespectsViewableRuleOnJoin(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var spawnTracker = connection.trackIncoming(SpawnEntityPacket.class);

        var vehicle = new Entity(EntityType.ZOMBIE);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        var passenger = new Entity(EntityType.PIG);
        passenger.updateViewableRule(p -> false);
        vehicle.addPassenger(passenger);

        var testPlayer = connection.connect(instance, new Pos(0, 40, 0));

        var spawns = spawnTracker.collect().stream()
                .filter(p -> p.entityId() != testPlayer.getEntityId())
                .toList();
        assertEquals(1, spawns.size());
        assertEquals(vehicle.getEntityId(), spawns.getFirst().entityId());
    }

    @Test
    public void passengerRespectsViewableRuleChange(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var spawnTracker = connection.trackIncoming(SpawnEntityPacket.class);

        var vehicle = new Entity(EntityType.ZOMBIE);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        var passenger = new Entity(EntityType.PIG);
        vehicle.addPassenger(passenger);

        var testPlayer = connection.connect(instance, new Pos(0, 40, 0));

        var spawns = spawnTracker.collect().stream()
                .filter(p -> p.entityId() != testPlayer.getEntityId())
                .toList();
        assertEquals(2, spawns.size());

        passenger.updateViewableRule(p -> false);

        assertTrue(vehicle.getViewers().contains(testPlayer));
        assertFalse(passenger.getViewers().contains(testPlayer));
    }


    @Test
    public void vehicleViewableRuleChange(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var spawnTracker = connection.trackIncoming(SpawnEntityPacket.class);

        var vehicle = new Entity(EntityType.ZOMBIE);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        var passenger = new Entity(EntityType.PIG);
        vehicle.addPassenger(passenger);

        var testPlayer = connection.connect(instance, new Pos(0, 40, 0));

        var spawns = spawnTracker.collect().stream()
                .filter(p -> p.entityId() != testPlayer.getEntityId())
                .toList();
        assertEquals(2, spawns.size());

        vehicle.updateViewableRule(p -> false);

        assertFalse(vehicle.getViewers().contains(testPlayer));
        assertFalse(passenger.getViewers().contains(testPlayer));
    }

    @Test
    public void manualViewerOnlySeesVehicle(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var spawnTracker1 = connection.trackIncoming(SpawnEntityPacket.class);
        var spawnTracker2 = connection.trackIncoming(SpawnEntityPacket.class);

        var vehicle = new Entity(EntityType.ZOMBIE);
        var passenger = new Entity(EntityType.PIG);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();

        vehicle.setAutoViewable(false);
        passenger.setAutoViewable(false);
        vehicle.addPassenger(passenger);

        var testPlayer = connection.connect(instance, new Pos(0, 40, 5000));
        spawnTracker1.assertCount(0);

        vehicle.addViewer(testPlayer);

        spawnTracker2.assertCount(1);
        assertTrue(vehicle.isViewer(testPlayer));
        assertFalse(passenger.isViewer(testPlayer));
    }

    @Test
    public void manualViewerRespectsPassengerRule(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var spawnTracker1 = connection.trackIncoming(SpawnEntityPacket.class);
        var spawnTracker2 = connection.trackIncoming(SpawnEntityPacket.class);

        var vehicle = new Entity(EntityType.ZOMBIE);
        var passenger = new Entity(EntityType.PIG);
        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();

        vehicle.setAutoViewable(false);
        passenger.updateViewableRule(p -> false);
        vehicle.addPassenger(passenger);

        var testPlayer = connection.connect(instance, new Pos(0, 40, 5000));
        spawnTracker1.assertCount(0);

        vehicle.addViewer(testPlayer);

        spawnTracker2.assertCount(1);
        assertEquals(vehicle.getEntityId(), spawnTracker2.collect().getFirst().entityId());
        assertTrue(vehicle.isViewer(testPlayer));
        assertFalse(passenger.isViewer(testPlayer));
    }
}