package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;
import java.util.function.Consumer;

import static net.minestom.server.coordinate.CoordConversion.SECTION_SIZE;
import static net.minestom.server.coordinate.CoordConversion.globalToChunk;

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
@NotNullByDefault
public sealed interface Chunk extends Viewable, Tickable, Taggable, Snapshotable permits ChunkImpl {
    int CHUNK_SIZE_X = SECTION_SIZE;
    int CHUNK_SIZE_Z = SECTION_SIZE;
    int CHUNK_SECTION_SIZE = SECTION_SIZE;

    static Chunk chunk(Instance instance, int chunkX, int chunkZ, Consumer<Builder> consumer) {
        var builder = new ChunkImpl.Builder();
        consumer.accept(builder);
        return builder.build(instance, chunkX, chunkZ);
    }

    static Chunk chunk(Instance instance, int chunkX, int chunkZ) {
        return new ChunkImpl.Builder().build(instance, chunkX, chunkZ);
    }

    static Chunk chunkLight(Instance instance, int chunkX, int chunkZ) {
        return chunk(instance, chunkX, chunkZ, Builder::lightEngine);
    }

    sealed interface Builder permits ChunkImpl.Builder {
        void lightEngine(boolean lightEngine);

        default void lightEngine() {
            lightEngine(true);
        }

        void readOnly(boolean readOnly);

        default void readOnly() {
            readOnly(true);
        }

        void generate(boolean generate);

        default void generate() {
            generate(true);
        }

        void sections(List<Section> sections);
    }

    List<Section> getSections();

    Section getSection(int section);

    Heightmap motionBlockingHeightmap();

    Heightmap worldSurfaceHeightmap();

    void loadHeightmapsFromNBT(CompoundBinaryTag heightmaps);

    default Section getSectionAt(int blockY) {
        return getSection(globalToChunk(blockY));
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
    void tick(long time);

    /**
     * Sends the chunk data to {@code player}.
     *
     * @param player the player
     */
    default void sendChunk(Player player) {
        player.sendChunk(this);
    }

    default void sendChunk() {
        getViewers().forEach(this::sendChunk);
    }

    @ApiStatus.Internal
    SendablePacket getFullDataPacket();

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
    Chunk copy(Instance instance, int chunkX, int chunkZ);

    /**
     * Resets the chunk, this means clearing all the data making it empty.
     */
    void reset();

    Instance getInstance();

    int getChunkX();

    int getChunkZ();

    /**
     * Gets the lowest (inclusive) section Y available in this chunk
     *
     * @return the lowest (inclusive) section Y available in this chunk
     */
    int getMinSection();

    /**
     * Gets the highest (exclusive) section Y available in this chunk
     *
     * @return the highest (exclusive) section Y available in this chunk
     */
    int getMaxSection();

    /**
     * Gets if this chunk will or had been loaded with a {@link Generator}.
     * <p>
     * If false, the chunk will be entirely empty when loaded.
     *
     * @return true if this chunk is affected by a {@link Generator}
     */
    boolean shouldGenerate();

    /**
     * Gets the world position of this chunk.
     *
     * @return the position of this chunk
     */
    default Point toPosition() {
        return new BlockVec(CHUNK_SIZE_X * getChunkX(), 0, CHUNK_SIZE_Z * getChunkZ());
    }

    /**
     * Gets if this chunk is read-only.
     * <p>
     * Being read-only should prevent block placing/breaking and setting block from an {@link Instance}.
     * It does not affect {@link IChunkLoader} and {@link Generator}.
     *
     * @return true if the chunk is read-only
     */
    boolean isReadOnly();

    /**
     * Gets if this chunk has a light engine.
     * <p>
     * If true, the chunk will be able to calculate light updates and send them to players.
     *
     * @return true if the chunk has a light engine
     */
    boolean hasLightEngine();

    /**
     * Used to verify if the chunk should still be kept in memory.
     *
     * @return true if the chunk is loaded
     */
    boolean isLoaded();

    /**
     * Sets the chunk as "unloaded".
     */
    void unload();

    TagHandler tagHandler();

    /**
     * Invalidate the chunk caches
     */
    void invalidate();
}