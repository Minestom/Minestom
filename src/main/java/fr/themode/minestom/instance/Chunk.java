package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.PacketUtils;
import fr.themode.minestom.utils.SerializerUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk implements Viewable {

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SIZE = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z;

    private Biome biome;
    private int chunkX, chunkZ;
    private short[] blocksId = new short[CHUNK_SIZE];
    private short[] customBlocks = new short[CHUNK_SIZE];

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // Cache
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Buffer fullDataPacket;

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    protected void setBlock(byte x, byte y, byte z, short blockId) {
        setBlock(x, y, z, blockId, (short) 0);
    }

    protected void setCustomBlock(byte x, byte y, byte z, String blockId) {
        CustomBlock customBlock = Main.getBlockManager().getBlock(blockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The block " + blockId + " does not exist or isn't registered");

        setBlock(x, y, z, customBlock.getType(), customBlock.getId());
    }

    protected void setCustomBlock(byte x, byte y, byte z, short customBlockId) {
        CustomBlock customBlock = Main.getBlockManager().getBlock(customBlockId);
        if (customBlock == null)
            throw new IllegalArgumentException("The custom block " + customBlockId + " does not exist or isn't registered");

        setBlock(x, y, z, customBlock.getType(), customBlockId);
    }

    private void setBlock(byte x, byte y, byte z, short blockType, short customId) {
        int index = SerializerUtils.chunkCoordToIndex(x, y, z);
        this.blocksId[index] = blockType;
        this.customBlocks[index] = customId;
        if (isBlockEntity(blockType)) {
            blockEntities.add(index);
        } else {
            blockEntities.remove(index);
        }
    }

    public short getBlockId(byte x, byte y, byte z) {
        return this.blocksId[SerializerUtils.chunkCoordToIndex(x, y, z)];
    }

    public CustomBlock getCustomBlock(byte x, byte y, byte z) {
        short id = this.customBlocks[SerializerUtils.chunkCoordToIndex(x, y, z)];
        return id != 0 ? Main.getBlockManager().getBlock(id) : null;
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

    public Buffer getFullDataPacket() {
        return fullDataPacket;
    }

    private boolean isBlockEntity(short blockId) {
        // TODO complete
        return blockId == 2033;
    }

    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    public void setFullDataPacket(Buffer fullDataPacket) {
        this.fullDataPacket = fullDataPacket;
    }

    protected byte[] getSerializedData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeByte(biome.getId());

        // TODO customblock id map (StringId -> short id)
        // TODO List of (sectionId;blockcount;blocktype;blockarray)
        // TODO block data
        for (byte x = 0; x < CHUNK_SIZE_X; x++) {
            for (byte y = -128; y < 127; y++) {
                for (byte z = 0; z < CHUNK_SIZE_Z; z++) {
                    int index = SerializerUtils.chunkCoordToIndex(x, y, z);
                    boolean isCustomBlock = customBlocks[index] != 0;
                    short id = isCustomBlock ? customBlocks[index] : blocksId[index];
                    if (id != 0) {
                        dos.writeInt(index); // Correspond to chunk coord
                        dos.writeBoolean(isCustomBlock);
                        dos.writeShort(id);
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

    protected void refreshDataPacket() {
        Packet packet = PacketUtils.writePacket(getFreshFullDataPacket());
        this.fullDataPacket = PacketUtils.encode(packet); // TODO write packet buffer in another thread (heavy calculations)
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
