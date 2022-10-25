package net.minestom.server.instance.batch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * A Batch used when all of the block changed are contained inside a single chunk.
 * If more than one chunk is needed, use an {@link AbsoluteBlockBatch} instead.
 * <p>
 * The batch can be placed in any chunk in any instance, however it will always remain
 * aligned to a chunk border. If completely translatable block changes are needed, use a
 * {@link RelativeBlockBatch} instead.
 * <p>
 * Coordinates are relative to the chunk (0-15) instead of world coordinates.
 *
 * @see Batch
 */
public class ChunkBatch implements Batch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBatch.class);

    private final Int2ObjectMap<SectionBatch> sections = new Int2ObjectOpenHashMap<>();

    public ChunkBatch() {
    }

    private SectionBatch get(int index) {
        return sections.computeIfAbsent(index, i -> new SectionBatch());
    }

    @Override
    public void clear() {
        synchronized (sections) {
            sections.clear();
        }
    }

    @Override
    public CompletableFuture<Void> apply(@NotNull Instance instance) {
        return apply(instance, 0, 0);
    }

    public CompletableFuture<Void> apply(@NotNull Instance instance, int chunkX, int chunkZ) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        synchronized (sections) {
            for (var entry : sections.int2ObjectEntrySet()) {
                int sectionY = entry.getIntKey();
                SectionBatch sectionBatch = entry.getValue();
                futures.add(sectionBatch.apply(instance, chunkX, sectionY, chunkZ));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        int sectionY = ChunkUtils.getChunkCoordinate(y);
        SectionBatch sectionBatch = get(sectionY);
        sectionBatch.setBlock(x, y, z, block);
    }
}