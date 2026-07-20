package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class GeneratorIntegrationTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void loader(boolean data, Env env) {
        var manager = env.process().instance();
        var block = data ? Block.STONE.withNbt(CompoundBinaryTag.builder().putString("key", "value").build()) : Block.STONE;
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fill(block));
        instance.loadChunk(0, 0).join();
        assertEquals(block, instance.getBlock(0, 0, 0));
        assertEquals(block, instance.getBlock(15, 0, 0));
        assertEquals(block, instance.getBlock(0, 15, 0));
        assertEquals(block, instance.getBlock(0, 0, 15));
    }

    @Test
    public void exceptionCatch(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        var ref = new AtomicReference<Throwable>();
        env.process().exception().setExceptionHandler(ref::set);

        var exception = new RuntimeException();
        instance.setGenerator(unit -> {
            unit.modifier().fill(Block.STONE);
            throw exception;
        });
        instance.loadChunk(0, 0).join();

        assertSame(exception, ref.get());
    }

    @Test
    public void fillHeightNegative(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(-64, -60, Block.STONE));
        instance.loadChunk(0, 0).join();
        for (int y = -64; y < -60; y++) {
            assertEquals(Block.STONE, instance.getBlock(0, y, 0), "y=" + y);
        }
        for (int y = -60; y < 100; y++) {
            assertEquals(Block.AIR, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightSingleSectionFull(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 16, Block.GRASS_BLOCK));
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 16; y++) {
            assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightSingleSection(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(4, 5, Block.GRASS_BLOCK));
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 5; y++) {
            assertEquals(y == 4 ? Block.GRASS_BLOCK : Block.AIR, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightOverride(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 39, Block.GRASS_BLOCK);
            unit.modifier().fillHeight(39, 40, Block.STONE);
        });
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 40; y++) {
            assertEquals(y == 39 ? Block.STONE : Block.GRASS_BLOCK, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void explicitChunkGenerate(Env env) {
        var instance = env.createEmptyInstance();
        Generator generator = unit -> {
            assertTrue(Thread.currentThread().isVirtual());
            unit.modifier().fill(Block.GRASS_BLOCK);
        };
        instance.generateChunk(0, 0, generator).join();
        assertNotNull(instance.getChunk(0, 0));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, 0, 0));
    }

    @Test
    public void explicitChunkGenerateOverride(Env env) {
        var instance = env.createEmptyInstance();
        instance.setGenerator(unit -> unit.modifier().fill(Block.STONE));
        Generator generator = unit -> unit.modifier().fill(Block.GRASS_BLOCK);
        instance.generateChunk(0, 0, generator).join();
        assertNotNull(instance.getChunk(0, 0));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, 0, 0));
    }

    @Test
    public void explicitChunkGenerateLock(Env env) {
        var instance = env.createEmptyInstance();
        DynamicChunk chunk = (DynamicChunk) instance.loadChunk(0, 0).join();
        Generator generator = unit -> {
            chunk.assertWriteLock();
            unit.modifier().fill(Block.GRASS_BLOCK);
        };
        instance.generateChunk(0, 0, generator).join();
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, 0, 0));
    }

    @Test
    public void chunkReadLockCannotUpgradeToWriteLock(Env env) {
        assumeTrue(Chunk.class.desiredAssertionStatus(), "Chunk lock contract checks require assertions");
        var instance = env.createEmptyInstance();
        var chunk = instance.loadChunk(0, 0).join();
        chunk.lockReadLock();
        try {
            assertThrows(AssertionError.class, chunk::lockWriteLock);
        } finally {
            chunk.unlockReadLock();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void loaderExceptionCompletesChunkFuture(boolean parallel, Env env) {
        var exception = new RuntimeException("loader failure");
        env.process().exception().setExceptionHandler(_ -> {
        });
        ChunkLoader chunkLoader = new ChunkLoader() {
            @Override
            public Chunk loadChunk(Instance instance, int chunkX, int chunkZ) {
                throw exception;
            }

            @Override
            public void saveChunk(Chunk chunk) {
            }

            @Override
            public boolean supportsParallelLoading() {
                return parallel;
            }
        };
        var instance = env.createEmptyInstance(chunkLoader);

        var thrown = assertThrows(CompletionException.class, () -> instance.loadChunk(0, 0).join());
        assertSame(exception, thrown.getCause());
        assertNull(instance.getChunk(0, 0));
    }

    @Test
    public void concurrentChunkLoadsComplete(Env env) {
        var instance = env.createEmptyInstance();
        CompletableFuture<?>[] futures = new CompletableFuture<?>[64];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = instance.loadChunk(i & 7, i >> 3);
        }

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> CompletableFuture.allOf(futures).join());
        assertEquals(futures.length, instance.getChunks().size());
    }

    @Test
    public void explicitChunkGeneratePacket(Env env) {
        var instance = env.createEmptyInstance();
        var connection = env.createConnection();
        connection.connect(instance, Pos.ZERO);
        Generator generator = unit -> unit.modifier().fill(Block.GRASS_BLOCK);
        var tracker = connection.trackIncoming(ChunkDataPacket.class);
        instance.generateChunk(0, 0, generator).join();
        tracker.assertAny();
    }
}
