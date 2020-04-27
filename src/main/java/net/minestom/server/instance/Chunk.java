package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.UpdateConsumer;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.UpdateOption;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO light data & API
public class Chunk implements Viewable {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    private Biome biome;
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
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private ByteBuf fullDataPacket;

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void UNSAFE_setBlock(int x, int y, int z, short blockId, Data data) {
        setBlock(x, y, z, blockId, (short) 0, data, null);
    }

    public void UNSAFE_setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The custom block " + customBlockId + " does not exist or isn't registered");

        setCustomBlock(x, y, z, customBlock, data);
    }

    private void setCustomBlock(int x, int y, int z, CustomBlock customBlock, Data data) {
        UpdateConsumer updateConsumer = customBlock.hasUpdate() ? customBlock::update : null;
        setBlock(x, y, z, customBlock.getBlockId(), customBlock.getCustomBlockId(), data, updateConsumer);
    }

    private void setBlock(int x, int y, int z, short blockId, short customId, Data data, UpdateConsumer updateConsumer) {
        int index = SerializerUtils.coordToChunkIndex(x, y, z);
        if (blockId != 0
                || (blockId == 0 && customId != 0 && updateConsumer != null)) { // Allow custom air block for update purpose, refused if no update consumer has been found
            refreshBlockValue(x, y, z, blockId, customId);
        } else {
            // Block has been deleted, clear cache and return

            this.blocksId[getBlockIndex(x, y, z)] = 0; // Set to air
            //this.blocks.remove(index);

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
        int index = SerializerUtils.coordToChunkIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    public short getBlockId(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if(index < 0 || index >= blocksId.length) {
            return 0; // TODO: custom invalid block
        }
        short id = blocksId[index];
        return id;
    }

    public short getCustomBlockId(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if(index < 0 || index >= blocksId.length) {
            return 0; // TODO: custom invalid block
        }
        short id = customBlocksId[index];
        return id;
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        int index = getBlockIndex(x, y, z);
        if(index < 0 || index >= blocksId.length) {
            return null; // TODO: custom invalid block
        }
        short id = customBlocksId[index];
        return id != 0 ? BLOCK_MANAGER.getCustomBlock(id) : null;
    }

    protected CustomBlock getCustomBlock(int index) {
        byte[] pos = SerializerUtils.indexToChunkPosition(index);
        return getCustomBlock(pos[0], pos[1], pos[2]);
    }

    protected void refreshBlockValue(int x, int y, int z, short blockId, short customId) {
        int blockIndex = getBlockIndex(x, y, z);
        if(blockIndex < 0 || blockIndex >= blocksId.length) {
            return;
        }

        this.blocksId[blockIndex] = blockId;
        this.customBlocksId[blockIndex] = customId;
    }

    protected void refreshBlockValue(int x, int y, int z, short blockId) {
        CustomBlock customBlock = getCustomBlock(x, y, z);
        short customBlockId = customBlock == null ? 0 : customBlock.getCustomBlockId();
        refreshBlockValue(x, y, z, blockId, customBlockId);
    }

    public Data getData(byte x, byte y, byte z) {
        int index = SerializerUtils.coordToChunkIndex(x, y, z);
        return getData(index);
    }

    protected Data getData(int index) {
        return blocksData.get(index);
    }

    public void updateBlocks(long time, Instance instance) {
        if (updatableBlocks.isEmpty())
            return;

        // Block all chunk operation during the update
        synchronized (this) {
            IntIterator iterator = new IntOpenHashSet(updatableBlocks).iterator();
            while (iterator.hasNext()) {
                int index = iterator.nextInt();
                CustomBlock customBlock = getCustomBlock(index);

                // Update cooldown
                UpdateOption updateOption = customBlock.getUpdateOption();
                long lastUpdate = updatableBlocksLastUpdate.get(index);
                boolean hasCooldown = CooldownUtils.hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
                if (hasCooldown)
                    continue;

                this.updatableBlocksLastUpdate.put(index, time); // Refresh last update time

                byte[] blockPos = SerializerUtils.indexToChunkPosition(index);
                byte x = blockPos[0];
                byte y = blockPos[1];
                byte z = blockPos[2];

                BlockPosition blockPosition = new BlockPosition(x + 16 * chunkX, y, z + 16 * chunkZ);
                Data data = getData(index);
                customBlock.update(instance, blockPosition, data);
            }
        }
    }

    public Biome getBiome() {
        return biome;
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
        dos.writeByte(biome.getId());

        for (byte x = 0; x < CHUNK_SIZE_X; x++) {
            for (short y = 0; y < CHUNK_SIZE_Y; y++) {
                for (byte z = 0; z < CHUNK_SIZE_Z; z++) {
                    int index = SerializerUtils.coordToChunkIndex(x, y, z);

                    short blockId = getBlockId(x, y, z);
                    short customBlockId = getCustomBlockId(x, y, z);
                    boolean isCustomBlock = customBlockId != 0;
                    short id = isCustomBlock ? customBlockId : blockId;

                    if (id == 0)
                        continue;

                    Data data = blocksData.get(index);
                    boolean hasData = data != null;

                    // Chunk coord
                    dos.writeInt(x);
                    dos.writeInt(y);
                    dos.writeInt(z);

                    dos.writeBoolean(isCustomBlock); // Determine the type of the ID
                    dos.writeShort(id);

                    dos.writeBoolean(hasData);
                    if (hasData) {
                        byte[] d = data.getSerializedData();
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
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.chunk = this;
        fullDataPacket.fullChunk = true;
        return fullDataPacket;
    }

    public ChunkDataPacket getFreshPartialDataPacket() {
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.chunk = this;
        fullDataPacket.fullChunk = false;
        return fullDataPacket;
    }

    // Write the packet in the current thread
    public void refreshDataPacket() {
        ByteBuf buffer = PacketUtils.writePacket(getFreshFullDataPacket());
        setFullDataPacket(buffer);
    }

    // Write the pakcet in the writer thread pools
    public void refreshDataPacket(Runnable runnable) {
        PacketWriterUtils.writeCallbackPacket(getFreshFullDataPacket(), buf -> {
            setFullDataPacket(buf);
            runnable.run();
        });
    }

    @Override
    public String toString() {
        return "Chunk[" + chunkX + ":" + chunkZ + "]";
    }

    // UNSAFE
    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
    }

    // UNSAFE
    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    private int getBlockIndex(int x, int y, int z) {
        x = x % Chunk.CHUNK_SIZE_X;
        z = z % Chunk.CHUNK_SIZE_Z;

        x = x < 0 ? Chunk.CHUNK_SIZE_X + x : x;
        z = z < 0 ? Chunk.CHUNK_SIZE_Z + z : z;

        int index = (((y * 16) + x) * 16) + z;
        return index;
    }
}