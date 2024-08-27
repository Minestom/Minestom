package net.minestom.server.instance.chunksystem;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
class ChunkManagerTest {
    @Test
    void testHigherRadiusSamePriority(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        for (var i = 0; i < 100; i++) {
            manager.addClaim(150, i, 0, 10, ChunkClaim.Shape.SQUARE);
        }
        manager.addClaim(140, 50, 30, 20, ChunkClaim.Shape.SQUARE);
        assertEquals(3760, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape1(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.DIAMOND);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.SQUARE);

        assertEquals(1681, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape1R(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.SQUARE);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.DIAMOND);

        assertEquals(1681, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape2(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.CIRCLE);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.SQUARE);

        assertEquals(1681, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape2R(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.SQUARE);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.CIRCLE);

        assertEquals(1681, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape3(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.DIAMOND);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.CIRCLE);

        assertEquals(1257, manager.getLoadedChunks().size());
    }

    @Test
    void testSameClaimDifferentShape3R(Env env) {
        var instance = env.createFlatInstance();
        var manager = instance.getChunkManager();
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.CIRCLE);
        manager.addClaim(10, 10, 20, 20, ChunkClaim.Shape.DIAMOND);

        assertEquals(1257, manager.getLoadedChunks().size());
    }
}
