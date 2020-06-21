package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO light data & API
public final class Chunk implements Viewable {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    public static final int BIOME_COUNT = 1024; // 4x4x4 blocks

    private Biome[] biomes;
    private int chunkX, chunkZ;

    // blocks id based on coord, see Chunk#getBlockIndex
    private short[] blocksId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
    private short[] customBlocksId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];

    // Used to get all blocks with data (no null)
    // Key is still chunk coord
    private Int2ObjectMap<Data> blocksData = new Int2ObjectOpenHashMap<>(16 * 16); // Start with the size of a single row

    // Contains CustomBlocks' index which are updatable
    private IntSet updatableBlocks = new IntOpenHashSet();
    // (block index)/(last update in ms)
    private Int2LongMap updatableBlocksLastUpdate = new Int2LongOpenHashMap();

    protected volatile boolean packetUpdated;

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // Cache
    private boolean loaded = true;
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private ByteBuf fullDataPacket;

    public Chunk(Biome[] biomes, int chunkX, int chunkZ) {
        this.biomes = biomes;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void UNSAFE_setBlock(int x, int y, int z, short blockId, Data data) {
        setBlock(x, y, z, blockId, (short) 0, data, null);
    }

    public void UNSAFE_setCustomBlock(int x, int y, int z, short visualBlockId, short customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block " + customBlockId + " does not exist or isn't registered");

        UNSAFE_setCustomBlock(x, y, z, visualBlockId, customBlock, data);
    }

    protected void UNSAFE_setCustomBlock(int x, int y, int z, short visualBlockId, CustomBlock customBlock, Data data) {
        UpdateConsumer updateConsumer = customBlock.hasUpdate() ? customBlock::update : null;
        setBlock(x, y, z, visualBlockId, customBlock.getCustomBlockId(), data, updateConsumer);
    }

    public void UNSAFE_removeCustomBlock(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        this.customBlocksId[index] = 0; // Set to none
        this.blocksData.remove(index);

        this.updatableBlocks.remove(index);
        this.updatableBlocksLastUpdate.remove(index);

        this.blockEntities.remove(index);
    }

    private void setBlock(int x, int y, int z, short blockId, short customId, Data data, UpdateConsumer updateConsumer) {
        int index = getBlockIndex(x, y, z);
        if (blockId != 0
                || (blockId == 0 && customId != 0 && updateConsumer != null)) { // Allow custom air block for update purpose, refused if no update consumer has been found
            this.blocksId[index] = blockId;
            this.customBlocksId[index] = customId;
        } else {
            // Block has been deleted, clear cache and return

            this.blocksId[index] = 0; // Set to air

            this.blocksData.remove(index);

            this.updatableBlocks.remove(index);
            this.updatableBlocksLastUpdate.remove(index);

            this.blockEntities.remove(index);

            this.packetUpdated = false;
            return;
        }

        // Set the new data (or remove from the map if is null)
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }

        // Set update consumer
        if (updateConsumer != null) {
            this.updatableBlocks.add(index);
            this.updatableBlocksLastUpdate.put(index, System.currentTimeMillis());
        } else {
            this.updatableBlocks.remove(index);
            this.updatableBlocksLastUpdate.remove(index);
        }

        if (isBlockEntity(blockId)) {
            this.blockEntities.add(index);
        } else {
            this.blockEntities.remove(index);
        }

        this.packetUpdated = false;
    }

    public void setBlockData(int x, int y, int z, Data data) {
        int index = getBlockIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    public short getBlockId(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksId.length)) {
            return 0; // TODO: custom invalid block
        }
        short id = blocksId[index];
        return id;
    }

    public short getCustomBlockId(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksId.length)) {
            return 0; // TODO: custom invalid block
        }
        short id = customBlocksId[index];
        return id;
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksId.length)) {
            return null; // TODO: custom invalid block
        }
        short id = customBlocksId[index];
        return id != 0 ? BLOCK_MANAGER.getCustomBlock(id) : null;
    }

    protected CustomBlock getCustomBlock(int index) {
        int[] pos = ChunkUtils.indexToChunkPosition(index);
        return getCustomBlock(pos[0], pos[1], pos[2]);
    }

    protected void refreshBlockValue(int x, int y, int z, short blockId, short customId) {
        int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksId.length)) {
            return;
        }

        this.blocksId[blockIndex] = blockId;
        this.customBlocksId[blockIndex] = customId;
    }

    protected void refreshBlockId(int x, int y, int z, short blockId) {
        int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksId.length)) {
            return;
        }

        this.blocksId[blockIndex] = blockId;
    }

    protected void refreshBlockValue(int x, int y, int z, short blockId) {
        CustomBlock customBlock = getCustomBlock(x, y, z);
        short customBlockId = customBlock == null ? 0 : customBlock.getCustomBlockId();
        refreshBlockValue(x, y, z, blockId, customBlockId);
    }

    public Data getData(int x, byte y, int z) {
        int index = getBlockIndex(x, y, z);
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
            int x = blockPos[0];
            int y = blockPos[1];
            int z = blockPos[2];

            BlockPosition blockPosition = new BlockPosition(x, y, z);
            Data data = getData(index);
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

    public ByteBuf getFullDataPacket() {
        return fullDataPacket;
    }

    private boolean isBlockEntity(short blockId) {
        Block block = Block.fromId(blockId);
        return block.isBlockEntity();
    }

    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    public void setFullDataPacket(ByteBuf fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
        this.packetUpdated = true;
    }

    protected byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);

        for (int i = 0; i < BIOME_COUNT; i++) {
            dos.writeByte(biomes[i].getId());
        }

        for (byte x = 0; x < CHUNK_SIZE_X; x++) {
            for (short y = 0; y < CHUNK_SIZE_Y; y++) {
                for (byte z = 0; z < CHUNK_SIZE_Z; z++) {
                    int index = getBlockIndex(x, y, z);

                    short blockId = blocksId[index];
                    short customBlockId = customBlocksId[index];

                    if (blockId == 0 && customBlockId == 0)
                        continue;

                    Data data = blocksData.get(index);
                    boolean hasData = data != null;

                    // Chunk coordinates
                    dos.writeInt(x);
                    dos.writeInt(y);
                    dos.writeInt(z);

                    // Id
                    dos.writeShort(blockId);
                    dos.writeShort(customBlockId);

                    // Data
                    hasData = (data != null && (data instanceof SerializableData)) && hasData;
                    dos.writeBoolean(hasData);
                    if (hasData) {
                        byte[] d = ((SerializableData) data).getSerializedData();
                        dos.writeInt(d.length);
                        dos.write(d);
                    }
                }
            }
        }

        byte[] result = output.toByteArray();
        return result;
    }

    public ChunkDataPacket getFreshFullDataPacket() {
        ChunkDataPacket fullDataPacket = getFreshPacket();
        fullDataPacket.fullChunk = true;
        return fullDataPacket;
    }

    public ChunkDataPacket getFreshPartialDataPacket() {
        ChunkDataPacket fullDataPacket = getFreshPacket();
        fullDataPacket.fullChunk = false;
        return fullDataPacket;
    }

    /**
     * @return a {@link ChunkDataPacket} containing a copy this chunk data
     */
    private ChunkDataPacket getFreshPacket() {
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.biomes = biomes.clone();
        fullDataPacket.chunkX = chunkX;
        fullDataPacket.chunkZ = chunkZ;
        fullDataPacket.blocksId = blocksId.clone();
        fullDataPacket.customBlocksId = customBlocksId.clone();
        fullDataPacket.blockEntities = new CopyOnWriteArraySet<>(blockEntities);
        fullDataPacket.blocksData = new Int2ObjectOpenHashMap<>(blocksData);
        return fullDataPacket;
    }

    // Write the packet in the current thread
    public void refreshDataPacket() {
        ByteBuf buffer = PacketUtils.writePacket(getFreshFullDataPacket());
        setFullDataPacket(buffer);
    }

    // Write the packet in the writer thread pools
    public void refreshDataPacket(Runnable runnable) {
        PacketWriterUtils.writeCallbackPacket(getFreshFullDataPacket(), buf -> {
            setFullDataPacket(buf);
            runnable.run();
        });
    }

    /**
     * Used to verify if the chunk should still be kept in memory
     * having the chunk unloaded means no data is contained in it (blocks, data, etc...)
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
        boolean result = this.viewers.add(player);

        PlayerChunkLoadEvent playerChunkLoadEvent = new PlayerChunkLoadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkLoadEvent.class, playerChunkLoadEvent);
        return result;
    }

    // UNSAFE
    @Override
    public boolean removeViewer(Player player) {
        boolean result = this.viewers.remove(player);

        PlayerChunkUnloadEvent playerChunkUnloadEvent = new PlayerChunkUnloadEvent(player, chunkX, chunkZ);
        player.callEvent(PlayerChunkUnloadEvent.class, playerChunkUnloadEvent);
        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    /**
     * Set the chunk as "unloaded"
     */
    protected void unload() {
        this.loaded = false;
    }

    private int getBlockIndex(int x, int y, int z) {
        return ChunkUtils.getBlockIndex(x, y, z);
    }
}