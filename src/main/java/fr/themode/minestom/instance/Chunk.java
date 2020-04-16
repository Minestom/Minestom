package fr.themode.minestom.instance;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.instance.block.UpdateConsumer;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.PacketUtils;
import fr.themode.minestom.utils.SerializerUtils;
import fr.themode.minestom.utils.time.CooldownUtils;
import fr.themode.minestom.utils.time.UpdateOption;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.*;

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

    private Biome biome;
    private int chunkX, chunkZ;

    // Int represent the chunk coord of the block
    // value is: 2 bytes -> blockId | 2 bytes -> customBlockId (filled with 0 if isn't)
    private Int2IntMap blocks = new Int2IntOpenHashMap(16 * 16 * 16); // Start with the size of a full chunk section

    // Used to get all blocks with data (no null)
    // Key is still chunk coord
    // FIXME: shouldn't take Data object (too much memory overhead)
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

    public void UNSAFE_setBlock(int index, short blockId, Data data) {
        setBlock(index, blockId, (short) 0, data, null);
    }

    public void UNSAFE_setBlock(int index, short blockId) {
        UNSAFE_setBlock(index, blockId, null);
    }

    public void UNSAFE_setCustomBlock(int index, short customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getBlock(customBlockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The custom block " + customBlockId + " does not exist or isn't registered");

        setCustomBlock(index, customBlock, data);
    }

    public void UNSAFE_setCustomBlock(int index, short customBlockId) {
        UNSAFE_setCustomBlock(index, customBlockId, null);
    }

    private void setCustomBlock(int index, CustomBlock customBlock, Data data) {
        UpdateConsumer updateConsumer = customBlock.hasUpdate() ? customBlock::update : null;
        setBlock(index, customBlock.getType(), customBlock.getId(), data, updateConsumer);
    }

    private void setBlock(int index, short blockId, short customId, Data data, UpdateConsumer updateConsumer) {
        if (blockId != 0
                || (blockId == 0 && customId != 0 && updateConsumer != null)) { // Allow custom air block for update purpose, refused if no update consumer has been found
            refreshBlockValue(index, blockId, customId);
        } else {
            // Block has been deleted, clear cache and return

            this.blocks.remove(index);

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

    public void setBlockData(byte x, byte y, byte z, Data data) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        if (data != null) {
            this.blocksData.put(index, data);
        } else {
            this.blocksData.remove(index);
        }
    }

    public short getBlockId(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        int value = getBlockValue(index);
        return (short) (value >>> 16);
    }

    public CustomBlock getCustomBlock(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        return getCustomBlock(index);
    }

    protected CustomBlock getCustomBlock(int index) {
        int value = getBlockValue(index);
        short id = (short) (value & 0xffff);
        return id != 0 ? BLOCK_MANAGER.getBlock(id) : null;
    }

    protected void refreshBlockValue(int index, short blockId, short customId) {
        int value = createBlockValue(blockId, customId);
        this.blocks.put(index, value);
    }

    protected void refreshBlockValue(int index, short blockId) {
        CustomBlock customBlock = getCustomBlock(index);
        short customBlockId = customBlock == null ? 0 : customBlock.getId();
        refreshBlockValue(index, blockId, customBlockId);
    }

    public int createBlockValue(short blockId, short customId) {
        // Merge blockType and customId to one unique Integer (16/16 bits)
        int value = (blockId << 16 | customId & 0xFFFF);
        return value;
    }

    private int getBlockValue(int index) {
        return blocks.getOrDefault(index, 0);
    }

    public Data getData(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
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
        // TODO complete
        return blockId == 2033;
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

        for (Int2IntMap.Entry entry : blocks.int2IntEntrySet()) {
            int index = entry.getIntKey();
            int value = entry.getIntValue();

            short blockId = (short) (value >>> 16);
            short customBlockId = (short) (value & 0xffff);
            boolean isCustomBlock = customBlockId != 0;
            short id = isCustomBlock ? customBlockId : blockId;

            Data data = blocksData.get(index);
            boolean hasData = data != null;

            dos.writeInt(index); // Chunk coord
            dos.writeBoolean(isCustomBlock); // Determine the type of the ID
            dos.writeShort(id);

            dos.writeBoolean(hasData);
            if (hasData) {
                byte[] d = data.getSerializedData();
                dos.writeInt(d.length);
                dos.write(d);
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
}