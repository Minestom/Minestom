package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

// TODO light data & API
public abstract class Chunk implements Viewable {

    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    public static final int BIOME_COUNT = 1024; // 4x4x4 blocks

    protected Biome[] biomes;
    protected int chunkX, chunkZ;

    // blocks id based on coord, see Chunk#getBlockIndex
    //public short[] blocksStateId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
    //protected short[] customBlocksId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];

    // Used to get all blocks with data (no null)
    // Key is still chunk coord
    protected Int2ObjectMap<Data> blocksData = new Int2ObjectOpenHashMap<>(16 * 16); // Start with the size of a single row

    // Contains CustomBlocks' index which are updatable
    protected IntSet updatableBlocks = new IntOpenHashSet();
    // (block index)/(last update in ms)
    protected Int2LongMap updatableBlocksLastUpdate = new Int2LongOpenHashMap();

    protected AtomicBoolean packetUpdated = new AtomicBoolean(false);

    // Block entities
    protected Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // Path finding
    protected PFColumnarSpace columnarSpace;

    // Cache
    protected volatile boolean loaded = true;
    protected Set<Player> viewers = new CopyOnWriteArraySet<>();
    protected ByteBuf fullDataPacket;

    public Chunk(Biome[] biomes, int chunkX, int chunkZ) {
        this.biomes = biomes;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void UNSAFE_setBlock(int x, int y, int z, short blockStateId, Data data) {
        setBlock(x, y, z, blockStateId, (short) 0, data, null);
    }

    public void UNSAFE_setCustomBlock(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block " + customBlockId + " does not exist or isn't registered");

        UNSAFE_setCustomBlock(x, y, z, blockStateId, customBlock, data);
    }

    protected void UNSAFE_setCustomBlock(int x, int y, int z, short blockStateId, CustomBlock customBlock, Data data) {
        final UpdateConsumer updateConsumer = customBlock.hasUpdate() ? customBlock::update : null;
        setBlock(x, y, z, blockStateId, customBlock.getCustomBlockId(), data, updateConsumer);
    }

    public abstract void UNSAFE_removeCustomBlock(int x, int y, int z);

    protected abstract void setBlock(int x, int y, int z, short blockStateId, short customId, Data data, UpdateConsumer updateConsumer);

    public void setBlockData(int x, int y, int z, Data data) {
        final int index = getBlockIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    public abstract short getBlockStateId(int x, int y, int z);

    public abstract short getCustomBlockId(int x, int y, int z);

    public abstract CustomBlock getCustomBlock(int x, int y, int z);

    protected CustomBlock getCustomBlock(int index) {
        final int[] pos = ChunkUtils.indexToChunkPosition(index);
        return getCustomBlock(pos[0], pos[1], pos[2]);
    }

    protected abstract void refreshBlockValue(int x, int y, int z, short blockStateId, short customId);

    protected abstract void refreshBlockStateId(int x, int y, int z, short blockStateId);

    protected void refreshBlockValue(int x, int y, int z, short blockStateId) {
        final CustomBlock customBlock = getCustomBlock(x, y, z);
        final short customBlockId = customBlock == null ? 0 : customBlock.getCustomBlockId();
        refreshBlockValue(x, y, z, blockStateId, customBlockId);
    }

    public Data getData(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        return getData(index);
    }

    protected Data getData(int index) {
        return blocksData.get(index);
    }

    public synchronized void updateBlocks(long time, Instance instance) {
        if (updatableBlocks.isEmpty())
            return;

        // Block all chunk operation during the update
        IntIterator iterator = new IntOpenHashSet(updatableBlocks).iterator();
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

            final int[] blockPos = ChunkUtils.indexToPosition(index, chunkX, chunkZ);
            final int x = blockPos[0];
            final int y = blockPos[1];
            final int z = blockPos[2];

            final BlockPosition blockPosition = new BlockPosition(x, y, z);
            final Data data = getData(index);
            customBlock.update(instance, blockPosition, data);
        }
    }

    public Biome[] getBiomes() {
        return biomes;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * Get the cached data packet
     * <p>
     * Use {@link #retrieveDataBuffer(Consumer)} to be sure to get the updated version
     *
     * @return the current cached data packet, can be null or outdated
     */
    public ByteBuf getFullDataPacket() {
        return fullDataPacket;
    }

    public void setFullDataPacket(ByteBuf fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
        this.packetUpdated.set(true);
    }

    protected boolean isBlockEntity(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId);
        return block.hasBlockEntity();
    }

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
     * Retrieve the updated data packet
     *
     * @param consumer the consumer called once the packet is sure to be up-to-date
     */
    public void retrieveDataBuffer(Consumer<ByteBuf> consumer) {
        final ByteBuf data = getFullDataPacket();
        if (data == null || !packetUpdated.get()) {
            PacketWriterUtils.writeCallbackPacket(getFreshFullDataPacket(), packet -> {
                setFullDataPacket(packet);
                consumer.accept(packet);
            });
        } else {
            consumer.accept(data);
        }
    }

    /**
     * Serialize the chunk
     *
     * @return the serialized chunk
     * @throws IOException
     */
    protected abstract byte[] getSerializedData() throws IOException;

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

    // UNSAFE
    @Override
    public boolean addViewer(Player player) {
        final boolean result = this.viewers.add(player);

        // Send the chunk data & light packets to the player
        sendChunk(player);

        PlayerChunkLoadEvent playerChunkLoadEvent = new PlayerChunkLoadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkLoadEvent.class, playerChunkLoadEvent);
        return result;
    }

    // UNSAFE
    @Override
    public boolean removeViewer(Player player) {
        final boolean result = this.viewers.remove(player);

        PlayerChunkUnloadEvent playerChunkUnloadEvent = new PlayerChunkUnloadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkUnloadEvent.class, playerChunkUnloadEvent);
        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    /**
     * Send the chunk data to {@code player}
     *
     * @param player the player
     */
    protected void sendChunk(Player player) {
        if (!isLoaded())
            return;
        if (!PlayerUtils.isNettyClient(player))
            return;

        final PlayerConnection playerConnection = player.getPlayerConnection();

        retrieveDataBuffer(buf -> {
            playerConnection.sendPacket(buf, true);
        });

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
            List<byte[]> temp = new ArrayList<>();
            List<byte[]> temp2 = new ArrayList<>();
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
            retrieveDataBuffer(buf -> {
                chunkViewers.forEach(player -> {
                    final PlayerConnection playerConnection = player.getPlayerConnection();
                    if (!PlayerUtils.isNettyClient(playerConnection))
                        return;

                    playerConnection.sendPacket(buf, true);
                });
            });

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

        PacketWriterUtils.writeAndSend(player, getChunkSectionUpdatePacket(section));
    }

    protected ChunkDataPacket getChunkSectionUpdatePacket(int section) {
        ChunkDataPacket chunkDataPacket = getFreshPartialDataPacket();
        chunkDataPacket.fullChunk = false;
        int[] sections = new int[16];
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

    protected int getBlockIndex(int x, int y, int z) {
        return ChunkUtils.getBlockIndex(x, y, z);
    }
}