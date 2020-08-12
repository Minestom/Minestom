package net.minestom.server.instance;

import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFBlockDescription;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
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
import net.minestom.server.world.biomes.Biome;

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
    public short[] blocksStateId = new short[CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z];
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

    // Path finding
    private PFColumnarSpace columnarSpace;

    // Cache
    private volatile boolean loaded = true;
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private ByteBuf fullDataPacket;

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

    public void UNSAFE_removeCustomBlock(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        this.customBlocksId[index] = 0; // Set to none
        this.blocksData.remove(index);

        this.updatableBlocks.remove(index);
        this.updatableBlocksLastUpdate.remove(index);

        this.blockEntities.remove(index);
    }

    private void setBlock(int x, int y, int z, short blockStateId, short customId, Data data, UpdateConsumer updateConsumer) {
        final int index = getBlockIndex(x, y, z);
        if (blockStateId != 0
                || (blockStateId == 0 && customId != 0 && updateConsumer != null)) { // Allow custom air block for update purpose, refused if no update consumer has been found
            this.blocksStateId[index] = blockStateId;
            this.customBlocksId[index] = customId;
        } else {
            // Block has been deleted, clear cache and return

            this.blocksStateId[index] = 0; // Set to air

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

        if (isBlockEntity(blockStateId)) {
            this.blockEntities.add(index);
        } else {
            this.blockEntities.remove(index);
        }

        this.packetUpdated = false;

        if (columnarSpace != null) {
            final ColumnarOcclusionFieldList columnarOcclusionFieldList = columnarSpace.occlusionFields();
            final PFBlockDescription blockDescription = new PFBlockDescription(Block.fromStateId(blockStateId));
            columnarOcclusionFieldList.onBlockChanged(x, y, z, blockDescription, 0);
        }
    }

    public void setBlockData(int x, int y, int z, Data data) {
        final int index = getBlockIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    public short getBlockStateId(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return 0; // TODO: custom invalid block
        }
        final short id = blocksStateId[index];
        return id;
    }

    public short getCustomBlockId(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return 0; // TODO: custom invalid block
        }
        final short id = customBlocksId[index];
        return id;
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        final int index = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(index, 0, blocksStateId.length)) {
            return null; // TODO: custom invalid block
        }
        final short id = customBlocksId[index];
        return id != 0 ? BLOCK_MANAGER.getCustomBlock(id) : null;
    }

    protected CustomBlock getCustomBlock(int index) {
        final int[] pos = ChunkUtils.indexToChunkPosition(index);
        return getCustomBlock(pos[0], pos[1], pos[2]);
    }

    protected void refreshBlockValue(int x, int y, int z, short blockStateId, short customId) {
        final int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksStateId.length)) {
            return;
        }

        this.blocksStateId[blockIndex] = blockStateId;
        this.customBlocksId[blockIndex] = customId;
    }

    protected void refreshBlockStateId(int x, int y, int z, short blockStateId) {
        final int blockIndex = getBlockIndex(x, y, z);
        if (!MathUtils.isBetween(blockIndex, 0, blocksStateId.length)) {
            return;
        }

        this.blocksStateId[blockIndex] = blockStateId;
    }

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

    public ByteBuf getFullDataPacket() {
        return fullDataPacket;
    }

    private boolean isBlockEntity(short blockStateId) {
        final Block block = Block.fromStateId(blockStateId);
        return block.hasBlockEntity();
    }

    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    /**
     * Get the columnar space linked to this chunk
     * <p>
     * Used internally by the pathfinder
     *
     * @return this chunk columnar space
     */
    public PFColumnarSpace getColumnarSpace() {
        return columnarSpace;
    }

    /**
     * Change this chunk columnar space
     *
     * @param columnarSpace the new columnar space
     */
    public void setColumnarSpace(PFColumnarSpace columnarSpace) {
        this.columnarSpace = columnarSpace;
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
                    final int index = getBlockIndex(x, y, z);

                    final short blockStateId = blocksStateId[index];
                    final short customBlockId = customBlocksId[index];

                    if (blockStateId == 0 && customBlockId == 0)
                        continue;

                    final Data data = blocksData.get(index);

                    // Chunk coordinates
                    dos.writeInt(x);
                    dos.writeInt(y);
                    dos.writeInt(z);

                    // Id
                    dos.writeShort(blockStateId);
                    dos.writeShort(customBlockId);

                    // Data
                    final boolean hasData = (data != null && (data instanceof SerializableData));
                    dos.writeBoolean(hasData);
                    if (hasData) {
                        final byte[] d = ((SerializableData) data).getSerializedData();
                        dos.writeInt(d.length);
                        dos.write(d);
                    }
                }
            }
        }

        final byte[] result = output.toByteArray();
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
        fullDataPacket.blocksStateId = blocksStateId.clone();
        fullDataPacket.customBlocksId = customBlocksId.clone();
        fullDataPacket.blockEntities = new CopyOnWriteArraySet<>(blockEntities);
        fullDataPacket.blocksData = new Int2ObjectOpenHashMap<>(blocksData);
        return fullDataPacket;
    }

    // Write the packet in the current thread
    public void refreshDataPacket() {
        final ByteBuf buffer = PacketUtils.writePacket(getFreshFullDataPacket());
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
     * Set the chunk as "unloaded"
     */
    protected void unload() {
        this.loaded = false;
    }

    private int getBlockIndex(int x, int y, int z) {
        return ChunkUtils.getBlockIndex(x, y, z);
    }
}