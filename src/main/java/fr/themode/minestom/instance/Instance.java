package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.utils.GroupedCollections;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Instance {

    private UUID uniqueId;

    private GroupedCollections<EntityCreature> creatures = new GroupedCollections<>(new CopyOnWriteArrayList());
    private GroupedCollections<Player> players = new GroupedCollections<>(new CopyOnWriteArrayList());

    private Set<Chunk> chunksSet = new CopyOnWriteArraySet<>();

    public Instance(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    // TODO BlockBatch with pool
    public synchronized void setBlock(int x, int y, int z, short blockId) {
        final int chunkX = Math.floorDiv(x, 16);
        final int chunkZ = Math.floorDiv(z, 16);
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = createChunk(Biome.VOID, chunkX, chunkZ);
        }
        synchronized (chunk) {
            chunk.setBlock(x % 16, y, z % 16, blockId);
            sendChunkUpdate(chunk);
        }
    }

    public BlockBatch createBlockBatch() {
        return new BlockBatch(this);
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
            // TODO based on distance with players
            getPlayers().forEach(p -> ((EntityCreature) entity).addViewer(p));
        } else if (entity instanceof Player) {
            Player player = (Player) entity;
            sendChunks(player);
            getCreatures().forEach(entityCreature -> entityCreature.addViewer(player));
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
        return creatures;
    }

    public GroupedCollections<Player> getPlayers() {
        return players;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    protected Chunk createChunk(Biome biome, int chunkX, int chunkZ) {
        Chunk chunk = new Chunk(biome, chunkX, chunkZ);
        this.creatures.addCollection(chunk.creatures);
        this.players.addCollection(chunk.players);
        this.chunksSet.add(chunk);
        return chunk;
    }

    protected void sendChunkUpdate(Chunk chunk) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = false; // TODO partial chunk data
        chunkDataPacket.chunk = chunk;
        getPlayers().forEach(player -> player.getPlayerConnection().sendPacket(chunkDataPacket));
    }

    private void sendChunks(Player player) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = true;
        for (Chunk chunk : getChunks()) {
            chunkDataPacket.chunk = chunk;
            player.getPlayerConnection().sendPacket(chunkDataPacket);
        }
    }
}
