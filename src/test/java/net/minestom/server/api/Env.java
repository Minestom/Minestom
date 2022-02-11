package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;

public interface Env {
    @NotNull ServerProcess process();

    @NotNull TestConnection createConnection();

    <E extends Event, H> @NotNull Collector<E> trackEvent(@NotNull Class<E> eventType, @NotNull EventFilter<? super E, H> filter, @NotNull H actor);

    <E extends Event> @NotNull FlexibleListener<E> listen(@NotNull Class<E> eventType);

    default void tick() {
        process().ticker().tick(System.nanoTime());
    }

    default boolean tickWhile(BooleanSupplier condition, Duration timeout) {
        var ticker = process().ticker();
        final long start = System.nanoTime();
        while (condition.getAsBoolean()) {
            final long tick = System.nanoTime();
            ticker.tick(tick);
            if (timeout != null && System.nanoTime() - start > timeout.toNanos()) {
                return false;
            }
        }
        return true;
    }

    default @NotNull Player createPlayer(@NotNull Instance instance, @NotNull Pos pos) {
        return createConnection().connect(instance, pos).join();
    }

    default @NotNull Instance createFlatInstance() {
        var instance = process().instance().createInstanceContainer();
        instance.setChunkGenerator(new ChunkGenerator() {
            @Override
            public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
                for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++)
                    for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        for (byte y = 0; y < 40; y++) {
                            batch.setBlock(x, y, z, Block.STONE);
                        }
                    }
            }

            @Override
            public List<ChunkPopulator> getPopulators() {
                return null;
            }
        });
        return instance;
    }
}
