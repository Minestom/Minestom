package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO light data & API

/**
 * A chunk is a part of an {@link Instance}, limited by a size of 16x256x16 blocks and subdivided in 16 sections of 16 blocks height.
 * Should contain all the blocks located at those positions and manage their tick updates.
 * Be aware that implementations do not need to be thread-safe, all chunks are guarded by their own instance ('this').
 * <p>
 * You can create your own implementation of this class by extending it
 * and create the objects in {@link InstanceContainer#setChunkSupplier(ChunkSupplier)}.
 * <p>
 * You generally want to avoid storing references of this object as this could lead to a huge memory leak,
 * you should store the chunk coordinates instead.
 */
public abstract class Chunk implements Block.Getter, Block.Setter, Biome.Getter, Biome.Setter, Viewable, Tickable, Taggable, Snapshotable {
    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    private final UUID identifier;

    protected Instance instance;
    protected final int chunkX, chunkZ;
    protected final int minSection, maxSection;

    // Options
    private final boolean shouldGenerate;
    private boolean readOnly;

    protected volatile boolean loaded = true;
    private final Viewable viewable;

    // Data
    private final TagHandler tagHandler = TagHandler.newHandler();

    public Chunk(@NotNull Instance instance, int chunkX, int chunkZ, boolean shouldGenerate) {
        this.identifier = UUID.randomUUID();
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.shouldGenerate = shouldGenerate;
        final DimensionType instanceDim = instance.getCachedDimensionType();
        this.minSection = instanceDim.minY() / CHUNK_SECTION_SIZE;
        this.maxSection = (instanceDim.minY() + instanceDim.height()) / CHUNK_SECTION_SIZE;
        final List<SharedInstance> shared = instance instanceof InstanceContainer instanceContainer ?
                instanceContainer.getSharedInstances() : List.of();
        this.viewable = instance.getEntityTracker().viewable(shared, chunkX, chunkZ);
    }

    /**
     * Sets a block at a position.
     * <p>
     * This is used when the previous block has to be destroyed/replaced, meaning that it clears the previous data and update method.
     * <p>
     * WARNING: this method is not thread-safe (in order to bring performance improvement with {@link net.minestom.server.instance.batch.Batch batches})
     * The thread-safe version is {@link Instance#setBlock(int, int, int, Block)} (or any similar instance methods)
     * Otherwise, you can simply do not forget to have this chunk synchronized when this is called.
     *
     * @param x     the block X
     * @param y     the block Y
     * @param z     the block Z
     * @param block the block to place
     */
    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block, null, null);
    }

    protected abstract void setBlock(int x, int y, int z, @NotNull Block block,
                                     @Nullable BlockHandler.Placement placement,
                                     @Nullable BlockHandler.Destroy destroy);

    public abstract @NotNull List<Section> getSections();

    public abstract @NotNull Section getSection(int section);

    public abstract @NotNull Heightmap motionBlockingHeightmap();
    public abstract @NotNull Heightmap worldSurfaceHeightmap();
    public abstract void loadHeightmapsFromNBT(CompoundBinaryTag heightmaps);

    public @NotNull Section getSectionAt(int blockY) {
        return getSection(CoordConversion.globalToChunk(blockY));
    }

    /**
     * Executes a chunk tick.
     * <p>
     * Should be used to update all the blocks in the chunk.
     * <p>
     * WARNING: this method doesn't necessary have to be thread-safe, proceed with caution.
     *
     * @param time the time of the update in milliseconds
     */
    @Override
    public abstract void tick(long time);

    /**
     * Gets the last time that this chunk changed.
     * <p>
     * "Change" means here data used in {@link ChunkDataPacket}.
     * It is necessary to see if the cached version of this chunk can be used
     * instead of re-writing and compressing everything.
     *
     * @return the last change time in milliseconds
     */
    public abstract long getLastChangeTime();

    /**
     * Sends the chunk data to {@code player}.
     *
     * @param player the player
     */
    public void sendChunk(@NotNull Player player) {
        player.sendChunk(this);
    }

    public void sendChunk() {
        getViewers().forEach(this::sendChunk);
    }

    @ApiStatus.Internal
    public abstract @NotNull SendablePacket getFullDataPacket();

    /**
     * Creates a copy of this chunk, including blocks state id, custom block id, biomes, update data.
     * <p>
     * The chunk position (X/Z) can be modified using the given arguments.
     *
     * @param instance the chunk owner
     * @param chunkX   the chunk X of the copy
     * @param chunkZ   the chunk Z of the copy
     * @return a copy of this chunk with a potentially new instance and position
     */
    public abstract @NotNull Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ);

    /**
     * Resets the chunk, this means clearing all the data making it empty.
     */
    public abstract void reset();

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
     * Gets the instance where this chunk is stored
     *
     * @return the linked instance
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
        return new Vec(CHUNK_SIZE_X * getChunkX(), 0, CHUNK_SIZE_Z * getChunkZ());
    }

    /**
     * Gets if this chunk will or had been loaded with a {@link Generator}.
     * <p>
     * If false, the chunk will be entirely empty when loaded.
     *
     * @return true if this chunk is affected by a {@link Generator}
     */
    public boolean shouldGenerate() {
        return shouldGenerate;
    }

    /**
     * Gets if this chunk is read-only.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link Generator}.
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
     * It does not affect {@link IChunkLoader} and {@link Generator}.
     *
     * @param readOnly true to make the chunk read-only, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Used to verify if the chunk should still be kept in memory.
     *
     * @return true if the chunk is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Called when the chunk has been successfully loaded.
     */
    protected void onLoad() {}

    /**
     * Called when the chunk generator has finished generating the chunk.
     */
    public void onGenerate() {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + chunkX + ":" + chunkZ + "]";
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        return viewable.addViewer(player);
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        return viewable.removeViewer(player);
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return viewable.getViewers();
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    /**
     * Sets the chunk as "unloaded".
     */
    protected void unload() {
        this.loaded = false;
    }

    /**
     * Invalidate the chunk caches
     */
    public abstract void invalidate();
}