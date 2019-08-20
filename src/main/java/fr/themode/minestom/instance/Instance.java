package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.BlockBreakEvent;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.net.packet.server.play.DestroyEntitiesPacket;
import fr.themode.minestom.net.packet.server.play.ParticlePacket;
import fr.themode.minestom.utils.GroupedCollections;
import fr.themode.minestom.utils.Position;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Instance implements BlockModifier {

    private UUID uniqueId;

    private GroupedCollections<ObjectEntity> objectEntities = new GroupedCollections<>(new CopyOnWriteArrayList<>());
    private GroupedCollections<EntityCreature> creatures = new GroupedCollections<>(new CopyOnWriteArrayList());
    private GroupedCollections<Player> players = new GroupedCollections<>(new CopyOnWriteArrayList());

    private ChunkGenerator chunkGenerator;
    private Map<Long, Chunk> chunks = new ConcurrentHashMap<>();

    public Instance(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, short blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            sendChunkUpdate(chunk);
        }
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, String blockId) {
        Chunk chunk = getChunkAt(x, z);
        synchronized (chunk) {
            chunk.setBlock((byte) (x % 16), (byte) y, (byte) (z % 16), blockId);
            sendChunkUpdate(chunk);
        }
    }

    public void breakBlock(Player player, Position position, short blockId) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(position);
        player.callEvent(BlockBreakEvent.class, blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            int x = position.getX();
            int y = position.getY();
            int z = position.getZ();
            setBlock(x, y, z, (short) 0);
            ParticlePacket particlePacket = new ParticlePacket(); // TODO change by a proper particle API
            particlePacket.particleId = 3; // Block particle
            particlePacket.longDistance = false;
            particlePacket.x = x + 0.5f;
            particlePacket.y = y;
            particlePacket.z = z + 0.5f;
            particlePacket.offsetX = 0.4f;
            particlePacket.offsetY = 0.65f;
            particlePacket.offsetZ = 0.4f;
            particlePacket.particleData = 0.3f;
            particlePacket.particleCount = 75;
            particlePacket.blockId = blockId;
            player.getPlayerConnection().sendPacket(particlePacket);
            player.sendPacketToViewers(particlePacket);
        } else {
            sendChunkUpdate(player, getChunkAt(position));
        }
    }

    public void breakBlock(Player player, Position position, CustomBlock customBlock) {
        breakBlock(player, position, customBlock.getType());
    }

    public Chunk loadChunk(int chunkX, int chunkZ) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        return chunk == null ? createChunk(chunkX, chunkZ) : chunk; // TODO load from file
    }

    public short getBlockId(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getBlockId((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public short getBlockId(Position position) {
        return getBlockId(position.getX(), position.getY(), position.getZ());
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getCustomBlock((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public BlockBatch createBlockBatch() {
        return new BlockBatch(this);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.getOrDefault(getChunkKey(chunkX, chunkZ), null);
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    public Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public Collection<Chunk> getChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }

    public void addEntity(Entity entity) {
        Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity);
        }

        // TODO based on distance with players
        getPlayers().forEach(p -> entity.addViewer(p));

        if (entity instanceof Player) {
            Player player = (Player) entity;
            sendChunks(player);
            getCreatures().forEach(entityCreature -> entityCreature.addViewer(player));
            getPlayers().forEach(p -> p.addViewer(player));
        }

        Chunk chunk = getChunkAt(entity.getX(), entity.getZ());
        chunk.addEntity(entity);
    }

    public void removeEntity(Entity entity) {
        Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        entity.getViewers().forEach(p -> entity.removeViewer(p));

        if (!(entity instanceof Player)) {
            DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
            destroyEntitiesPacket.entityIds = new int[]{entity.getEntityId()};

            entity.getViewers().forEach(p -> p.getPlayerConnection().sendPacket(destroyEntitiesPacket)); // TODO destroy batch
        }

        Chunk chunk = getChunkAt(entity.getX(), entity.getZ());
        chunk.removeEntity(entity);
    }

    public GroupedCollections<ObjectEntity> getObjectEntities() {
        return objectEntities;
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

    public void sendChunkUpdate(Player player, Chunk chunk) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = false;
        chunkDataPacket.chunk = chunk;
        player.getPlayerConnection().sendPacket(chunkDataPacket);
    }

    protected Chunk createChunk(int chunkX, int chunkZ) {
        Biome biome = chunkGenerator != null ? chunkGenerator.getBiome(chunkX, chunkZ) : Biome.VOID;
        Chunk chunk = new Chunk(biome, chunkX, chunkZ);
        this.objectEntities.addCollection(chunk.objectEntities);
        this.creatures.addCollection(chunk.creatures);
        this.players.addCollection(chunk.players);
        this.chunks.put(getChunkKey(chunkX, chunkZ), chunk);
        if (chunkGenerator != null) {
            ChunkBatch chunkBatch = createChunkBatch(chunk);
            chunkGenerator.generateChunkData(chunkBatch, chunkX, chunkZ);
            chunkBatch.flush();
        }
        return chunk;
    }

    protected ChunkBatch createChunkBatch(Chunk chunk) {
        return new ChunkBatch(this, chunk);
    }

    protected void sendChunkUpdate(Chunk chunk) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = false;
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

    private long getChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }
}
