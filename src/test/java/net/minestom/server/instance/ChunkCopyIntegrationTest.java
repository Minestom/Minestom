package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkCopyIntegrationTest {
    @Test
    public void copyViewer(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        env.createPlayer(instance, new Pos(0, 40, 0));

        var chunk = instance.getChunk(0, 0);
        assert chunk != null;

        assertEquals(1, chunk.getViewers().size());

        var copy = chunk.copy(instance, 9999, 9999);
        assertEquals(0, copy.getViewers().size());
    }
}
