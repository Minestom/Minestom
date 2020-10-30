package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

// TODO light data & API

/**
 * A chunk is a part of an {@link Instance}, limited by a size of 16x256x16 blocks and subdivided in 16 sections of 16 blocks height.
 * Should contains all the blocks located at those positions and manage their tick updates.
 * <p>
 * Chunks can be serialized using {@link #getSerializedData()} and deserialized back with {@link #readChunk(BinaryReader, ChunkCallback)},
 * allowing you to implement your own storage solution if needed.
 * <p>
 * You can create your own implementation of this class by extending it and create the objects in {@link InstanceContainer#setChunkSupplier(ChunkSupplier)}.
 */
public abstract class Chunk implements Viewable, DataContainer {

    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    protected static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    public static final int CHUNK_SECTION_COUNT = CHUNK_SIZE_Y / CHUNK_SECTION_SIZE;

    public static final int BIOME_COUNT = 1024; // 4x4x4 blocks group

    @NotNull
    protected final Instance instance;
    @NotNull
    protected final Biome[] biomes;
    protected final int chunkX, chunkZ;

    // Options
    private final boolean shouldGenerate;
    private boolean readOnly;

    // Packet cache
    private volatile boolean enableCachePacket;
    protected volatile boolean packetUpdated;
    private ByteBuf fullDataPacket;

    protected volatile boolean loaded = true;
    protected final Set<Player> viewers = new CopyOnWriteArraySet<>();

    // Path finding
    protected PFColumnarSpace columnarSpace;

    // Data
    protected Data data;

    public Chunk(@NotNull Instance instance, @Nullable Biome[] biomes, int chunkX, int chunkZ, boolean shouldGenerate) {
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.shouldGenerate = shouldGenerate;

        // true by default
        this.enableCachePacket = true;

        if (biomes != null && biomes.length == BIOME_COUNT) {
            this.biomes = biomes;
        } else {
            this.biomes = new Biome[BIOME_COUNT];
        }
    }

    /**
     * Sets a block at a position.
     * <p>
     * This is used when the previous block has to be destroyed/replaced, meaning that it clears the previous data and update method.
     * <p>
     * WARNING: this method is not thread-safe (in order to bring performance improvement with {@link ChunkBatch} and {@link BlockBatch})
     * The thread-safe version is {@link InstanceContainer#setSeparateBlocks(int, int, int, short, short, Data)} (or any similar instance methods)
     * Otherwise, you can simply do not forget to have this chunk synchronized when this is called.
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param blockStateId  the block state id
     * @param customBlockId the custom block id, 0 if not
     * @param data          the {@link Data} of the block, can be null
     * @param updatable     true if the block has an update method
     *                      Warning: <code>customBlockId</code> cannot be 0 in this case and needs to be valid since the update delay and method
     *                      will be retrieved from the associated {@link CustomBlock} object
     */
    public abstract void UNSAFE_setBlock(int x, int y, int z, short blockStateId, short customBlockId, @Nullable Data data, boolean updatable);

    /**
     * Executes a chunk tick.
     * <p>
     * Should be used to update all the blocks in the chunk.
     * <p>
     * WARNING: this method doesn't necessary have to be thread-safe, proceed with caution.
     *
     * @param time     the time of the update in milliseconds
     * @param instance the {@link Instance} linked to this chunk
     */
    public abstract void tick(long time, @NotNull Instance instance);

    /**
     * Gets the block state id at a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the block state id at the position
     */
    public abstract short getBlockStateId(int x, int y, int z);

    /**
     * Gets the custom block id at a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the custom block id at the position
     */
    public abstract short getCustomBlockId(int x, int y, int z);

    /**
     * Changes the block state id and the custom block id at a position.
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param blockStateId  the new block state id
     * @param customBlockId the new custom block id
     */
    protected abstract void refreshBlockValue(int x, int y, int z, short blockStateId, short customBlockId);

    /**
     * Changes the block state id at a position (the custom block id stays the same).
     *
     * @param x            the block X
     * @param y            the block Y
     * @param z            the block Z
     * @param blockStateId the new block state id
     */
    protected abstract void refreshBlockStateId(int x, int y, int z, short blockStateId);

    /**
     * Gets the {@link Data} at a block index.
     *
     * @param index the block index
     * @return the {@link Data} at the block index, null if none
     */
    @Nullable
    public abstract Data getBlockData(int index);

    /**
     * Sets the {@link Data} at a position.
     *
     * @param x    the block X
     * @param y    the block Y
     * @param z    the block Z
     * @param data the new data, can be null
     */
    public abstract void setBlockData(int x, int y, int z, Data data);

    /**
     * Gets all the block entities in this chunk.
     *
     * @return the block entities in this chunk
     */
    @NotNull
    public abstract Set<Integer> getBlockEntities();

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
    protected abstract ChunkDataPacket createFreshPacket();

    /**
     * Gets the {@link CustomBlock} at a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the {@link CustomBlock} at the position
     */
    public CustomBlock getCustomBlock(int x, int y, int z) {
        final short customBlockId = getCustomBlockId(x, y, z);
        return customBlockId != 0 ? BLOCK_MANAGER.getCustomBlock(customBlockId) : null;
    }

    /**
     * Gets the {@link CustomBlock} at a block index.
     *
     * @param index the block index
     * @return the {@link CustomBlock} at the block index
     */
    protected CustomBlock getCustomBlock(int index) {
        final int x = ChunkUtils.blockIndexToChunkPositionX(index);
        final int y = ChunkUtils.blockIndexToChunkPositionY(index);
        final int z = ChunkUtils.blockIndexToChunkPositionZ(index);
        return getCustomBlock(x, y, z);
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
     * Gets if this chunk will or had been loaded with a {@link ChunkGenerator}.
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
     * Gets if this chunk automatically cache the latest {@link ChunkDataPacket} version.
     * <p>
     * Retrieved with {@link #retrieveDataBuffer(Consumer)}.
     *
     * @return true if the chunk automatically cache the chunk packet
     */
    public boolean enableCachePacket() {
        return enableCachePacket;
    }

    /**
     * Enables or disable the automatic {@link ChunkDataPacket} caching.
     *
     * @param enableCachePacket true to enable to chunk packet caching
     */
    public synchronized void setEnableCachePacket(boolean enableCachePacket) {
        this.enableCachePacket = enableCachePacket;
        if (enableCachePacket && fullDataPacket != null) {
            this.fullDataPacket.release();
            this.fullDataPacket = null;
        }
    }

    /**
     * Gets the cached data packet.
     * <p>
     * Use {@link #retrieveDataBuffer(Consumer)} to be sure to get the updated version.
     *
     * @return the last cached data packet, can be null or outdated
     */
    public ByteBuf getFullDataPacket() {
        return fullDataPacket;
    }

    /**
     * Sets the cached {@link ChunkDataPacket} of this chunk.
     *
     * @param fullDataPacket the new cached chunk packet
     */
    public void setFullDataPacket(ByteBuf fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
        this.packetUpdated = true;
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
     * Retrieves (and cache if needed) the updated data packet.
     *
     * @param consumer the consumer called once the packet is sure to be up-to-date
     */
    public void retrieveDataBuffer(Consumer<ByteBuf> consumer) {
        final ByteBuf data = getFullDataPacket();
        if (data == null || !packetUpdated) {
            // Packet has never been wrote or is outdated, write it
            PacketWriterUtils.writeCallbackPacket(getFreshFullDataPacket(), packet -> {
                if (enableCachePacket) {
                    setFullDataPacket(packet);
                }
                consumer.accept(packet);
            });
        } else {
            // Packet is up-to-date
            consumer.accept(data);
        }
    }

    /**
     * Gets a {@link ChunkDataPacket} which should contain the full chunk.
     *
     * @return a fresh full chunk data packet
     */
    public ChunkDataPacket getFreshFullDataPacket() {
        ChunkDataPacket fullDataPacket = createFreshPacket();
        fullDataPacket.fullChunk = true;
        return fullDataPacket;
    }

    /**
     * Gets a {@link ChunkDataPacket} which should contain the non-full chunk.
     *
     * @return a fresh non-full chunk data packet
     */
    @NotNull
    public ChunkDataPacket getFreshPartialDataPacket() {
        ChunkDataPacket fullDataPacket = createFreshPacket();
        fullDataPacket.fullChunk = false;
        return fullDataPacket;
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

        // Send the chunk data & light packets to the player
        sendChunk(player);
        // Add to the viewable chunks set
        player.getViewableChunks().add(this);

        PlayerChunkLoadEvent playerChunkLoadEvent = new PlayerChunkLoadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkLoadEvent.class, playerChunkLoadEvent);
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

        PlayerChunkUnloadEvent playerChunkUnloadEvent = new PlayerChunkUnloadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkUnloadEvent.class, playerChunkUnloadEvent);
        return result;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    /**
     * Sends the chunk data to {@code player}.
     *
     * @param player the player
     */
    protected void sendChunk(Player player) {
        // Only send loaded chunk
        if (!isLoaded())
            return;
        // Only send chunk to netty client (because it sends raw ByteBuf buffer)
        if (!PlayerUtils.isNettyClient(player))
            return;

        final PlayerConnection playerConnection = player.getPlayerConnection();

        // Retrieve & send the buffer to the connection
        retrieveDataBuffer(buf -> playerConnection.sendPacket(buf, true));

        // TODO do not hardcode
        if (MinecraftServer.isFixLighting()) {
            UpdateLightPacket updateLightPacket = new UpdateLightPacket();
            updateLightPacket.chunkX = getChunkX();
            updateLightPacket.chunkZ = getChunkZ();
            updateLightPacket.skyLightMask = 0x3FFF0;
            updateLightPacket.blockLightMask = 0x3F;
            updateLightPacket.emptySkyLightMask = 0x0F;
            updateLightPacket.emptyBlockLightMask = 0x3FFC0;
            byte[] bytes = new byte[2048];
            Arrays.fill(bytes, (byte) 0xFF);
            List<byte[]> temp = new ArrayList<>(14);
            List<byte[]> temp2 = new ArrayList<>(6);
            for (int i = 0; i < 14; ++i) {
                temp.add(bytes);
            }
            for (int i = 0; i < 6; ++i) {
                temp2.add(bytes);
            }
            updateLightPacket.skyLight = temp;
            updateLightPacket.blockLight = temp2;
            PacketWriterUtils.writeAndSend(player, updateLightPacket);
        }
    }

    /**
     * Sends a full {@link ChunkDataPacket} to {@code player}.
     *
     * @param player the player to update the chunk to
     */
    public void sendChunkUpdate(Player player) {
        retrieveDataBuffer(buf -> {
            final PlayerConnection playerConnection = player.getPlayerConnection();
            playerConnection.sendPacket(buf, true);
        });
    }

    /**
     * Sends a full {@link ChunkDataPacket} to all chunk viewers.
     */
    public void sendChunkUpdate() {
        final Set<Player> chunkViewers = getViewers();
        if (!chunkViewers.isEmpty()) {
            retrieveDataBuffer(buf -> chunkViewers.forEach(player -> {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                if (!PlayerUtils.isNettyClient(playerConnection))
                    return;

                playerConnection.sendPacket(buf, true);
            }));

        }
    }

    /**
     * Sends a chunk section update packet to {@code player}.
     *
     * @param section the section to update
     * @param player  the player to send the packet to
     */
    public void sendChunkSectionUpdate(int section, Player player) {
        if (!PlayerUtils.isNettyClient(player))
            return;
        Check.argCondition(!MathUtils.isBetween(section, 0, CHUNK_SECTION_COUNT),
                "The chunk section " + section + " does not exist");

        PacketWriterUtils.writeAndSend(player, getChunkSectionUpdatePacket(section));
    }

    /**
     * Gets the {@link ChunkDataPacket} to update a single chunk section.
     *
     * @param section the chunk section to update
     * @return the {@link ChunkDataPacket} to update a single chunk section
     */
    @NotNull
    protected ChunkDataPacket getChunkSectionUpdatePacket(int section) {
        ChunkDataPacket chunkDataPacket = getFreshPartialDataPacket();
        chunkDataPacket.fullChunk = false;
        int[] sections = new int[CHUNK_SECTION_COUNT];
        sections[section] = 1;
        chunkDataPacket.sections = sections;
        return chunkDataPacket;
    }

    /**
     * Sets the chunk as "unloaded".
     */
    protected void unload() {
        this.loaded = false;
    }

    /**
     * Gets if a block state id represents a block entity.
     *
     * @param blockStateId the block state id to check
     * @return true if {@code blockStateId} represents a block entity
     */
    protected boolean isBlockEntity(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId);
        return block.hasBlockEntity();
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