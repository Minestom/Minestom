package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.PacketUtils;
import fr.themode.minestom.utils.SerializerUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk {

    private static final int CHUNK_SIZE_X = 16;
    private static final int CHUNK_SIZE_Y = 256;
    private static final int CHUNK_SIZE_Z = 16;
    private static final int CHUNK_SIZE = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z;

    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<Player> players = new CopyOnWriteArraySet<>();

    private Biome biome;
    private int chunkX, chunkZ;
    private short[] blocksId = new short[CHUNK_SIZE];
    private short[] customBlocks = new short[CHUNK_SIZE];

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    // Cache
    private Buffer fullDataPacket;

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        //refreshDataPacket(); // TODO remove
    }

    protected void setBlock(byte x, byte y, byte z, short blockId) {
        setBlock(x, y, z, blockId, (short) 0);
    }

    protected void setBlock(byte x, byte y, byte z, String blockId) {
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

    public void addEntity(Entity entity) {
        if (entity instanceof Player) {
            synchronized (players) {
                if (this.players.contains(entity))
                    return;
                this.players.add((Player) entity);
            }
        } else if (entity instanceof EntityCreature) {
            synchronized (creatures) {
                if (this.creatures.contains(entity))
                    return;
                this.creatures.add((EntityCreature) entity);
            }
        } else if (entity instanceof ObjectEntity) {
            synchronized (objectEntities) {
                if (this.objectEntities.contains(entity))
                    return;
                this.objectEntities.add((ObjectEntity) entity);
            }
        }
    }

    public void removeEntity(Entity entity) {
        if (entity instanceof Player) {
            synchronized (players) {
                this.players.remove(entity);
            }
        } else if (entity instanceof EntityCreature) {
            synchronized (creatures) {
                this.creatures.remove(entity);
            }
        } else if (entity instanceof ObjectEntity) {
            synchronized (objectEntities) {
                this.objectEntities.remove(entity);
            }
        }
    }

    public short[] getBlocksId() {
        return blocksId;
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

    public Set<ObjectEntity> getObjectEntities() {
        return Collections.unmodifiableSet(objectEntities);
    }

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
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

    protected void loadFromFile(File file) throws IOException {
        System.out.println("LOAD FROM FILE");
        byte[] array = Files.readAllBytes(file.toPath());
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(array));
        this.chunkX = stream.readInt();
        this.chunkZ = stream.readInt();
        System.out.println("chunk: " + chunkX + " : " + chunkZ);
        try {
            while (true) {
                int index = stream.readInt();
                boolean isCustomBlock = stream.readBoolean();
                short block = stream.readShort();
            }
        } catch (EOFException e) {
            System.out.println("END");
        }

    }

    protected ChunkDataPacket getFreshFullDataPacket() {
        ChunkDataPacket fullDataPacket = new ChunkDataPacket();
        fullDataPacket.chunk = this;
        fullDataPacket.fullChunk = true;
        return fullDataPacket;
    }

    protected void refreshDataPacket() {
        Packet packet = PacketUtils.writePacket(getFreshFullDataPacket());
        this.fullDataPacket = PacketUtils.encode(packet); // TODO write packet buffer in another thread (heavy calculations)
    }
}
