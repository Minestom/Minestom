package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.world.biomes.Biome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class GeneratorForkIntegrationTest {

    @Test
    public void local(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        var block = Block.STONE;
        instance.setGenerator(unit -> {
            var u = unit.fork(unit.absoluteStart(), unit.absoluteEnd());
            assertEquals(unit.absoluteStart(), u.absoluteStart());
            assertEquals(unit.absoluteEnd(), u.absoluteEnd());
            u.modifier().setRelative(0, 0, 0, Block.STONE);
        });
        instance.loadChunk(0, 0).join();
        assertEquals(block, instance.getBlock(0, -64, 0));
    }

    @Test
    public void size(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        // Set the Generator
        instance.setGenerator(unit -> {
            Point start = unit.absoluteStart();
            GenerationUnit fork = unit.fork(start, start.add(18, 18, 18));
            assertDoesNotThrow(() -> fork.modifier().setBlock(start.add(17, 17, 17), Block.STONE));
        });
        // Load the chunks
        instance.loadChunk(0, 0).join();
        instance.setGenerator(null);
        instance.loadChunk(1, 1).join();
        assertEquals(Block.STONE, instance.getBlock(17, -64 + 17, 17));
    }

    @Test
    public void signal(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        var block = Block.STONE;
        instance.setGenerator(unit -> {
            var u = unit.fork(unit.absoluteStart(), unit.absoluteEnd().add(16, 0, 16));
            assertEquals(unit.absoluteStart(), u.absoluteStart());
            assertEquals(unit.absoluteEnd().add(16, 0, 16), u.absoluteEnd());
            u.modifier().setRelative(16, 0, 0, Block.STONE);
            u.modifier().setRelative(16, 33, 0, Block.STONE);
        });
        instance.loadChunk(0, 0).join();
        instance.setGenerator(null);
        instance.loadChunk(1, 0).join();
        assertEquals(block, instance.getBlock(16, -64, 0));
        assertEquals(block, instance.getBlock(16, -31, 0));
    }

    @Test
    public void air(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> {
            var u = unit.fork(unit.absoluteStart(), unit.absoluteEnd().add(16, 0, 16));
            u.modifier().setRelative(16, 39 + 64, 0, Block.AIR);
        });
        instance.loadChunk(0, 0).join();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        instance.loadChunk(1, 0).join();
        assertEquals(Block.AIR, instance.getBlock(16, 39, 0));
    }

    @Test
    public void fillHeight(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> {
            var u = unit.fork(unit.absoluteStart(), unit.absoluteEnd().add(16, 0, 16));
            u.modifier().fillHeight(0, 40, Block.STONE);
        });
        instance.loadChunk(0, 0).join();
        instance.setGenerator(null);
        instance.loadChunk(1, 0).join();
        for (int y = 0; y < 40; y++) {
            assertEquals(Block.STONE, instance.getBlock(16, y, 0), "y=" + y);
        }
    }

    @Test
    public void biome(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> {
            var u = unit.fork(unit.absoluteStart(), unit.absoluteEnd().add(16, 0, 16));
            assertThrows(IllegalStateException.class, () -> u.modifier().setBiome(16, 0, 0, Biome.PLAINS));
            assertThrows(IllegalStateException.class, () -> u.modifier().fillBiome(Biome.PLAINS));
        });
        instance.loadChunk(0, 0).join();
    }
}
