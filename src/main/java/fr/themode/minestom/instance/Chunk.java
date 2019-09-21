package fr.themode.minestom.instance;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.instance.block.UpdateConsumer;
import fr.themode.minestom.instance.block.UpdateOption;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.PacketUtils;
import fr.themode.minestom.utils.SerializerUtils;
import fr.themode.minestom.utils.time.CooldownUtils;
import it.unimi.dsi.fastutil.ints.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk implements Viewable {

    private static final BlockManager BLOCK_MANAGER = Main.getBlockManager();

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
    // TODO shouldn't take Data object (too much memory overhead)
    private Int2ObjectMap<Data> blocksData = new Int2ObjectOpenHashMap<>(16 * 16); // Start with the size of a single row

    // Contains CustomBlocks' index which are updatable
    private IntSet updatableBlocks = new IntOpenHashSet();
    // (block index)/(last update in ms)
    private Int2LongMap updatableBlocksLastUpdate = new Int2LongOpenHashMap();

    protected volatile boolean packetUpdated;

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // TODO blocks update

    // Cache
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Packet fullDataPacket;

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void UNSAFE_setBlock(byte x, byte y, byte z, short blockId, Data data) {
        setBlock(x, y, z, blockId, (short) 0, data, null);
    }

    public void UNSAFE_setBlock(byte x, byte y, byte z, short blockId) {
        UNSAFE_setBlock(x, y, z, blockId, null);
    }

    public void UNSAFE_setCustomBlock(byte x, byte y, byte z, short customBlockId, Data data) {
        CustomBlock customBlock = BLOCK_MANAGER.getBlock(customBlockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The custom block " + customBlockId + " does not exist or isn't registered");

        setCustomBlock(x, y, z, customBlock, data);
    }

    public void UNSAFE_setCustomBlock(byte x, byte y, byte z, short customBlockId) {
        UNSAFE_setCustomBlock(x, y, z, customBlockId, null);
    }

    private void setCustomBlock(byte x, byte y, byte z, CustomBlock customBlock, Data data) {
        UpdateConsumer updateConsumer = customBlock.hasUpdate() ? customBlock::update : null;
        setBlock(x, y, z, customBlock.getType(), customBlock.getId(), data, updateConsumer);
    }

    private void setBlock(byte x, byte y, byte z, short blockType, short customId, Data data, UpdateConsumer updateConsumer) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        if (blockType != 0 || customId != 0) {
            int value = (blockType << 16 | customId & 0xFFFF); // Merge blockType and customId to one unique Integer (16/16)
            this.blocks.put(index, value);
        } else {
            // Block has been deleted
            this.blocks.remove(index);
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

        if (isBlockEntity(blockType)) {
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

    private CustomBlock getCustomBlock(int index) {
        int value = getBlockValue(index);
        short id = (short) (value & 0xffff);
        return id != 0 ? BLOCK_MANAGER.getBlock(id) : null;
    }

    private int getBlockValue(int index) {
        return blocks.get(index);
    }

    public Data getData(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        return getData(index);
    }

    private Data getData(int index) {
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
                byte[] blockPos = SerializerUtils.indexToChunkPosition(index);
                byte x = blockPos[0];
                byte y = blockPos[1];
                byte z = blockPos[2];
                CustomBlock customBlock = getCustomBlock(x, y, z);

                // Update cooldown
                UpdateOption updateOption = customBlock.getUpdateOption();
                long lastUpdate = updatableBlocksLastUpdate.get(index);
                boolean shouldUpdate = !CooldownUtils.hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
                if (!shouldUpdate)
                    continue;

                this.updatableBlocksLastUpdate.put(index, time); // Refresh last update time
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

    public Packet getFullDataPacket() {
        return fullDataPacket;
    }

    private boolean isBlockEntity(short blockId) {
        // TODO complete
        return blockId == 2033;
    }

    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    public void setFullDataPacket(Packet fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
        this.packetUpdated = true;
    }

    protected byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeByte(biome.getId());

        // TODO customblock id map (StringId -> short id)
        // TODO List of (sectionId;blockcount;blocktype;blockarray)
        // TODO block data
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
        PacketUtils.writePacket(getFreshFullDataPacket(), packet -> {
            setFullDataPacket(packet);
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
}
