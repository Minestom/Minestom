package fr.themode.minestom.instance;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk {

    private static final int CHUNK_SIZE = 16 * 256 * 16;

    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    private int chunkX, chunkZ;
    private Biome biome;
    private short[] blocksId = new short[CHUNK_SIZE];
    private String[] customBlocks = new String[CHUNK_SIZE];

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    protected void setBlock(byte x, byte y, byte z, short blockId) {
        int index = getIndex(x, y, z);
        this.blocksId[index] = blockId;
        if (blockId == 0) {
            this.customBlocks[index] = null;
        }
    }

    protected void setBlock(byte x, byte y, byte z, String blockId) {
        int index = getIndex(x, y, z);
        CustomBlock customBlock = Main.getBlockManager().getBlock(blockId);
        this.blocksId[index] = customBlock.getType();
        this.customBlocks[index] = blockId;
    }

    public short getBlockId(byte x, byte y, byte z) {
        return this.blocksId[getIndex(x, y, z)];
    }

    public String getCustomBlockId(byte x, byte y, byte z) {
        return this.customBlocks[getIndex(x, y, z)];
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

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    private int getIndex(byte x, byte y, byte z) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return index & 0xffff;
    }
}
