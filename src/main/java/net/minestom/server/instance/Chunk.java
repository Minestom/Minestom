package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.instance.block.BlockSetter;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO light data & API

/**
 * A chunk is a part of an {@link Instance}, limited by a size of 16x256x16 blocks and subdivided in 16 sections of 16 blocks height.
 * Should contains all the blocks located at those positions and manage their tick updates.
 * Be aware that implementations do not need to be thread-safe, all chunks are guarded by their own instance ('this').
 * <p>
 * Chunks can be serialized using {@link #getSerializedData()} and deserialized back with {@link #readChunk(BinaryReader, ChunkCallback)},
 * allowing you to implement your own storage solution if needed.
 * <p>
 * You can create your own implementation of this class by extending it
 * and create the objects in {@link InstanceContainer#setChunkSupplier(ChunkSupplier)}.
 * <p>
 * You generally want to avoid storing references of this object as this could lead to a huge memory leak,
 * you should store the chunk coordinates instead.
 */
public abstract class Chunk implements BlockGetter, BlockSetter, Viewable, Tickable, DataContainer {

    protected static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    private final UUID identifier;

    protected Instance instance;
    @NotNull
    protected final Biome[] biomes;
    protected final int chunkX, chunkZ;

    // Options
    private final boolean shouldGenerate;
    private boolean readOnly;

    protected volatile boolean loaded = true;
    protected final Set<Player> viewers = ConcurrentHashMap.newKeySet();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);

    // Path finding
    protected PFColumnarSpace columnarSpace;

    // Data
    protected Data data;

    public Chunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ, boolean shouldGenerate) {
        this.identifier = UUID.randomUUID();
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.shouldGenerate = shouldGenerate;

        final int biomeCount = Biome.getBiomeCount(instance.getDimensionType());
        if (biomes != null && biomes.length == biomeCount) {
            this.biomes = biomes;
        } else {
            this.biomes = new Biome[biomeCount];
        }
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
    public abstract void setBlock(int x, int y, int z, @NotNull Block block);

    public abstract @NotNull Map<Integer, Section> getSections();

    public abstract @Nullable Section getSection(int section);

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
     * Gets all the block entities in this chunk.
     *
     * @return the block entities in this chunk
     */
    @NotNull
    public abstract Set<Integer> getBlockEntities();

    /**
     * Gets the last time that this chunk changed.
     * <p>
     * "Change" means here data used in {@link ChunkDataPacket}.
     * It is necessary to see if the cached version of this chunk can be used
     * instead of re writing and compressing everything.
     *
     * @return the last change time in milliseconds
     */
    public abstract long getLastChangeTime();

    /**
     * Serializes the chunk into bytes.
     *
     * @return the serialized chunk, can be null if this chunk cannot be serialized
     * @see #readChunk(BinaryReader, ChunkCallback) which should be able to read what is written in this method
     */
    public abstract byte[] getSerializedData();

    /**
     * Reads the chunk from binary.
     * <p>
     * Used if the chunk is loaded from file.
     *
     * @param reader   the data reader
     *                 WARNING: the data will not necessary be read directly in the same thread,
     *                 be sure that the data is only used for this reading.
     * @param callback the optional callback to execute once the chunk is done reading
     *                 WARNING: this need to be called to notify the instance.
     * @see #getSerializedData() which is responsible for the serialized data given
     */
    public abstract void readChunk(@NotNull BinaryReader reader, @Nullable ChunkCallback callback);

    /**
     * Creates a {@link ChunkDataPacket} with this chunk data ready to be written.
     *
     * @return a new chunk data packet
     */
    @NotNull
    public abstract ChunkDataPacket createChunkPacket();

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
    @NotNull
    public abstract Chunk copy(@NotNull Instance instance, int chunkX, int chunkZ);

    /**
     * Resets the chunk, this means clearing all the data making it empty.
     */
    public abstract void reset();

    /**
     * Gets the unique identifier of this chunk.
     * <p>
     * WARNING: this UUID is not persistent but randomized once the object is instantiate.
     *
     * @return the chunk identifier
     */
    @NotNull
    public UUID getIdentifier() {
        return identifier;
    }

    /**
     * Gets the instance where this chunk is stored
     *
     * @return the linked instance
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }

    public Biome[] getBiomes() {
        return biomes;
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
     * Creates a {@link Position} object based on this chunk.
     *
     * @return the position of this chunk
     */
    @NotNull
    public Position toPosition() {
        return new Position(CHUNK_SIZE_Z * getChunkX(), 0, CHUNK_SIZE_Z * getChunkZ());
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
     * Gets the light packet of this chunk.
     *
     * @return the light packet
     */
    @NotNull
    public UpdateLightPacket getLightPacket() {
        // TODO do not hardcode light

        // Creates a light packet for the given number of sections with all block light at max and no sky light.
        UpdateLightPacket updateLightPacket = new UpdateLightPacket(getIdentifier(), getLastChangeTime());
        updateLightPacket.chunkX = getChunkX();
        updateLightPacket.chunkZ = getChunkZ();

        final int sectionCount = (getInstance().getDimensionType().getTotalHeight() / 16) + 2;
        final int maskLength = (int) Math.ceil((double) sectionCount / 64);

        updateLightPacket.skyLightMask = new long[maskLength];
        updateLightPacket.blockLightMask = new long[maskLength];
        updateLightPacket.emptySkyLightMask = new long[maskLength];
        updateLightPacket.emptyBlockLightMask = new long[maskLength];
        // Set all block light and no sky light
        Arrays.fill(updateLightPacket.blockLightMask, -1L);
        Arrays.fill(updateLightPacket.emptySkyLightMask, -1L);

        byte[] bytes = new byte[2048];
        Arrays.fill(bytes, (byte) 0xFF);
        final List<byte[]> temp = new ArrayList<>(sectionCount);
        for (int i = 0; i < sectionCount; ++i) {
            temp.add(bytes);
        }
        updateLightPacket.blockLight = temp;
        return updateLightPacket;
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

    /**
     * Sends the chunk to {@code player} and add it to the player viewing chunks collection
     * and send a {@link PlayerChunkLoadEvent}.
     *
     * @param player the viewer to add
     * @return true if the player has just been added to the viewer collection
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        final boolean result = this.viewers.add(player);

        // Add to the viewable chunks set
        player.getViewableChunks().add(this);

        // Send the chunk data & light packets to the player
        sendChunk(player);

        if (result) {
            PlayerChunkLoadEvent playerChunkLoadEvent = new PlayerChunkLoadEvent(player, chunkX, chunkZ);
            player.callEvent(PlayerChunkLoadEvent.class, playerChunkLoadEvent);
        }

        return result;
    }

    /**
     * Removes the chunk to the player viewing chunks collection
     * and send a {@link PlayerChunkUnloadEvent}.
     *
     * @param player the viewer to remove
     * @return true if the player has just been removed to the viewer collection
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        final boolean result = this.viewers.remove(player);

        // Remove from the viewable chunks set
        player.getViewableChunks().remove(this);

        if (result) {
            PlayerChunkUnloadEvent playerChunkUnloadEvent = new PlayerChunkUnloadEvent(player, chunkX, chunkZ);
            player.callEvent(PlayerChunkUnloadEvent.class, playerChunkUnloadEvent);
        }

        return result;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
    }

    @Nullable
    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(@Nullable Data data) {
        this.data = data;
    }

    /**
     * Sends the chunk data to {@code player}.
     *
     * @param player the player
     */
    public synchronized void sendChunk(@NotNull Player player) {
        // Only send loaded chunk
        if (!isLoaded())
            return;

        final PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(getLightPacket());
        playerConnection.sendPacket(createChunkPacket());
    }

    public synchronized void sendChunk() {
        if (!isLoaded()) {
            return;
        }
        sendPacketToViewers(getLightPacket());
        sendPacketToViewers(createChunkPacket());
    }

    /**
     * Sends a full {@link ChunkDataPacket} to {@code player}.
     *
     * @param player the player to update the chunk to
     */
    public synchronized void sendChunkUpdate(@NotNull Player player) {
        final PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createChunkPacket());
    }

    /**
     * Sends a full {@link ChunkDataPacket} to all chunk viewers.
     */
    public synchronized void sendChunkUpdate() {
        PacketUtils.sendGroupedPacket(getViewers(), createChunkPacket());
    }

    /**
     * Sends a chunk section update packet to {@code player}.
     *
     * @param section the section to update
     * @param player  the player to send the packet to
     * @throws IllegalArgumentException if {@code section} is not a valid section
     */
    public synchronized void sendChunkSectionUpdate(int section, @NotNull Player player) {
        if (!PlayerUtils.isNettyClient(player))
            return;
        player.getPlayerConnection().sendPacket(createChunkSectionUpdatePacket(section));
    }

    /**
     * Gets the {@link ChunkDataPacket} to update a single chunk section.
     *
     * @param section the chunk section to update
     * @return the {@link ChunkDataPacket} to update a single chunk section
     */
    protected @NotNull ChunkDataPacket createChunkSectionUpdatePacket(int section) {
        ChunkDataPacket chunkDataPacket = createChunkPacket();
        chunkDataPacket.sections.entrySet().removeIf(entry -> entry.getKey() != section);
        return chunkDataPacket;
    }

    /**
     * Sets the chunk as "unloaded".
     */
    protected void unload() {
        this.loaded = false;
        ChunkDataPacket.CACHE.invalidate(getIdentifier());
        UpdateLightPacket.CACHE.invalidate(getIdentifier());
    }

    /**
     * Gets the index of a position, used to store blocks.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the block index
     */
    protected int getBlockIndex(int x, int y, int z) {
        return ChunkUtils.getBlockIndex(x, y, z);
    }
}