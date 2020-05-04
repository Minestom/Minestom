package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.*;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.ChunkUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.Dimension;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class Instance implements BlockModifier, DataContainer {

    protected static final ChunkLoader CHUNK_LOADER_IO = new ChunkLoader();
    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private Dimension dimension;

    // Entities present in this instance
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<ExperienceOrb> experienceOrbs = new CopyOnWriteArraySet<>();
    // Entities per chunk
    protected Map<Long, Set<Entity>> chunkEntities = new ConcurrentHashMap<>();
    private UUID uniqueId;

    private Data data;

    protected Instance(UUID uniqueId, Dimension dimension) {
        this.uniqueId = uniqueId;
        this.dimension = dimension;
    }

    public abstract void refreshBlockId(BlockPosition blockPosition, short blockId);

    // Used to call BlockBreakEvent and sending particle packet if true
    public abstract void breakBlock(Player player, BlockPosition blockPosition);

    // Force the generation of the chunk, even if no file and ChunkGenerator are defined
    public abstract void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    // Load only if auto chunk load is enabled
    public abstract void loadOptionalChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void unloadChunk(int chunkX, int chunkZ);

    public abstract Chunk getChunk(int chunkX, int chunkZ);

    public abstract void saveChunkToStorageFolder(Chunk chunk, Runnable callback);

    public abstract void saveChunksToStorageFolder(Runnable callback);

    public abstract BlockBatch createBlockBatch();

    public abstract ChunkBatch createChunkBatch(Chunk chunk);

    public abstract void setChunkGenerator(ChunkGenerator chunkGenerator);

    public abstract Collection<Chunk> getChunks();

    public abstract StorageFolder getStorageFolder();

    public abstract void setStorageFolder(StorageFolder storageFolder);

    public abstract void sendChunkUpdate(Player player, Chunk chunk);

    protected abstract void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    public abstract void sendChunks(Player player);

    public abstract void sendChunk(Player player, Chunk chunk);

    public abstract void enableAutoChunkLoad(boolean enable);

    public abstract boolean hasEnabledAutoChunkLoad();

    /**
     * Determines whether a position in the void. If true, entities should take damage and die.
     * Always returning false allow entities to survive in the void
     *
     * @param position the position in the world
     * @return true iif position is inside the void
     */
    public abstract boolean isInVoid(Position position);

    //
    protected void sendChunkUpdate(Collection<Player> players, Chunk chunk) {
        ByteBuf chunkData = chunk.getFullDataPacket();
        players.forEach(player -> {
            player.getPlayerConnection().sendPacket(chunkData);
        });
    }

    protected void sendChunkSectionUpdate(Chunk chunk, int section, Collection<Player> players) {
        PacketWriterUtils.writeAndSend(players, getChunkSectionUpdatePacket(chunk, section));
    }

    public void sendChunkSectionUpdate(Chunk chunk, int section, Player player) {
        PacketWriterUtils.writeAndSend(player, getChunkSectionUpdatePacket(chunk, section));
    }

    protected ChunkDataPacket getChunkSectionUpdatePacket(Chunk chunk, int section) {
        ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
        chunkDataPacket.fullChunk = false;
        chunkDataPacket.chunk = chunk;
        int[] sections = new int[16];
        sections[section] = 1;
        chunkDataPacket.sections = sections;
        return chunkDataPacket;
    }
    //


    public Dimension getDimension() {
        return dimension;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public Set<ObjectEntity> getObjectEntities() {
        return Collections.unmodifiableSet(objectEntities);
    }

    public Set<ExperienceOrb> getExperienceOrbs() {
        return Collections.unmodifiableSet(experienceOrbs);
    }

    public Set<Entity> getChunkEntities(Chunk chunk) {
        return Collections.unmodifiableSet(getEntitiesInChunk(ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ())));
    }

    public void refreshBlockId(int x, int y, int z, short blockId) {
        refreshBlockId(new BlockPosition(x, y, z), blockId);
    }

    public void refreshBlockId(int x, int y, int z, Block block) {
        refreshBlockId(x, y, z, block.getBlockId());
    }

    public void refreshBlockId(BlockPosition blockPosition, Block block) {
        refreshBlockId(blockPosition, block.getBlockId());
    }

    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    public void loadChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = ChunkUtils.getChunkCoordinate((int) position.getX());
        int chunkZ = ChunkUtils.getChunkCoordinate((int) position.getZ());
        loadChunk(chunkX, chunkZ, callback);
    }

    public void loadOptionalChunk(Position position, Consumer<Chunk> callback) {
        int chunkX = ChunkUtils.getChunkCoordinate((int) position.getX());
        int chunkZ = ChunkUtils.getChunkCoordinate((int) position.getZ());
        loadOptionalChunk(chunkX, chunkZ, callback);
    }

    public short getBlockId(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getBlockId(x, y, z);
    }

    public short getBlockId(float x, float y, float z) {
        return getBlockId(Math.round(x), Math.round(y), Math.round(z));
    }

    public short getBlockId(BlockPosition blockPosition) {
        return getBlockId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public CustomBlock getCustomBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getCustomBlock(x, y, z);
    }

    public CustomBlock getCustomBlock(BlockPosition blockPosition) {
        return getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public void sendBlockAction(BlockPosition blockPosition, byte actionId, byte actionParam) {
        short blockId = getBlockId(blockPosition);

        BlockActionPacket blockActionPacket = new BlockActionPacket();
        blockActionPacket.blockPosition = blockPosition;
        blockActionPacket.actionId = actionId;
        blockActionPacket.actionParam = actionParam;
        blockActionPacket.blockId = blockId; // FIXME: block id and not block state?

        Chunk chunk = getChunkAt(blockPosition);
        chunk.sendPacketToViewers(blockActionPacket);
    }

    public Data getBlockData(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, z);
        return chunk.getData((byte) x, (byte) y, (byte) z);
    }

    public Data getBlockData(BlockPosition blockPosition) {
        return getBlockData(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public Chunk getChunkAt(double x, double z) {
        int chunkX = ChunkUtils.getChunkCoordinate((int) x);
        int chunkZ = ChunkUtils.getChunkCoordinate((int) z);
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

    public void saveChunkToStorageFolder(Chunk chunk) {
        saveChunkToStorageFolder(chunk, null);
    }

    public void saveChunksToStorageFolder() {
        saveChunksToStorageFolder(null);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    // UNSAFE METHODS (need most of time to be synchronized)

    public void addEntity(Entity entity) {
        Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity); // If entity is in another instance, remove it from there and add it to this
        }

        long[] visibleChunksEntity = ChunkUtils.getChunksInRange(entity.getPosition(), MinecraftServer.ENTITY_VIEW_DISTANCE);
        boolean isPlayer = entity instanceof Player;

        if (isPlayer) {
            sendChunks((Player) entity);
        }

        // Send all visible entities
        for (long chunkIndex : visibleChunksEntity) {
            getEntitiesInChunk(chunkIndex).forEach(ent -> {
                if (isPlayer) {
                    ent.addViewer((Player) entity);
                }

                if (ent instanceof Player) {
                    entity.addViewer((Player) ent);
                }
            });
        }

        Chunk chunk = getChunkAt(entity.getPosition());
        addEntityToChunk(entity, chunk);
    }

    public void removeEntity(Entity entity) {
        Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        entity.getViewers().forEach(p -> entity.removeViewer(p)); // Remove this entity from players viewable list and send delete entities packet

        Chunk chunk = getChunkAt(entity.getPosition());
        removeEntityFromChunk(entity, chunk);
    }

    public void addEntityToChunk(Entity entity, Chunk chunk) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        synchronized (chunkEntities) {
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.add(entity);
            this.chunkEntities.put(chunkIndex, entities);
            if (entity instanceof Player) {
                this.players.add((Player) entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.add((EntityCreature) entity);
            } else if (entity instanceof ObjectEntity) {
                this.objectEntities.add((ObjectEntity) entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.add((ExperienceOrb) entity);
            }
        }
    }

    public void removeEntityFromChunk(Entity entity, Chunk chunk) {
        long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        synchronized (chunkEntities) {
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.remove(entity);
            if (entities.isEmpty()) {
                this.chunkEntities.remove(chunkIndex);
            } else {
                this.chunkEntities.put(chunkIndex, entities);
            }
            if (entity instanceof Player) {
                this.players.remove(entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.remove(entity);
            } else if (entity instanceof ObjectEntity) {
                this.objectEntities.remove(entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.remove(entity);
            }
        }
    }

    private Set<Entity> getEntitiesInChunk(long index) {
        Set<Entity> entities = chunkEntities.get(index);
        return entities != null ? entities : new CopyOnWriteArraySet<>();
    }

    /**
     * Schedule a block update at a given position.
     * Does nothing if no custom block is present at 'position'.
     * Cancelled if the block changes between this call and the actual update
     *
     * @param time     in how long this update must be performed?
     * @param unit     in what unit is the time expressed
     * @param position the location of the block to update
     */
    public abstract void scheduleUpdate(int time, TimeUnit unit, BlockPosition position);

    /**
     * Performs a single tick in the instance.
     * By default, does nothing
     * @param time the current time
     */
    public void tick(long time) { }
}