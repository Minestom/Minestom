package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkViewerIntegrationTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void basicJoin(boolean sharedInstance, Env env) {
        Instance instance = env.createFlatInstance();
        if (sharedInstance) {
            // Chunks get their viewers from the instance
            // Ensuring that the system works with shared instances is therefore important
            var manager = env.process().instance();
            instance = manager.createSharedInstance((InstanceContainer) instance);
        }

        var chunk = instance.loadChunk(0, 0).join();
        assertEquals(0, chunk.getViewers().size());

        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        assertEquals(1, chunk.getViewers().size());
        assertEquals(player, chunk.getViewers().iterator().next());
    }

    @Test
    public void renderDistance(Env env) {
        final int viewRadius = MinecraftServer.getChunkViewDistance();
        final int count = ChunkUtils.getChunkCount(viewRadius);
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        // Check initial load
        {
            var tracker = connection.trackIncoming(ChunkDataPacket.class);
            var player = connection.connect(instance, new Pos(0, 40, 0)).join();
            assertEquals(instance, player.getInstance());
            assertEquals(new Pos(0, 40, 0), player.getPosition());
            assertEquals(count, tracker.collect().size());
        }
        // Check chunk#sendChunk
        {
            var tracker = connection.trackIncoming(ChunkDataPacket.class);
            for (int x = -viewRadius; x <= viewRadius; x++) {
                for (int z = -viewRadius; z <= viewRadius; z++) {
                    instance.getChunk(x, z).sendChunk();
                }
            }
            assertEquals(count, tracker.collect().size());
        }
    }
}
