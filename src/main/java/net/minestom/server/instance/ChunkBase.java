package net.minestom.server.instance;

import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.instance.block.Block;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

// TODO light data & API

/**
 * A chunk is a part of an {@link Instance}, limited by a size of 16x256x16 blocks and subdivided in 16 sections of 16 blocks height.
 * Should contains all the blocks located at those positions and manage their tick updates.
 * Be aware that implementations do not need to be thread-safe, all chunks are guarded by their own chunk ('this').
 * <p>
 * You generally want to avoid storing references of this object as this could lead to a huge memory leak,
 * you should store the chunk coordinates instead.
 */
public abstract class ChunkBase implements Chunk, Block.Getter, Block.Setter, Biome.Getter, Biome.Setter, Viewable, Tickable, Taggable, Snapshotable {

    private final UUID identifier;

    protected Instance instance;
    protected final int chunkX, chunkZ;
    protected final int minSection, maxSection;

    // Options
    private final boolean shouldGenerate;
    private boolean readOnly;

    protected volatile boolean loaded = true;
    private final ChunkView viewers;

    // Path finding
    protected PFColumnarSpace columnarSpace;

    // Data
    private final TagHandler tagHandler = TagHandler.newHandler();

    public ChunkBase(@NotNull Instance instance, int chunkX, int chunkZ, boolean shouldGenerate) {
        this.identifier = UUID.randomUUID();
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.shouldGenerate = shouldGenerate;
        this.minSection = instance.getDimensionType().getMinY() / Section.SIZE_Y;
        this.maxSection = (instance.getDimensionType().getMinY() + instance.getDimensionType().getHeight()) / Section.SIZE_Y;
        this.viewers = new ChunkView(instance, toPosition());
    }

    /**
     * Gets the unique identifier of this chunk.
     * <p>
     * WARNING: this UUID is not persistent but randomized once the object is instantiated.
     *
     * @return the chunk identifier
     */
    public @NotNull UUID getIdentifier() {
        return identifier;
    }

    /**
     * Gets the chunk where this chunk is stored
     *
     * @return the linked chunk
     */
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Gets the lowest (inclusive) section Y available in this chunk
     *
     * @return the lowest (inclusive) section Y available in this chunk
     */
    public int getMinSection() {
        return minSection;
    }

    /**
     * Gets the highest (exclusive) section Y available in this chunk
     *
     * @return the highest (exclusive) section Y available in this chunk
     */
    public int getMaxSection() {
        return maxSection;
    }

    /**
     * Gets the world position of this chunk.
     *
     * @return the position of this chunk
     */
    public @NotNull Point toPosition() {
        return new Vec(SIZE_X * getChunkX(), 0, SIZE_Z * getChunkZ());
    }

    /**
     * Gets if this chunk will or had been loaded with a {@link ChunkGenerator}.
     * <p>
     * If false, the chunk will be entirely empty when loaded.
     *
     * @return true if this chunk is affected by a {@link ChunkGenerator}
     */
    public boolean shouldGenerate() {
        return shouldGenerate;
    }

    /**
     * Gets if this chunk is read-only.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link ChunkGenerator}.
     *
     * @return true if the chunk is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Changes the read state of the chunk.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link ChunkGenerator}.
     *
     * @param readOnly true to make the chunk read-only, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Changes this chunk columnar space.
     *
     * @param columnarSpace the new columnar space
     */
    public void setColumnarSpace(PFColumnarSpace columnarSpace) {
        this.columnarSpace = columnarSpace;
    }

    /**
     * Used to verify if the chunk should still be kept in memory.
     *
     * @return true if the chunk is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + chunkX + ":" + chunkZ + "]";
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        throw new UnsupportedOperationException("Chunk does not support manual viewers");
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        throw new UnsupportedOperationException("Chunk does not support manual viewers");
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return viewers.set;
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }
}