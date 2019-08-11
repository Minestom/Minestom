package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.GroupedCollections;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Instance {

    private int id;

    private Set<Chunk> chunksSet = Collections.synchronizedSet(new HashSet<>());

    public Instance(int id) {
        this.id = id;
    }

    // TODO BlockBatch with pool
    public synchronized void setBlock(int x, int y, int z, Block block) {
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = new Chunk(Biome.VOID, chunkX, chunkZ);
            this.chunksSet.add(chunk);
        }
        synchronized (chunk) {
            chunk.setBlock(x % 16, y, z % 16, block);
        }
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        for (Chunk chunk : getChunks()) {
            if (chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                return chunk;
        }
        return null;
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    public Set<Chunk> getChunks() {
        return Collections.unmodifiableSet(chunksSet);
    }

    public void addEntity(Entity entity) {
        Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity);
        }

        if (entity instanceof EntityCreature) {
            // TODO based on distance with player
            getPlayers().forEach(p -> ((EntityCreature) entity).addViewer(p));
        } else if (entity instanceof Player) {
            getCreatures().forEach(entityCreature -> entityCreature.addViewer((Player) entity));
        }

        Chunk chunk = getChunkAt(entity.getX(), entity.getZ());
        chunk.addEntity(entity);
    }

    public void removeEntity(Entity entity) {
        Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        if (entity instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) entity;
            creature.getViewers().forEach(p -> creature.removeViewer(p));
        }

        Chunk chunk = getChunkAt(entity.getX(), entity.getZ());
        chunk.removeEntity(entity);
    }

    public GroupedCollections<EntityCreature> getCreatures() {
        GroupedCollections<EntityCreature> creatures = new GroupedCollections();
        for (Chunk chunk : getChunks()) {
            creatures.addCollection(chunk.creatures);
        }
        return creatures;
    }

    public GroupedCollections<Player> getPlayers() {
        GroupedCollections<Player> players = new GroupedCollections();
        for (Chunk chunk : getChunks()) {
            players.addCollection(chunk.players);
        }
        return players;
    }

    public int getId() {
        return id;
    }

    private Chunk createChunk(int chunkX, int chunkZ) {
        Chunk chunk = new Chunk(Biome.VOID, chunkX, chunkZ);
        this.chunksSet.add(chunk);
        return chunk;
    }
}
