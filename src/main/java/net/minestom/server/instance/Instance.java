package net.minestom.server.instance;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.*;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Instances are what are called "worlds" in Minecraft
 * <p>
 * An instance has entities and chunks, each instance contains its own entity list but the
 * chunk implementation has to be defined, see {@link InstanceContainer}
 */
public abstract class Instance implements BlockModifier, EventHandler, DataContainer {

    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private DimensionType dimensionType;

    private WorldBorder worldBorder;

    // Tick since the creation of the instance
    private long worldAge;

    // The time of the instance
    private long time;
    private int timeRate = 1;
    private UpdateOption timeUpdate = new UpdateOption(1, TimeUnit.TICK);
    private long lastTimeUpdate;

    private Map<Class<? extends Event>, List<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();

    // Entities present in this instance
    protected Set<Player> players = new CopyOnWriteArraySet<>();
    protected Set<EntityCreature> creatures = new CopyOnWriteArraySet<>();
    protected Set<ObjectEntity> objectEntities = new CopyOnWriteArraySet<>();
    protected Set<ExperienceOrb> experienceOrbs = new CopyOnWriteArraySet<>();
    // Entities per chunk
    protected Long2ObjectMap<Set<Entity>> chunkEntities = new Long2ObjectOpenHashMap<>();
    protected UUID uniqueId;

    protected List<Consumer<Instance>> nextTick = Collections.synchronizedList(new ArrayList<>());

    private Data data;
    private ExplosionSupplier explosionSupplier;

    // Pathfinder
    private PFInstanceSpace instanceSpace = new PFInstanceSpace(this);

    public Instance(UUID uniqueId, DimensionType dimensionType) {
        this.uniqueId = uniqueId;
        this.dimensionType = dimensionType;

        this.worldBorder = new WorldBorder(this);
    }

    public void scheduleNextTick(Consumer<Instance> callback) {
        nextTick.add(callback);
    }

    /**
     * Used to change the id of the block in a specific position.
     * <p>
     * In case of a CustomBlock it does not remove it but only refresh its visual
     *
     * @param blockPosition the block position
     * @param blockId       the new block id
     */
    public abstract void refreshBlockId(BlockPosition blockPosition, short blockId);

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the player who break the block
     * @param blockPosition the position of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    public abstract boolean breakBlock(Player player, BlockPosition blockPosition);

    /**
     * Force the generation of the chunk, even if no file and ChunkGenerator are defined
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback consumer called after the chunk has been generated,
     *                 the returned chunk will never be null
     */
    public abstract void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    /**
     * Load the chunk if the chunk is already loaded or if
     * {@link #hasEnabledAutoChunkLoad()} returns true
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback consumer called after the chunk has tried to be loaded,
     *                 contains a chunk if it is successful, null otherwise
     */
    public abstract void loadOptionalChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    /**
     * Unload a chunk
     * <p>
     * WARNING: all entities other than {@link Player} will be removed
     *
     * @param chunk the chunk to unload
     */
    public abstract void unloadChunk(Chunk chunk);

    /**
     * Get the specified chunk
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk at the specified position, null if not loaded
     */
    public abstract Chunk getChunk(int chunkX, int chunkZ);

    /**
     * Save a chunk into the defined storage folder
     *
     * @param chunk    the chunk to save
     * @param callback called when the chunk is done saving
     * @throws NullPointerException if {@link #getStorageFolder()} returns null
     */
    public abstract void saveChunkToStorageFolder(Chunk chunk, Runnable callback);

    /**
     * Save multiple chunks into the defined storage folder
     *
     * @param callback called when the chunks are done saving
     * @throws NullPointerException if {@link #getStorageFolder()} returns null
     */
    public abstract void saveChunksToStorageFolder(Runnable callback);

    /**
     * Create a new block batch linked to this instance
     *
     * @return a BlockBatch linked to the instance
     */
    public abstract BlockBatch createBlockBatch();

    /**
     * Create a new chunk batch linked to this instance and the specified chunk
     *
     * @param chunk the chunk to modify
     * @return a ChunkBatch linked to {@code chunk}
     * @throws NullPointerException if {@code chunk} is null
     */
    public abstract ChunkBatch createChunkBatch(Chunk chunk);

    /**
     * Get the instance chunk generator
     *
     * @return the chunk generator of the instance
     */
    public abstract ChunkGenerator getChunkGenerator();

    /**
     * Change the instance chunk generator
     *
     * @param chunkGenerator the new chunk generator of the instance
     */
    public abstract void setChunkGenerator(ChunkGenerator chunkGenerator);

    /**
     * Get all the instance's chunks
     *
     * @return an unmodifiable containing all the loaded chunks of the instance
     */
    public abstract Collection<Chunk> getChunks();

    /**
     * Get the instance storage folder
     *
     * @return the storage folder of the instance
     */
    public abstract StorageFolder getStorageFolder();

    /**
     * Change the instance storage folder
     *
     * @param storageFolder the new storage folder of the instance
     */
    public abstract void setStorageFolder(StorageFolder storageFolder);

    protected abstract void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    protected abstract void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback);

    /**
     * Send all chunks data to {@code player}
     *
     * @param player the player
     */
    public abstract void sendChunks(Player player);

    /**
     * Send a specific chunk data to {@code player}
     *
     * @param player the player
     * @param chunk  the chunk
     */
    public abstract void sendChunk(Player player, Chunk chunk);

    /**
     * When set to true, chunks will load with players moving closer
     * Otherwise using {@link #loadChunk(int, int)} will be required to even spawn a player
     *
     * @param enable enable the auto chunk load
     */
    public abstract void enableAutoChunkLoad(boolean enable);

    /**
     * @return true if auto chunk load is enabled, false otherwise
     */
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

    /**
     * Send a full {@link ChunkDataPacket} to {@code player}
     *
     * @param player the player to update the chunk to
     * @param chunk  the chunk to send
     */
    public void sendChunkUpdate(Player player, Chunk chunk) {
        player.getPlayerConnection().sendPacket(chunk.getFullDataPacket(), true);
    }

    protected void sendChunkUpdate(Collection<Player> players, Chunk chunk) {
        final ByteBuf chunkData = chunk.getFullDataPacket();
        players.forEach(player -> {
            if (!PlayerUtils.isNettyClient(player))
                return;

            player.getPlayerConnection().sendPacket(chunkData, true);
        });
    }

    protected void sendChunkSectionUpdate(Chunk chunk, int section, Collection<Player> players) {
        PacketWriterUtils.writeAndSend(players, getChunkSectionUpdatePacket(chunk, section));
    }

    public void sendChunkSectionUpdate(Chunk chunk, int section, Player player) {
        if (!PlayerUtils.isNettyClient(player))
            return;

        PacketWriterUtils.writeAndSend(player, getChunkSectionUpdatePacket(chunk, section));
    }

    protected ChunkDataPacket getChunkSectionUpdatePacket(Chunk chunk, int section) {
        ChunkDataPacket chunkDataPacket = chunk.getFreshPartialDataPacket();
        chunkDataPacket.fullChunk = false;
        int[] sections = new int[16];
        sections[section] = 1;
        chunkDataPacket.sections = sections;
        return chunkDataPacket;
    }
    //


    /**
     * Get the instance dimension
     *
     * @return the dimension of the instance
     */
    public DimensionType getDimensionType() {
        return dimensionType;
    }

    /**
     * Get the age of this instance in tick
     *
     * @return the age of this instance in tick
     */
    public long getWorldAge() {
        return worldAge;
    }

    /**
     * Get the current time in the instance (sun/moon)
     *
     * @return the time in the instance
     */
    public long getTime() {
        return time;
    }

    /**
     * Change the current time in the instance, from 0 to 24000
     * <p>
     * 0 = sunrise
     * 6000 = noon
     * 12000 = sunset
     * 18000 = midnight
     * <p>
     * This method is unaffected by {@link #getTimeRate()}
     * <p>
     * It does send the new time to all players in the instance, unaffected by {@link #getTimeUpdate()}
     *
     * @param time the new time of the instance
     */
    public void setTime(long time) {
        this.time = time;
        PacketWriterUtils.writeAndSend(getPlayers(), getTimePacket());
    }

    /**
     * Get the rate of the time passing, it is 1 by default
     *
     * @return the time rate of the instance
     */
    public int getTimeRate() {
        return timeRate;
    }

    /**
     * Change the time rate of the instance
     * <p>
     * 1 is the default value and can be set to 0 to be completely disabled (constant time)
     *
     * @param timeRate the new time rate of the instance
     * @throws IllegalStateException if {@code timeRate} is lower than 0
     */
    public void setTimeRate(int timeRate) {
        Check.stateCondition(timeRate < 0, "The time rate cannot be lower than 0");
        this.timeRate = timeRate;
    }

    /**
     * Get the rate at which the client is updated with the current instance time
     *
     * @return the client update rate for time related packet
     */
    public UpdateOption getTimeUpdate() {
        return timeUpdate;
    }

    /**
     * Change the rate at which the client is updated about the time
     * <p>
     * Setting it to null means that the client will never know about time change
     * (but will still change server-side)
     *
     * @param timeUpdate the new update rate concerning time
     */
    public void setTimeUpdate(UpdateOption timeUpdate) {
        this.timeUpdate = timeUpdate;
    }

    private TimeUpdatePacket getTimePacket() {
        TimeUpdatePacket timeUpdatePacket = new TimeUpdatePacket();
        timeUpdatePacket.worldAge = worldAge;
        timeUpdatePacket.timeOfDay = time;
        return timeUpdatePacket;
    }

    /**
     * Get the instance world border
     *
     * @return the world border linked to the instance
     */
    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    /**
     * Get the players in the instance
     *
     * @return an unmodifiable list containing all the players in the instance
     */
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Get the creatures in the instance
     *
     * @return an unmodifiable list containing all the creatures in the instance
     */
    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    /**
     * Get the object entities in the instance
     *
     * @return an unmodifiable list containing all the object entities in the instance
     */
    public Set<ObjectEntity> getObjectEntities() {
        return Collections.unmodifiableSet(objectEntities);
    }

    /**
     * Get the experience orbs in the instance
     *
     * @return an unmodifiable list containing all the experience orbs in the instance
     */
    public Set<ExperienceOrb> getExperienceOrbs() {
        return Collections.unmodifiableSet(experienceOrbs);
    }

    /**
     * Get the entities located in the chunk
     *
     * @param chunk the chunk to get the entities from
     * @return an unmodifiable set containing all the entities in a chunk,
     * if {@code chunk} is null, return an empty {@link HashSet}
     */
    public Set<Entity> getChunkEntities(Chunk chunk) {
        if (chunk == null)
            return new HashSet<>();

        final long index = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        return Collections.unmodifiableSet(getEntitiesInChunk(index));
    }

    /**
     * Refresh the visual block id at the position
     * <p>
     * WARNING: the custom block id at the position will not change
     *
     * @param x       the X position
     * @param y       the Y position
     * @param z       the Z position
     * @param blockId the new visual block id
     */
    public void refreshBlockId(int x, int y, int z, short blockId) {
        refreshBlockId(new BlockPosition(x, y, z), blockId);
    }

    /**
     * Refresh the visual block id at the position
     * <p>
     * WARNING: the custom block id at the position will not change
     *
     * @param x     the X position
     * @param y     the Y position
     * @param z     the Z position
     * @param block the new visual block
     */
    public void refreshBlockId(int x, int y, int z, Block block) {
        refreshBlockId(x, y, z, block.getBlockId());
    }

    /**
     * Refresh the visual block id at the position
     * <p>
     * WARNING: the custom block id at the position will not change
     *
     * @param blockPosition the block position
     * @param block         the new visual block
     */
    public void refreshBlockId(BlockPosition blockPosition, Block block) {
        refreshBlockId(blockPosition, block.getBlockId());
    }

    /**
     * Load the chunk at the given position without any callback
     * <p>
     * WARNING: this is a non-blocking task
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    /**
     * Load the chunk at the given position with a callback
     *
     * @param position the chunk position
     * @param callback the callback to run when the chunk is loaded
     */
    public void loadChunk(Position position, Consumer<Chunk> callback) {
        final int chunkX = ChunkUtils.getChunkCoordinate((int) position.getX());
        final int chunkZ = ChunkUtils.getChunkCoordinate((int) position.getZ());
        loadChunk(chunkX, chunkZ, callback);
    }

    public void loadOptionalChunk(Position position, Consumer<Chunk> callback) {
        final int chunkX = ChunkUtils.getChunkCoordinate((int) position.getX());
        final int chunkZ = ChunkUtils.getChunkCoordinate((int) position.getZ());
        loadOptionalChunk(chunkX, chunkZ, callback);
    }

    /**
     * Unload the chunk at the given position
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void unloadChunk(int chunkX, int chunkZ) {
        unloadChunk(getChunk(chunkX, chunkZ));
    }

    /**
     * Give the visual block id at the given position
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the visual block id at the position
     */
    public short getBlockId(int x, int y, int z) {
        final Chunk chunk = getChunkAt(x, z);
        Check.notNull(chunk, "The chunk at " + x + ":" + z + " is not loaded");
        return chunk.getBlockId(x, y, z);
    }

    /**
     * Give the visual block id at the given position
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the visual block id at the position
     */
    public short getBlockId(float x, float y, float z) {
        return getBlockId(Math.round(x), Math.round(y), Math.round(z));
    }

    /**
     * Give the visual block id at the given position
     *
     * @param blockPosition the block position
     * @return the visual block id at the position
     */
    public short getBlockId(BlockPosition blockPosition) {
        return getBlockId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    /**
     * Get the custom block object at the given position, or null if not any
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the custom block object at the position, null if not any
     */
    public CustomBlock getCustomBlock(int x, int y, int z) {
        final Chunk chunk = getChunkAt(x, z);
        Check.notNull(chunk, "The chunk at " + x + ":" + z + " is not loaded");
        return chunk.getCustomBlock(x, y, z);
    }

    /**
     * Get the custom block object at the given position, or null if not any
     *
     * @param blockPosition the block position
     * @return the custom block object at the position, null if not any
     */
    public CustomBlock getCustomBlock(BlockPosition blockPosition) {
        return getCustomBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public void sendBlockAction(BlockPosition blockPosition, byte actionId, byte actionParam) {
        final short blockId = getBlockId(blockPosition);
        final Block block = Block.fromId(blockId);

        BlockActionPacket blockActionPacket = new BlockActionPacket();
        blockActionPacket.blockPosition = blockPosition;
        blockActionPacket.actionId = actionId;
        blockActionPacket.actionParam = actionParam;
        blockActionPacket.blockId = block.getBlockId();

        final Chunk chunk = getChunkAt(blockPosition);
        chunk.sendPacketToViewers(blockActionPacket);
    }

    /**
     * Get the block data at the given position, or null if not any
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the block data at the position, null if not any
     */
    public Data getBlockData(int x, int y, int z) {
        final Chunk chunk = getChunkAt(x, z);
        Check.notNull(chunk, "The chunk at " + x + ":" + z + " is not loaded");
        return chunk.getData(x, (byte) y, z);
    }

    /**
     * Get the block data at the given position, or null if not any
     *
     * @param blockPosition the block position
     * @return the block data at the position, null if not any
     */
    public Data getBlockData(BlockPosition blockPosition) {
        return getBlockData(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    /**
     * Get the chunk at the given position, null if not loaded
     *
     * @param x the X position
     * @param z the Z position
     * @return the chunk at the given position, null if not loaded
     */
    public Chunk getChunkAt(double x, double z) {
        final int chunkX = ChunkUtils.getChunkCoordinate((int) x);
        final int chunkZ = ChunkUtils.getChunkCoordinate((int) z);
        return getChunk(chunkX, chunkZ);
    }

    /**
     * Check if the chunk at the position is loaded
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return true if the chunk is loaded, false otherwise
     */
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    /**
     * Get the chunk at the given position, null if not loaded
     *
     * @param blockPosition the chunk position
     * @return the chunk at the given position, null if not loaded
     */
    public Chunk getChunkAt(BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    /**
     * Get the chunk at the given position, null if not loaded
     *
     * @param position the chunk position
     * @return the chunk at the given position, null if not loaded
     */
    public Chunk getChunkAt(Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    /**
     * Save a chunk to the instance storage folder without any callback
     *
     * @param chunk the chunk to save
     */
    public void saveChunkToStorageFolder(Chunk chunk) {
        saveChunkToStorageFolder(chunk, null);
    }

    /**
     * Save all chunks to the instance storage folder without any callback
     */
    public void saveChunksToStorageFolder() {
        saveChunksToStorageFolder(null);
    }

    /**
     * Get the instance unique id
     *
     * @return the instance unique id
     */
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

    @Override
    public <E extends Event> void addEventCallback(Class<E> eventClass, EventCallback<E> eventCallback) {
        List<EventCallback> callbacks = getEventCallbacks(eventClass);
        callbacks.add(eventCallback);
        this.eventCallbacks.put(eventClass, callbacks);
    }

    @Override
    public <E extends Event> void removeEventCallback(Class<E> eventClass, EventCallback<E> eventCallback) {
        List<EventCallback> callbacks = getEventCallbacks(eventClass);
        callbacks.remove(eventCallback);
        this.eventCallbacks.put(eventClass, callbacks);
    }

    @Override
    public <E extends Event> List<EventCallback> getEventCallbacks(Class<E> eventClass) {
        return eventCallbacks.getOrDefault(eventClass, new CopyOnWriteArrayList<>());
    }

    // UNSAFE METHODS (need most of time to be synchronized)

    /**
     * Used when called {@link Entity#setInstance(Instance)}, it is used to refresh viewable chunks
     * and add viewers if {@code entity} is a Player
     * <p>
     * Warning: unsafe, you probably want to use {@link Entity#setInstance(Instance)} instead
     *
     * @param entity the entity to add
     */
    public void addEntity(Entity entity) {
        final Instance lastInstance = entity.getInstance();
        if (lastInstance != null && lastInstance != this) {
            lastInstance.removeEntity(entity); // If entity is in another instance, remove it from there and add it to this
        }
        AddEntityToInstanceEvent event = new AddEntityToInstanceEvent(this, entity);
        callCancellableEvent(AddEntityToInstanceEvent.class, event, () -> {
            final long[] visibleChunksEntity = ChunkUtils.getChunksInRange(entity.getPosition(), MinecraftServer.ENTITY_VIEW_DISTANCE);
            final boolean isPlayer = entity instanceof Player;

            if (isPlayer) {
                final Player player = (Player) entity;
                sendChunks(player);
                getWorldBorder().init(player);
            }

            // Send all visible entities
            for (long chunkIndex : visibleChunksEntity) {
                getEntitiesInChunk(chunkIndex).forEach(ent -> {
                    if (isPlayer) {
                        ent.addViewer((Player) entity);
                    }

                    if (ent instanceof Player) {
                        if (entity.isAutoViewable())
                            entity.addViewer((Player) ent);
                    }
                });
            }

            final Position entityPosition = entity.getPosition();
            final Chunk chunk = getChunkAt(entityPosition);
            Check.notNull(chunk, "You tried to spawn an entity in an unloaded chunk, " + entityPosition);
            addEntityToChunk(entity, chunk);
        });
    }

    /**
     * Used when an entity is removed from the instance, it removes all of his viewers
     * <p>
     * Warning: unsafe, you probably want to set the entity to another instance
     *
     * @param entity the entity to remove
     */
    public void removeEntity(Entity entity) {
        final Instance entityInstance = entity.getInstance();
        if (entityInstance == null || entityInstance != this)
            return;

        RemoveEntityFromInstanceEvent event = new RemoveEntityFromInstanceEvent(this, entity);
        callCancellableEvent(RemoveEntityFromInstanceEvent.class, event, () -> {
            // Remove this entity from players viewable list and send delete entities packet
            entity.getViewers().forEach(p -> entity.removeViewer(p));

            // Remove the entity from cache
            final Chunk chunk = getChunkAt(entity.getPosition());
            removeEntityFromChunk(entity, chunk);
        });
    }

    /**
     * Add the specified entity to the instance entities cache
     * <p>
     * Warning: this is done automatically when the entity move out of his chunk
     *
     * @param entity the entity to add
     * @param chunk  the chunk where the entity will be added
     */
    public void addEntityToChunk(Entity entity, Chunk chunk) {
        Check.notNull(chunk,
                "The chunk " + chunk + " is not loaded, you can make it automatic by using Instance#enableAutoChunkLoad(true)");
        Check.argCondition(!chunk.isLoaded(), "Chunk " + chunk + " has been unloaded previously");
        final long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
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

    /**
     * Remove the specified entity to the instance entities cache
     * <p>
     * Warning: this is done automatically when the entity move out of his chunk
     *
     * @param entity the entity to remove
     * @param chunk  the chunk where the entity will be removed
     */
    public void removeEntityFromChunk(Entity entity, Chunk chunk) {
        synchronized (chunkEntities) {
            if (chunk != null) {
                final long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
                Set<Entity> entities = getEntitiesInChunk(chunkIndex);
                entities.remove(entity);
                if (entities.isEmpty()) {
                    this.chunkEntities.remove(chunkIndex);
                } else {
                    this.chunkEntities.put(chunkIndex, entities);
                }
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
        final Set<Entity> entities = chunkEntities.getOrDefault(index, new CopyOnWriteArraySet<>());
        return entities;
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
     *
     * @param time the current time
     */
    public void tick(long time) {
        synchronized (nextTick) {
            for (final Consumer<Instance> e : nextTick) {
                e.accept(this);
            }
            nextTick.clear();
        }
        {
            // time
            this.worldAge++;

            this.time += 1 * timeRate;

            if (timeUpdate != null && !CooldownUtils.hasCooldown(time, lastTimeUpdate, timeUpdate)) {
                PacketWriterUtils.writeAndSend(getPlayers(), getTimePacket());
                this.lastTimeUpdate = time;
            }

        }
        this.worldBorder.update();
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX
     * @param centerY
     * @param centerZ
     * @param strength
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength) {
        explode(centerX, centerY, centerZ, strength, null);
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX
     * @param centerY
     * @param centerZ
     * @param strength
     * @param additionalData data to pass to the explosion supplier
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength, Data additionalData) {
        final ExplosionSupplier explosionSupplier = getExplosionSupplier();
        if (explosionSupplier == null)
            throw new IllegalStateException("Tried to create an explosion with no explosion supplier");
        final Explosion explosion = explosionSupplier.createExplosion(centerX, centerY, centerZ, strength, additionalData);
        explosion.apply(this);
    }

    /**
     * Return the registered explosion supplier, or null if none was provided
     *
     * @return the instance explosion supplier, null if none was provided
     */
    public ExplosionSupplier getExplosionSupplier() {
        return explosionSupplier;
    }

    /**
     * Registers the explosion supplier to use in this instance
     *
     * @param supplier the explosion supplier
     */
    public void setExplosionSupplier(ExplosionSupplier supplier) {
        this.explosionSupplier = supplier;
    }

    /**
     * Get the instance space
     * <p>
     * Used by the pathfinder for entities
     *
     * @return the instance space
     */
    public PFInstanceSpace getInstanceSpace() {
        return instanceSpace;
    }
}