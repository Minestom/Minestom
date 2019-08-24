package fr.themode.minestom.instance;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.ObjectEntity;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.DestroyEntitiesPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class Instance implements BlockModifier {

    protected static final ChunkLoaderIO CHUNK_LOADER_IO = new ChunkLoaderIO();
    // Entities present in this instance
    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    // Entities per chunk
    protected Map<Long, Set<Entity>> chunkEntities = new ConcurrentHashMap<>();
    private UUID uniqueId;

    protected Instance(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public abstract void breakBlock(Player player, BlockPosition blockPosition, short blockId);

    public abstract void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract Chunk getChunk(int chunkX, int chunkZ);

    public abstract void saveToFolder(Runnable callback);

    public abstract BlockBatch createBlockBatch();

    public abstract ChunkBatch createChunkBatch(Chunk chunk);

    public abstract void setChunkGenerator(ChunkGenerator chunkGenerator);

    public abstract Collection<Chunk> getChunks();

    public abstract File getFolder();

    public abstract void setFolder(File folder);

    public abstract void sendChunkUpdate(Player player, Chunk chunk);

    public abstract void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void sendChunks(Player player);

    //
    protected void sendChunkUpdate(Collection<Player> players, Chunk chunk) {
        Buffer chunkData = chunk.getFullDataPacket();
        chunkData.getData().retain(players.size()).markReaderIndex();
        players.forEach(player -> {
            player.getPlayerConnection().sendUnencodedPacket(chunkData);
            chunkData.getData().resetReaderIndex();
        });
    }
    //

    public Set<ObjectEntity> getObjectEntities() {
        return Collections.unmodifiableSet(objectEntities);
    }

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<Entity> getChunkEntities(Chunk chunk) {
        return Collections.unmodifiableSet(getEntitiesInChunk(getChunkIndex(chunk.getChunkX(), chunk.getChunkZ())));
    }

    public void breakBlock(Player player, BlockPosition blockPosition, CustomBlock customBlock) {
        breakBlock(player, blockPosition, customBlock.getType());
    }

    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    public void loadChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = Math.floorDiv((int) position.getX(), 16);
        int chunkZ = Math.floorDiv((int) position.getY(), 16);
        loadChunk(chunkX, chunkZ, callback);
    }

    public short getBlockId(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getBlockId((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public short getBlockId(BlockPosition blockPosition) {
        return getBlockId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getCustomBlock((byte) (x % 16), (byte) y, (byte) (z % 16));
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = Math.floorDiv((int) x, 16);
        int chunkZ = Math.floorDiv((int) z, 16);
        return getChunk(chunkX, chunkZ);
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    public Chunk getChunkAt(BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    public Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    public void saveToFolder() {
        saveToFolder(null);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    protected long getChunkIndex(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    // UNSAFE METHODS (need most of time to be synchronized)

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
            getObjectEntities().forEach(objectEntity -> objectEntity.addViewer(player));
            getCreatures().forEach(entityCreature -> entityCreature.addViewer(player));
            getPlayers().forEach(p -> p.addViewer(player));
        }

        Chunk chunk = getChunkAt(entity.getPosition());
        addEntityToChunk(entity, chunk);
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
        } else {
            // TODO optimize (cache all entities that the player see)
            Player player = (Player) entity;
            getObjectEntities().forEach(objectEntity -> objectEntity.removeViewer(player));
            getCreatures().forEach(entityCreature -> entityCreature.removeViewer(player));
            getPlayers().forEach(p -> p.removeViewer(player));

        }

        Chunk chunk = getChunkAt(entity.getPosition());
        removeEntityFromChunk(entity, chunk);
    }

    public void addEntityToChunk(Entity entity, Chunk chunk) {
        long chunkIndex = getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        Set<Entity> entities = getEntitiesInChunk(chunkIndex);
        entities.add(entity);
        this.chunkEntities.put(chunkIndex, entities);
        if (entity instanceof Player) {
            this.players.add((Player) entity);
        } else if (entity instanceof EntityCreature) {
            this.creatures.add((EntityCreature) entity);
        } else if (entity instanceof ObjectEntity) {
            this.objectEntities.add((ObjectEntity) entity);
        }
    }

    public void removeEntityFromChunk(Entity entity, Chunk chunk) {
        long chunkIndex = getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        Set<Entity> entities = getEntitiesInChunk(chunkIndex);
        entities.remove(entity);
        this.chunkEntities.put(chunkIndex, entities);
        if (entity instanceof Player) {
            this.players.remove(entity);
        } else if (entity instanceof EntityCreature) {
            this.creatures.remove(entity);
        } else if (entity instanceof ObjectEntity) {
            this.objectEntities.remove(entity);
        }
    }

    private Set<Entity> getEntitiesInChunk(long index) {
        return chunkEntities.getOrDefault(index, new CopyOnWriteArraySet<>());
    }
}
