package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
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
    public void playerChunkRenderDistance(Env env) {
        final int viewRadius = MinecraftServer.getChunkViewDistance();
        final int viewLength = 1 + viewRadius * 2;

        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        // Check initial load
        {
            var tracker = connection.trackIncoming(ChunkDataPacket.class);

            var player = connection.connect(instance, new Pos(0, 40, 0)).join();
            assertEquals(instance, player.getInstance());
            assertEquals(new Pos(0, 40, 0), player.getPosition());

            assertEquals(viewLength * viewLength, tracker.collect().size());
        }

        // Check chunk#sendChunk
        {
            var tracker = connection.trackIncoming(ChunkDataPacket.class);

            for (int x = -viewRadius; x <= viewRadius; x++) {
                for (int z = -viewRadius; z <= viewRadius; z++) {
                    instance.getChunk(x, z).sendChunk();
                }
            }
            assertEquals(viewLength * viewLength, tracker.collect().size());
        }
    }
}
