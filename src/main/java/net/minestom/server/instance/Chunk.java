package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
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
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

// TODO light data & API
public abstract class Chunk implements Viewable, DataContainer {

    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    protected static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    public static final int CHUNK_SECTION_COUNT = CHUNK_SIZE_Y / CHUNK_SECTION_SIZE;

    public static final int BIOME_COUNT = 1024; // 4x4x4 blocks group

    protected final Instance instance;
    protected Biome[] biomes;
    protected int chunkX, chunkZ;

    // Used to get all blocks with data (no null)
    // Key is still chunk coord
    protected Int2ObjectMap<Data> blocksData = new Int2ObjectOpenHashMap<>(16 * 16); // Start with the size of a single row

    // Contains CustomBlocks' block index which are updatable
    protected IntSet updatableBlocks = new IntOpenHashSet();
    // (block index)/(last update in ms)
    protected Int2LongMap updatableBlocksLastUpdate = new Int2LongOpenHashMap();

    protected volatile boolean packetUpdated;

    // Block entities
    protected Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // Path finding
    protected PFColumnarSpace columnarSpace;

    // Cache
    protected volatile boolean loaded = true;
    protected Set<Player> viewers = new CopyOnWriteArraySet<>();
    protected ByteBuf fullDataPacket;

    // Data
    protected Data data;

    public Chunk(Instance instance, Biome[] biomes, int chunkX, int chunkZ) {
        this.instance = instance;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        if (biomes != null && biomes.length == BIOME_COUNT) {
            this.biomes = biomes;
        } else {
            this.biomes = new Biome[BIOME_COUNT];
        }
    }

    /**
     * Set a block at a position
     * <p>
     * This is used when the previous block has to be destroyed, meaning that it clears the previous data and update method
     * <p>
     * WARNING: this method is not thread-safe (in order to bring performance improvement with {@link ChunkBatch} &amp; {@link BlockBatch})
     * The thread-safe version is {@link InstanceContainer#setSeparateBlocks(int, int, int, short, short, Data)} (or any similar instance methods)
     * Otherwise, you can simply do not forget to have this chunk synchronized when this is called
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param blockStateId  the block state id
     * @param customBlockId the custom block id
     * @param data          the data of the block, can be null
     * @param updatable     true if the block has an update method
     *                      Warning: <code>customBlockId</code> cannot be 0 and needs to be valid since the update delay and method
     *                      will be retrieved from the associated {@link CustomBlock} object
     */
    public abstract void setBlock(int x, int y, int z, short blockStateId, short customBlockId, Data data, boolean updatable);

    /**
     * Set the {@link Data} at a position
     *
     * @param x    the block X
     * @param y    the block Y
     * @param z    the block Z
     * @param data the new data
     */
    public void setBlockData(int x, int y, int z, Data data) {
        final int index = getBlockIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    /**
     * Get the block state id at a position
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the block state id at the position
     */
    public abstract short getBlockStateId(int x, int y, int z);

    /**
     * Get the custom block id at a position
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the custom block id at the position
     */
    public abstract short getCustomBlockId(int x, int y, int z);

    /**
     * Get the {@link CustomBlock} at a position
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
     * Get the {@link CustomBlock} at a block index
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

    /**
     * Change the block state id and the custom block id at a position
     *
     * @param x             the block X
     * @param y             the block Y
     * @param z             the block Z
     * @param blockStateId  the new block state id
     * @param customBlockId the new custom block id
     */
    protected abstract void refreshBlockValue(int x, int y, int z, short blockStateId, short customBlockId);

    /**
     * Change the block state id at a position (the custom block id stays the same)
     *
     * @param x            the block X
     * @param y            the block Y
     * @param z            the block Z
     * @param blockStateId the new block state id
     */
    protected abstract void refreshBlockStateId(int x, int y, int z, short blockStateId);

    /**
     * Get the {@link Data} at a position
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return the {@link Data} at the position, null if none
     */
    public Data getBlockData(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        return getBlockData(index);
    }

    /**
     * Get the {@link Data} at a block index
     *
     * @param index the block index
     * @return the {@link Data} at the block index, null if none
     */
    protected Data getBlockData(int index) {
        return blocksData.get(index);
    }

    /**
     * Execute a tick update for all the updatable blocks in this chunk
     *
     * @param time     the time of the update in milliseconds
     * @param instance the instance linked to this chunk
     */
    public synchronized void updateBlocks(long time, Instance instance) {
        if (updatableBlocks.isEmpty())
            return;

        // Block all chunk operation during the update
        final IntIterator iterator = new IntOpenHashSet(updatableBlocks).iterator();
        while (iterator.hasNext()) {
            final int index = iterator.nextInt();
            final CustomBlock customBlock = getCustomBlock(index);

            // Update cooldown
            final UpdateOption updateOption = customBlock.getUpdateOption();
            final long lastUpdate = updatableBlocksLastUpdate.get(index);
            final boolean hasCooldown = CooldownUtils.hasCooldown(time, lastUpdate, updateOption);
            if (hasCooldown)
                continue;

            this.updatableBlocksLastUpdate.put(index, time); // Refresh last update time

            final BlockPosition blockPosition = ChunkUtils.getBlockPosition(index, chunkX, chunkZ);
            final Data data = getBlockData(index);
            customBlock.update(instance, blockPosition, data);
        }
    }

    public Biome[] getBiomes() {
        return biomes;
    }

    /**
     * Get the chunk X
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Get the chunk Z
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Get the cached data packet
     * <p>
     * Use {@link #retrieveDataBuffer(Consumer)} to be sure to get the updated version
     *
     * @return the last cached data packet, can be null or outdated
     */
    public ByteBuf getFullDataPacket() {
        return fullDataPacket;
    }

    public void setFullDataPacket(ByteBuf fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
        this.packetUpdated = true;
    }

    /**
     * Get if a block state id represents a block entity
     *
     * @param blockStateId the block state id to check
     * @return true if {@code blockStateId} represents a block entity
     */
    protected boolean isBlockEntity(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId);
        return block.hasBlockEntity();
    }

    /**
     * Get all the block entities in this chunk
     *
     * @return the block entities in this chunk
     */
    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    /**
     * Change this chunk columnar space
     *
     * @param columnarSpace the new columnar space
     */
    public void setColumnarSpace(PFColumnarSpace columnarSpace) {
        this.columnarSpace = columnarSpace;
    }

    /**
     * Retrieve (and save if needed) the updated data packet
     *
     * @param consumer the consumer called once the packet is sure to be up-to-date
     */
    public void retrieveDataBuffer(Consumer<ByteBuf> consumer) {
        final ByteBuf data = getFullDataPacket();
        if (data == null || !packetUpdated) {
            // Packet has never been wrote or is outdated, write it
            PacketWriterUtils.writeCallbackPacket(getFreshFullDataPacket(), packet -> {
                setFullDataPacket(packet);
                consumer.accept(packet);
            });
        } else {
            // Packet is up-to-date
            consumer.accept(data);
        }
    }

    /**
     * Serialize the chunk into bytes
     *
     * @return the serialized chunk, can be null if this chunk cannot be serialized
     */
    public abstract byte[] getSerializedData();

    /**
     * Read the chunk from binary
     * <p>
     * Used if the chunk is loaded from file
     *
     * @param reader   the data reader
     * @param callback the callback to execute once the chunk is done reading
     *                 WARNING: this need to be called to notify the instance
     */
    public abstract void readChunk(BinaryReader reader, ChunkCallback callback);

    /**
     * Get a {@link ChunkDataPacket} which should contain the full chunk
     *
     * @return a fresh full chunk data packet
     */
    public ChunkDataPacket getFreshFullDataPacket() {
        ChunkDataPacket fullDataPacket = getFreshPacket();
        fullDataPacket.fullChunk = true;
        return fullDataPacket;
    }

    /**
     * Get a {@link ChunkDataPacket} which should contain the non-full chunk
     *
     * @return a fresh non-full chunk data packet
     */
    public ChunkDataPacket getFreshPartialDataPacket() {
        ChunkDataPacket fullDataPacket = getFreshPacket();
        fullDataPacket.fullChunk = false;
        return fullDataPacket;
    }

    /**
     * @return a {@link ChunkDataPacket} containing a copy this chunk data
     */
    protected abstract ChunkDataPacket getFreshPacket();

    /**
     * Used to verify if the chunk should still be kept in memory
     *
     * @return true if the chunk is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String toString() {
        return "Chunk[" + chunkX + ":" + chunkZ + "]";
    }

    /**
     * Send the chunk to {@code player} and add it to the player viewing chunks collection
     * and send a {@link PlayerChunkLoadEvent}
     *
     * @param player the viewer to add
     * @return true if the player has just been added to the viewer collection
     */
    @Override
    public boolean addViewer(Player player) {
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
     * Remove the chunk to the player viewing chunks collection
     * and send a {@link PlayerChunkUnloadEvent}
     *
     * @param player the viewer to remove
     * @return true if the player has just been removed to the viewer collection
     */
    @Override
    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);

        // Remove from the viewable chunks set
        player.getViewableChunks().remove(this);

        PlayerChunkUnloadEvent playerChunkUnloadEvent = new PlayerChunkUnloadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkUnloadEvent.class, playerChunkUnloadEvent);
        return result;
    }

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
     * Send the chunk data to {@code player}
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
     * Send a full {@link ChunkDataPacket} to {@code player}
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
     * Send a full {@link ChunkDataPacket} to all chunk viewers
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
     * Send a chunk section update packet to {@code player}
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
     * Get the {@link ChunkDataPacket} to update a single chunk section
     *
     * @param section the chunk section to update
     * @return the {@link ChunkDataPacket} to update a single chunk sectionl
     */
    protected ChunkDataPacket getChunkSectionUpdatePacket(int section) {
        ChunkDataPacket chunkDataPacket = getFreshPartialDataPacket();
        chunkDataPacket.fullChunk = false;
        int[] sections = new int[CHUNK_SECTION_COUNT];
        sections[section] = 1;
        chunkDataPacket.sections = sections;
        return chunkDataPacket;
    }

    /**
     * Set the chunk as "unloaded"
     */
    protected void unload() {
        this.loaded = false;
    }

    /**
     * Get the index of a position, used to store blocks
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