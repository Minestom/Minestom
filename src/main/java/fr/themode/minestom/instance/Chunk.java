package fr.themode.minestom.instance;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.PacketUtils;
import fr.themode.minestom.utils.SerializerUtils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class Chunk implements Viewable {

    private static final BlockManager BLOCK_MANAGER = Main.getBlockManager();

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;

    private Biome biome;
    private int chunkX, chunkZ;
    protected volatile boolean packetUpdated;

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // TODO blocks update

    // Cache
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Packet fullDataPacket;
    // Int represent the chunk coord of the block
    // value is: 2 bytes -> blockId | 2 bytes -> customBlockId (filled with 0 if isn't)
    private Int2IntMap blocks = new Int2IntOpenHashMap(16 * 16 * 16); // Start with the size of a full chunk section

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void UNSAFE_setBlock(byte x, byte y, byte z, short blockId) {
        setBlock(x, y, z, blockId, (short) 0);
    }

    public void UNSAFE_setCustomBlock(byte x, byte y, byte z, short customBlockId) {
        CustomBlock customBlock = BLOCK_MANAGER.getBlock(customBlockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The custom block " + customBlockId + " does not exist or isn't registered");

        setCustomBlock(x, y, z, customBlock);
    }

    private void setBlock(byte x, byte y, byte z, short blockType, short customId) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        if (blockType != 0 || customId != 0) {
            int value = (blockType << 16 | customId & 0xFFFF);
            this.blocks.put(index, value);
        } else {
            // Block has been deleted
            this.blocks.remove(index);
        }

        if (isBlockEntity(blockType)) {
            this.blockEntities.add(index);
        } else {
            this.blockEntities.remove(index);
        }

        this.packetUpdated = false;
    }

    private void setCustomBlock(byte x, byte y, byte z, CustomBlock customBlock) {
        if (customBlock.hasUpdate()) {
            Consumer<Data> test = customBlock::update;
            // TODO add update callback
        }
        setBlock(x, y, z, customBlock.getType(), customBlock.getId());
    }

    public short getBlockId(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        int value = this.blocks.get(index);
        return (short) (value >>> 16);
    }

    public CustomBlock getCustomBlock(byte x, byte y, byte z) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        int value = this.blocks.get(index);
        short id = (short) (value & 0xffff);
        return id != 0 ? BLOCK_MANAGER.getBlock(id) : null;
    }

    public void updateBlocks() {
        /**
         * TODO blocks' update:
         *  - get all custom blocks
         *  - check if they have an update method
         *  - check if they should be updated
         *  - get custom block's data
         *  - call update method
         */
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

            dos.writeInt(index); // Chunk coord
            dos.writeBoolean(isCustomBlock); // Determine the type of the ID
            dos.writeShort(id);
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
