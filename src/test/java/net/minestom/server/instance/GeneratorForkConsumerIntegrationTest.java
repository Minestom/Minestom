package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class GeneratorForkConsumerIntegrationTest {

    @Test
    public void consumerLocal(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        instance.setGenerator(unit -> {
            unit.fork(setter -> {
                var dynamic = (GeneratorImpl.DynamicFork) setter;
                assertNull(dynamic.minSection);
                assertEquals(0, dynamic.width);
                assertEquals(0, dynamic.height);
                assertEquals(0, dynamic.depth);
                setter.setBlock(unit.absoluteStart(), Block.STONE);
                assertEquals(unit.absoluteStart(), dynamic.minSection);
                assertEquals(1, dynamic.width);
                assertEquals(1, dynamic.height);
                assertEquals(1, dynamic.depth);
            });
        });
        instance.loadChunk(0, 0).join();
        assertEquals(Block.STONE, instance.getBlock(0, -64, 0));
    }

    @Test
    public void consumerNeighborZ(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        instance.setGenerator(unit -> {
            unit.fork(setter -> {
                var dynamic = (GeneratorImpl.DynamicFork) setter;
                assertNull(dynamic.minSection);
                assertEquals(0, dynamic.width);
                assertEquals(0, dynamic.height);
                assertEquals(0, dynamic.depth);
                setter.setBlock(unit.absoluteStart(), Block.STONE);
                setter.setBlock(unit.absoluteStart().add(0, 0, 16), Block.GRASS);
                assertEquals(unit.absoluteStart(), dynamic.minSection);
                assertEquals(1, dynamic.width);
                assertEquals(1, dynamic.height);
                assertEquals(2, dynamic.depth);
            });
        });
        instance.loadChunk(0, 0).join();
        instance.setGenerator(null);
        instance.loadChunk(0, 1).join();
        assertEquals(Block.STONE, instance.getBlock(0, -64, 0));
        assertEquals(Block.GRASS, instance.getBlock(0, -64, 16));
    }

    @Test
    public void consumerNeighborX(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        instance.setGenerator(unit -> {
            unit.fork(setter -> {
                var dynamic = (GeneratorImpl.DynamicFork) setter;
                assertNull(dynamic.minSection);
                assertEquals(0, dynamic.width);
                assertEquals(0, dynamic.height);
                assertEquals(0, dynamic.depth);
                setter.setBlock(unit.absoluteStart(), Block.STONE);
                setter.setBlock(unit.absoluteStart().add(16, 0, 0), Block.GRASS);
                assertEquals(unit.absoluteStart(), dynamic.minSection);
                assertEquals(2, dynamic.width);
                assertEquals(1, dynamic.height);
                assertEquals(1, dynamic.depth);
            });
        });
        instance.loadChunk(0, 0).join();
        instance.setGenerator(null);
        instance.loadChunk(1, 0).join();
        assertEquals(Block.STONE, instance.getBlock(0, -64, 0));
        assertEquals(Block.GRASS, instance.getBlock(16, -64, 0));
    }

    @Test
    public void consumerNeighborY(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        instance.setGenerator(unit -> {
            unit.fork(setter -> {
                var dynamic = (GeneratorImpl.DynamicFork) setter;
                assertNull(dynamic.minSection);
                assertEquals(0, dynamic.width);
                assertEquals(0, dynamic.height);
                assertEquals(0, dynamic.depth);
                setter.setBlock(unit.absoluteStart(), Block.STONE);
                setter.setBlock(unit.absoluteStart().add(0, 16, 0), Block.GRASS);
                assertEquals(unit.absoluteStart(), dynamic.minSection);
                assertEquals(1, dynamic.width);
                assertEquals(2, dynamic.height);
                assertEquals(1, dynamic.depth);
            });
        });
        instance.loadChunk(0, 0).join();
        assertEquals(Block.STONE, instance.getBlock(0, -64, 0));
        assertEquals(Block.GRASS, instance.getBlock(0, -48, 0));
    }
}
