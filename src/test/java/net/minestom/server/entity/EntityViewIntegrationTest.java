package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.SpawnLivingEntityPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityViewIntegrationTest {

    @Test
    public void emptyEntity(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 42)).join();
        assertEquals(0, entity.getViewers().size());
    }

    @Test
    public void emptyPlayer(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 42, 0));
        assertEquals(0, player.getViewers().size());
    }

    @Test
    public void multiPlayers(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 42));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 42));

        assertEquals(1, p1.getViewers().size());
        p1.getViewers().forEach(p -> assertEquals(p2, p));

        assertEquals(1, p2.getViewers().size());
        p2.getViewers().forEach(p -> assertEquals(p1, p));

        p2.remove();
        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        var p3 = env.createPlayer(instance, new Pos(0, 42, 42));
        assertEquals(1, p1.getViewers().size());
        p1.getViewers().forEach(p -> assertEquals(p3, p));
    }

    @Test
    public void manualViewers(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 5_000));

        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
        p1.addViewer(p2);
        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        p2.teleport(new Pos(0, 42, 0)).join();
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void movements(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        var p2 = env.createPlayer(instance, new Pos(0, 42, 96));

        assertEquals(0, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());

        p2.teleport(new Pos(0, 42, 95)).join(); // Teleport in range (6 chunks)
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void autoViewable(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        assertTrue(p1.isAutoViewable());
        p1.setAutoViewable(false);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.setAutoViewable(true);
        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewableRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewableRule(player -> player.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewableRule(player -> false);

        assertEquals(0, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());
    }

    @Test
    public void viewerRule(Env env) {
        var instance = env.createFlatInstance();
        var p1 = env.createPlayer(instance, new Pos(0, 42, 0));
        p1.updateViewerRule(player -> player.getEntityId() == p1.getEntityId() + 1);

        var p2 = env.createPlayer(instance, new Pos(0, 42, 0));

        assertEquals(1, p1.getViewers().size());
        assertEquals(1, p2.getViewers().size());

        p1.updateViewerRule(player -> false);

        assertEquals(1, p1.getViewers().size());
        assertEquals(0, p2.getViewers().size());
    }

    @Test
    public void vehicle(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0)).join();

        var vehicle = new Entity(EntityType.ZOMBIE);
        var passenger = new Entity(EntityType.ZOMBIE);

        var tracker = connection.trackIncoming(SpawnLivingEntityPacket.class);

        vehicle.setInstance(instance, new Pos(0, 40, 0)).join();
        vehicle.addPassenger(passenger);
        // Verify packets
        {
            var results = tracker.collect();
            assertEquals(2, results.size());
            assertEquals(vehicle.getEntityId(), results.get(0).entityId());
            assertEquals(passenger.getEntityId(), results.get(1).entityId());
        }
        // Verify viewers
        {
            assertEquals(0, player.getViewers().size());
            assertEquals(1, vehicle.getViewers().size());
            assertTrue(vehicle.isViewer(player));
            assertEquals(1, passenger.getViewers().size());
            assertTrue(passenger.isViewer(player));
        }
    }
}
