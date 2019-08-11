package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chunk {

    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    private int chunkX, chunkZ;
    private Biome biome;
    private HashMap<Short, Block> blocks = new HashMap<>();
    private ChunkDataPacket fullChunkPacket;

    public Chunk(Biome biome, int chunkX, int chunkZ) {
        this.biome = biome;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public void setBlock(int x, int y, int z, Block block) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        this.blocks.put(index, block);
    }

    public Block getBlock(int x, int y, int z) {
        short index = (short) (x & 0x000F);
        index |= (y << 4) & 0x0FF0;
        index |= (z << 12) & 0xF000;
        return this.blocks.getOrDefault(index, new Block(0));
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

    public HashMap<Short, Block> getBlocks() {
        return blocks;
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

    private void refreshFullChunkPacket() {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = true;
        chunkDataPacket.chunk = this;
        // TODO fill buffer
    }

    public ChunkDataPacket getFullChunkPacket() {
        return fullChunkPacket;
    }
}
