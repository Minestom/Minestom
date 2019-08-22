package fr.themode.minestom.instance;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk {

    private static final int CHUNK_SIZE = 16 * 256 * 16;

    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<Player> players = new CopyOnWriteArraySet<>();

    private Biome biome;
    private int chunkX, chunkZ;
    private short[] blocksId = new short[CHUNK_SIZE];
    private short[] customBlocks = new short[CHUNK_SIZE];

    // Block entities
    private Set<Integer> blockEntities = new CopyOnWriteArraySet<>();

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
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

    private void setBlock(byte x, byte y, byte z, short blockType, short customId) {
        int index = getIndex(x, y, z);
        this.blocksId[index] = blockType;
        this.customBlocks[index] = customId;
        if (isBlockEntity(blockType)) {
            blockEntities.add(index);
        } else {
            blockEntities.remove(index);
        }
    }

    public short getBlockId(byte x, byte y, byte z) {
        return this.blocksId[getIndex(x, y, z)];
    }

    public CustomBlock getCustomBlock(byte x, byte y, byte z) {
        short id = this.customBlocks[getIndex(x, y, z)];
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

    private boolean isBlockEntity(short blockId) {
        // TODO complete
        return blockId == 2033;
    }

    public Set<Integer> getBlockEntities() {
        return blockEntities;
    }

    private int getIndex(byte x, byte y, byte z) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return index & 0xffff;
    }
}
