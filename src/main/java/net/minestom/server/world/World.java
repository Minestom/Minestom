package net.minestom.server.world;

import com.google.common.collect.Queues;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.UpdateManager;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFWorldSpace;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.world.AddEntityToWorldEvent;
import net.minestom.server.event.world.WorldTickEvent;
import net.minestom.server.event.world.RemoveEntityFromWorldEvent;
import net.minestom.server.block.Block;
import net.minestom.server.block.BlockGetter;
import net.minestom.server.block.BlockManager;
import net.minestom.server.block.BlockSetter;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * You can add an entity to a World using {@link Entity#setWorld(World)}.
 * <p>
 * A World has entities and chunks, each World contains its own entity list but the
 * chunk implementation has to be defined, see {@link WorldContainer}.
 * <p>
 * WARNING: when making your own implementation registering the World manually is required
 * with {@link WorldManager#registerWorld(World)}, and
 * you need to be sure to signal the {@link UpdateManager} of the changes using
 * {@link UpdateManager#signalChunkLoad(Chunk)} and {@link UpdateManager#signalChunkUnload(Chunk)}.
 */
public abstract class World implements BlockGetter, BlockSetter, Tickable, EventHandler, DataContainer, PacketGroupingAudience {

    protected static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    protected static final UpdateManager UPDATE_MANAGER = MinecraftServer.getUpdateManager();

    private boolean registered;

    private final DimensionType dimensionType;

    private final WorldBorder worldBorder;

    // Tick since the creation of the world
    private long worldAge;

    // The time of the world
    private long time;
    private int timeRate = 1;
    private UpdateOption timeUpdate = new UpdateOption(1, TimeUnit.SECOND);
    private long lastTimeUpdate;

    // Field for tick events
    private long lastTickAge = System.currentTimeMillis();

    private final Map<Class<? extends Event>, Collection<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Collection<EventCallback<?>>> extensionCallbacks = new ConcurrentHashMap<>();

    // Entities present in this world
    protected final Set<Entity> entities = ConcurrentHashMap.newKeySet();
    protected final Set<Player> players = ConcurrentHashMap.newKeySet();
    protected final Set<EntityCreature> creatures = ConcurrentHashMap.newKeySet();
    protected final Set<ExperienceOrb> experienceOrbs = ConcurrentHashMap.newKeySet();
    // Entities per chunk
    protected final Map<Long, Set<Entity>> chunkEntities = new ConcurrentHashMap<>();
    private final Object entitiesLock = new Object(); // Lock used to prevent the entities Set and Map to be subject to race condition

    // the uuid of this world
    protected UUID uniqueId;

    // list of scheduled tasks to be executed during the next world tick
    protected final Queue<Consumer<World>> nextTick = Queues.newConcurrentLinkedQueue();

    // world custom data
    private Data data;

    // the explosion supplier
    private ExplosionSupplier explosionSupplier;

    // Pathfinder
    private final PFWorldSpace worldSpace = new PFWorldSpace(this);

    /**
     * Creates a new World.
     *
     * @param uniqueId      the {@link UUID} of the World
     * @param dimensionType the {@link DimensionType} of the World
     */
    public World(@NotNull UUID uniqueId, @NotNull DimensionType dimensionType) {
        Check.argCondition(!dimensionType.isRegistered(),
                "The dimension " + dimensionType.getName() + " is not registered! Please use DimensionTypeManager#addDimension");
        this.uniqueId = uniqueId;
        this.dimensionType = dimensionType;

        this.worldBorder = new WorldBorder(this);
    }

    /**
     * Schedules a task to be run during the next World tick.
     * It ensures that the task will be executed in the same thread as the World
     * and its chunks/entities (depending of the {@link ThreadProvider}).
     *
     * @param callback the task to execute during the next World tick
     */
    public void scheduleNextTick(@NotNull Consumer<World> callback) {
        this.nextTick.add(callback);
    }

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the {@link Player} who break the block
     * @param blockPosition the {@link BlockPosition} of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    public abstract boolean breakBlock(@NotNull Player player, @NotNull BlockPosition blockPosition);

    /**
     * Forces the generation of a {@link Chunk}, even if no file and {@link ChunkGenerator} are defined.
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback optional consumer called after the chunk has been generated,
     *                 the returned chunk will never be null
     */
    public abstract void loadChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback);

    /**
     * Loads the chunk if the chunk is already loaded or if
     * {@link #hasEnabledAutoChunkLoad()} returns true.
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback optional consumer called after the chunk has tried to be loaded,
     *                 contains a chunk if it is successful, null otherwise
     */
    public abstract void loadOptionalChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback);

    /**
     * Schedules the removal of a {@link Chunk}, this method does not promise when it will be done.
     * <p>
     * WARNING: during unloading, all entities other than {@link Player} will be removed.
     * <p>
     * For {@link WorldContainer} it is done during the next {@link WorldContainer#tick(long)}.
     *
     * @param chunk the chunk to unload
     */
    public abstract void unloadChunk(@NotNull Chunk chunk);

    /**
     * Gets the loaded {@link Chunk} at a position.
     * <p>
     * WARNING: this should only return already-loaded chunk, use {@link #loadChunk(int, int)} or similar to load one instead.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk at the specified position, null if not loaded
     */
    @Nullable
    public abstract Chunk getChunk(int chunkX, int chunkZ);

    /**
     * Saves a {@link Chunk} to permanent storage.
     *
     * @param chunk    the {@link Chunk} to save
     * @param callback optional callback called when the {@link Chunk} is done saving
     */
    public abstract void saveChunkToStorage(@NotNull Chunk chunk, @Nullable Runnable callback);

    /**
     * Saves multiple chunks to permanent storage.
     *
     * @param callback optional callback called when the chunks are done saving
     */
    public abstract void saveChunksToStorage(@Nullable Runnable callback);

    /**
     * Gets the World's {@link ChunkGenerator}.
     *
     * @return the {@link ChunkGenerator} of the World
     */
    @Nullable
    public abstract ChunkGenerator getChunkGenerator();

    /**
     * Changes the World's {@link ChunkGenerator}.
     *
     * @param chunkGenerator the new {@link ChunkGenerator} of the World
     */
    public abstract void setChunkGenerator(@Nullable ChunkGenerator chunkGenerator);

    /**
     * Gets all the World's loaded chunks.
     *
     * @return an unmodifiable containing all the World's chunks
     */
    @NotNull
    public abstract Collection<Chunk> getChunks();

    /**
     * Gets the World's {@link StorageLocation}.
     *
     * @return the {@link StorageLocation} of the World
     */
    @Nullable
    public abstract StorageLocation getStorageLocation();

    /**
     * Changes the World {@link StorageLocation}.
     *
     * @param storageLocation the new {@link StorageLocation} of the World
     */
    public abstract void setStorageLocation(@Nullable StorageLocation storageLocation);

    /**
     * Used when a {@link Chunk} is not currently loaded in memory and need to be retrieved from somewhere else.
     * Could be read from disk, or generated from scratch.
     * <p>
     * Be sure to signal the chunk using {@link UpdateManager#signalChunkLoad(Chunk)} and to cache
     * that this chunk has been loaded.
     * <p>
     * WARNING: it has to retrieve a chunk, this is not optional and should execute the callback in all case.
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk X
     * @param callback the optional callback executed once the {@link Chunk} has been retrieved
     */
    protected abstract void retrieveChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback);

    /**
     * Called to generated a new {@link Chunk} from scratch.
     * <p>
     * Be sure to signal the chunk using {@link UpdateManager#signalChunkLoad(Chunk)} and to cache
     * that this chunk has been loaded.
     * <p>
     * This is where you can put your chunk generation code.
     *
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback the optional callback executed with the newly created {@link Chunk}
     */
    protected abstract void createChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback);

    /**
     * When set to true, chunks will load automatically when requested.
     * Otherwise using {@link #loadChunk(int, int)} will be required to even spawn a player
     *
     * @param enable enable the auto chunk load
     */
    public abstract void enableAutoChunkLoad(boolean enable);

    /**
     * Gets if the World should auto load chunks.
     *
     * @return true if auto chunk load is enabled, false otherwise
     */
    public abstract boolean hasEnabledAutoChunkLoad();

    /**
     * Determines whether a position in the void. If true, entities should take damage and die.
     * <p>
     * Always returning false allow entities to survive in the void.
     *
     * @param position the position in the world
     * @return true iif position is inside the void
     */
    public abstract boolean isInVoid(@NotNull Position position);

    /**
     * Gets if the World has been registered in {@link WorldManager}.
     *
     * @return true if the World has been registered
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Changes the registered field.
     * <p>
     * WARNING: should only be used by {@link WorldManager}.
     *
     * @param registered true to mark the World as registered
     */
    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * Gets the World's {@link DimensionType}.
     *
     * @return the dimension of the World
     */
    public DimensionType getDimensionType() {
        return dimensionType;
    }

    /**
     * Gets the age of this World in ticks.
     *
     * @return the age of this World in ticks
     */
    public long getWorldAge() {
        return worldAge;
    }

    /**
     * Gets the current time in the World (sun/moon).
     *
     * @return the time in the World
     */
    public long getTime() {
        return time;
    }

    /**
     * Changes the current time in the World, from 0 to 24000.
     * <p>
     * If the time is negative, the vanilla client will not move the sun.
     * <p>
     * 0 = sunrise
     * 6000 = noon
     * 12000 = sunset
     * 18000 = midnight
     * <p>
     * This method is unaffected by {@link #getTimeRate()}
     * <p>
     * It does send the new time to all players in the World, unaffected by {@link #getTimeUpdate()}
     *
     * @param time the new time of the World
     */
    public void setTime(long time) {
        this.time = time;
        PacketUtils.sendGroupedPacket(getPlayers(), createTimePacket());
    }

    /**
     * Gets the rate of the time passing, it is 1 by default
     *
     * @return the time rate of the World
     */
    public int getTimeRate() {
        return timeRate;
    }

    /**
     * Changes the time rate of the World
     * <p>
     * 1 is the default value and can be set to 0 to be completely disabled (constant time)
     *
     * @param timeRate the new time rate of the World
     * @throws IllegalStateException if {@code timeRate} is lower than 0
     */
    public void setTimeRate(int timeRate) {
        Check.stateCondition(timeRate < 0, "The time rate cannot be lower than 0");
        this.timeRate = timeRate;
    }

    /**
     * Gets the rate at which the client is updated with the current World's time
     *
     * @return the client update rate for time related packet
     */
    @Nullable
    public UpdateOption getTimeUpdate() {
        return timeUpdate;
    }

    /**
     * Changes the rate at which the client is updated about the time
     * <p>
     * Setting it to null means that the client will never know about time change
     * (but will still change server-side)
     *
     * @param timeUpdate the new update rate concerning time
     */
    public void setTimeUpdate(@Nullable UpdateOption timeUpdate) {
        this.timeUpdate = timeUpdate;
    }

    /**
     * Creates a {@link TimeUpdatePacket} with the current age and time of this World
     *
     * @return the {@link TimeUpdatePacket} with this World's data
     */
    @NotNull
    private TimeUpdatePacket createTimePacket() {
        TimeUpdatePacket timeUpdatePacket = new TimeUpdatePacket();
        timeUpdatePacket.worldAge = worldAge;
        timeUpdatePacket.timeOfDay = time;
        return timeUpdatePacket;
    }

    /**
     * Gets the World's {@link WorldBorder};
     *
     * @return the {@link WorldBorder} linked to the World
     */
    @NotNull
    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    /**
     * Gets the entities in the World;
     *
     * @return an unmodifiable {@link Set} containing all the entities in the World
     */
    @NotNull
    public Set<Entity> getEntities() {
        return Collections.unmodifiableSet(entities);
    }

    /**
     * Gets the players in the World;
     *
     * @return an unmodifiable {@link Set} containing all the players in the World
     */
    @Override
    @NotNull
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Gets the creatures in the World;
     *
     * @return an unmodifiable {@link Set} containing all the creatures in the World
     */
    @NotNull
    public Set<EntityCreature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    /**
     * Gets the experience orbs in the World.
     *
     * @return an unmodifiable {@link Set} containing all the experience orbs in the World
     */
    @NotNull
    public Set<ExperienceOrb> getExperienceOrbs() {
        return Collections.unmodifiableSet(experienceOrbs);
    }

    /**
     * Gets the entities located in the chunk.
     *
     * @param chunk the chunk to get the entities from
     * @return an unmodifiable {@link Set} containing all the entities in a chunk,
     * if {@code chunk} is unloaded, return an empty {@link HashSet}
     */
    public @NotNull Set<Entity> getChunkEntities(Chunk chunk) {
        if (!ChunkUtils.isLoaded(chunk))
            return Collections.emptySet();

        final long index = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        final Set<Entity> entities = getEntitiesInChunk(index);
        return Collections.unmodifiableSet(entities);
    }

    /**
     * Loads the {@link Chunk} at the given position without any callback.
     * <p>
     * WARNING: this is a non-blocking task.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void loadChunk(int chunkX, int chunkZ) {
        loadChunk(chunkX, chunkZ, null);
    }

    /**
     * Loads the chunk at the given {@link Position} with a callback.
     *
     * @param position the chunk position
     * @param callback the optional callback to run when the chunk is loaded
     */
    public void loadChunk(@NotNull Position position, @Nullable ChunkCallback callback) {
        final int chunkX = ChunkUtils.getChunkCoordinate(position.getX());
        final int chunkZ = ChunkUtils.getChunkCoordinate(position.getZ());
        loadChunk(chunkX, chunkZ, callback);
    }

    /**
     * Loads a {@link Chunk} (if {@link #hasEnabledAutoChunkLoad()} returns true)
     * at the given {@link Position} with a callback.
     *
     * @param position the chunk position
     * @param callback the optional callback executed when the chunk is loaded (or with a null chunk if not)
     */
    public void loadOptionalChunk(@NotNull Position position, @Nullable ChunkCallback callback) {
        final int chunkX = ChunkUtils.getChunkCoordinate(position.getX());
        final int chunkZ = ChunkUtils.getChunkCoordinate(position.getZ());
        loadOptionalChunk(chunkX, chunkZ, callback);
    }

    /**
     * Unloads the chunk at the given position.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void unloadChunk(int chunkX, int chunkZ) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        Check.notNull(chunk, "The chunk at " + chunkX + ":" + chunkZ + " is already unloaded");
        unloadChunk(chunk);
    }

    @Override
    public @NotNull Block getBlock(int x, int y, int z) {
        final Chunk chunk = getChunkAt(x, z);
        Check.notNull(chunk, "The chunk at {0}:{1} is not loaded", x, z);
        synchronized (chunk) {
            return chunk.getBlock(x, y, z);
        }
    }

    /**
     * Sends a {@link BlockActionPacket} for all the viewers of the specific position.
     *
     * @param blockPosition the block position
     * @param actionId      the action id, depends on the block
     * @param actionParam   the action parameter, depends on the block
     * @see <a href="https://wiki.vg/Protocol#Block_Action">BlockActionPacket</a> for the action id &amp; param
     */
    public void sendBlockAction(@NotNull BlockPosition blockPosition, byte actionId, byte actionParam) {
        final Block block = getBlock(blockPosition);

        BlockActionPacket blockActionPacket = new BlockActionPacket();
        blockActionPacket.blockPosition = blockPosition;
        blockActionPacket.actionId = actionId;
        blockActionPacket.actionParam = actionParam;
        blockActionPacket.blockId = block.getId();

        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "The chunk at " + blockPosition + " is not loaded!");
        chunk.sendPacketToViewers(blockActionPacket);
    }

    /**
     * Gets the {@link Chunk} at the given {@link BlockPosition}, null if not loaded.
     *
     * @param x the X position
     * @param z the Z position
     * @return the chunk at the given position, null if not loaded
     */
    @Nullable
    public Chunk getChunkAt(double x, double z) {
        final int chunkX = ChunkUtils.getChunkCoordinate(x);
        final int chunkZ = ChunkUtils.getChunkCoordinate(z);
        return getChunk(chunkX, chunkZ);
    }

    /**
     * Checks if the {@link Chunk} at the position is loaded.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return true if the chunk is loaded, false otherwise
     */
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    /**
     * Gets the {@link Chunk} at the given {@link BlockPosition}, null if not loaded.
     *
     * @param blockPosition the chunk position
     * @return the chunk at the given position, null if not loaded
     */
    @Nullable
    public Chunk getChunkAt(@NotNull BlockPosition blockPosition) {
        return getChunkAt(blockPosition.getX(), blockPosition.getZ());
    }

    /**
     * Gets the {@link Chunk} at the given {@link Position}, null if not loaded.
     *
     * @param position the chunk position
     * @return the chunk at the given position, null if not loaded
     */
    @Nullable
    public Chunk getChunkAt(@NotNull Position position) {
        return getChunkAt(position.getX(), position.getZ());
    }

    /**
     * Saves a {@link Chunk} without any callback.
     *
     * @param chunk the chunk to save
     */
    public void saveChunkToStorage(@NotNull Chunk chunk) {
        saveChunkToStorage(chunk, null);
    }

    /**
     * Saves all {@link Chunk} without any callback.
     */
    public void saveChunksToStorage() {
        saveChunksToStorage(null);
    }

    /**
     * Gets the World's unique id.
     *
     * @return the World's unique id
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(@Nullable Data data) {
        this.data = data;
    }

    @NotNull
    @Override
    public Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap() {
        return eventCallbacks;
    }

    @NotNull
    @Override
    public Collection<EventCallback<?>> getExtensionCallbacks(String extension) {
        return extensionCallbacks.computeIfAbsent(extension, e -> new CopyOnWriteArrayList<>());
    }

    // UNSAFE METHODS (need most of time to be synchronized)

    /**
     * Used when called {@link Entity#setWorld(World)}, it is used to refresh viewable chunks
     * and add viewers if {@code entity} is a {@link Player}.
     * <p>
     * Warning: unsafe, you probably want to use {@link Entity#setWorld(World)} instead.
     *
     * @param entity the entity to add
     */
    @ApiStatus.Internal
    public void UNSAFE_addEntity(@NotNull Entity entity) {
        final World lastWorld = entity.getWorld();
        if (lastWorld != null && lastWorld != this) {
            lastWorld.UNSAFE_removeEntity(entity); // If entity is in another world, remove it from there and add it to this
        }
        AddEntityToWorldEvent event = new AddEntityToWorldEvent(this, entity);
        callCancellableEvent(AddEntityToWorldEvent.class, event, () -> {
            final Position entityPosition = entity.getPosition();
            final boolean isPlayer = entity instanceof Player;

            if (isPlayer) {
                final Player player = (Player) entity;
                getWorldBorder().init(player);
                player.getPlayerConnection().sendPacket(createTimePacket());
            }

            // Send all visible entities
            EntityUtils.forEachRange(this, entityPosition, MinecraftServer.getEntityViewDistance(), ent -> {
                if (isPlayer) {
                    if (ent.isAutoViewable())
                        ent.addViewer((Player) entity);
                }

                if (ent instanceof Player) {
                    if (entity.isAutoViewable())
                        entity.addViewer((Player) ent);
                }
            });

            // Load the chunk if not already (or throw an error if auto chunk load is disabled)
            loadOptionalChunk(entityPosition, chunk -> {
                Check.notNull(chunk, "You tried to spawn an entity in an unloaded chunk, " + entityPosition);
                UNSAFE_addEntityToChunk(entity, chunk);
            });
        });
    }

    /**
     * Used when an {@link Entity} is removed from the World, it removes all of his viewers.
     * <p>
     * Warning: unsafe, you probably want to set the entity to another World.
     *
     * @param entity the entity to remove
     */
    @ApiStatus.Internal
    public void UNSAFE_removeEntity(@NotNull Entity entity) {
        final World entityWorld = entity.getWorld();
        if (entityWorld != this)
            return;

        RemoveEntityFromWorldEvent event = new RemoveEntityFromWorldEvent(this, entity);
        callCancellableEvent(RemoveEntityFromWorldEvent.class, event, () -> {
            // Remove this entity from players viewable list and send delete entities packet
            entity.getViewers().forEach(entity::removeViewer);

            // Remove the entity from cache
            final Chunk chunk = getChunkAt(entity.getPosition());
            Check.notNull(chunk, "Tried to interact with an unloaded chunk.");
            UNSAFE_removeEntityFromChunk(entity, chunk);
        });
    }

    /**
     * Synchronized method to execute {@link #UNSAFE_removeEntityFromChunk(Entity, Chunk)}
     * and {@link #UNSAFE_addEntityToChunk(Entity, Chunk)} simultaneously.
     *
     * @param entity    the entity to change its chunk
     * @param lastChunk the last entity chunk
     * @param newChunk  the new entity chunk
     */
    @ApiStatus.Internal
    public synchronized void UNSAFE_switchEntityChunk(@NotNull Entity entity, @NotNull Chunk lastChunk, @NotNull Chunk newChunk) {
        UNSAFE_removeEntityFromChunk(entity, lastChunk);
        UNSAFE_addEntityToChunk(entity, newChunk);
    }

    /**
     * Adds the specified {@link Entity} to the World's entities cache.
     * <p>
     * Warning: this is done automatically when the entity move out of his chunk.
     *
     * @param entity the entity to add
     * @param chunk  the chunk where the entity will be added
     */
    @ApiStatus.Internal
    public void UNSAFE_addEntityToChunk(@NotNull Entity entity, @NotNull Chunk chunk) {
        Check.notNull(chunk,
                "The chunk " + chunk + " is not loaded, you can make it automatic by using World#enableAutoChunkLoad(true)");
        Check.argCondition(!chunk.isLoaded(), "Chunk " + chunk + " has been unloaded previously");
        final long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        synchronized (entitiesLock) {
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.add(entity);

            this.entities.add(entity);
            if (entity instanceof Player) {
                this.players.add((Player) entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.add((EntityCreature) entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.add((ExperienceOrb) entity);
            }
        }
    }

    /**
     * Removes the specified {@link Entity} to the World's entities cache.
     * <p>
     * Warning: this is done automatically when the entity move out of his chunk.
     *
     * @param entity the entity to remove
     * @param chunk  the chunk where the entity will be removed
     */
    @ApiStatus.Internal
    public void UNSAFE_removeEntityFromChunk(@NotNull Entity entity, @NotNull Chunk chunk) {
        synchronized (entitiesLock) {
            final long chunkIndex = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
            Set<Entity> entities = getEntitiesInChunk(chunkIndex);
            entities.remove(entity);

            this.entities.remove(entity);
            if (entity instanceof Player) {
                this.players.remove(entity);
            } else if (entity instanceof EntityCreature) {
                this.creatures.remove(entity);
            } else if (entity instanceof ExperienceOrb) {
                this.experienceOrbs.remove(entity);
            }
        }
    }

    @NotNull
    private Set<Entity> getEntitiesInChunk(long index) {
        return chunkEntities.computeIfAbsent(index, i -> ConcurrentHashMap.newKeySet());
    }

    /**
     * Performs a single tick in the World, including scheduled tasks from {@link #scheduleNextTick(Consumer)}.
     * <p>
     * Warning: this does not update chunks and entities.
     *
     * @param time the tick time in milliseconds
     */
    @Override
    public void tick(long time) {
        // Scheduled tasks
        if (!nextTick.isEmpty()) {
            Consumer<World> callback;
            while ((callback = nextTick.poll()) != null) {
                callback.accept(this);
            }
        }

        // Time
        {
            this.worldAge++;

            this.time += timeRate;

            // time needs to be send to players
            if (timeUpdate != null && !Cooldown.hasCooldown(time, lastTimeUpdate, timeUpdate)) {
                PacketUtils.sendGroupedPacket(getPlayers(), createTimePacket());
                this.lastTimeUpdate = time;
            }

        }

        // Tick event
        {
            // Process tick events
            WorldTickEvent chunkTickEvent = new WorldTickEvent(this, time, lastTickAge);
            callEvent(WorldTickEvent.class, chunkTickEvent);

            // Set last tick age
            lastTickAge = time;
        }

        this.worldBorder.update();
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX  the center X
     * @param centerY  the center Y
     * @param centerZ  the center Z
     * @param strength the strength of the explosion
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength) {
        explode(centerX, centerY, centerZ, strength, null);
    }

    /**
     * Creates an explosion at the given position with the given strength.
     * The algorithm used to compute damages is provided by {@link #getExplosionSupplier()}.
     *
     * @param centerX        center X of the explosion
     * @param centerY        center Y of the explosion
     * @param centerZ        center Z of the explosion
     * @param strength       the strength of the explosion
     * @param additionalData data to pass to the explosion supplier
     * @throws IllegalStateException If no {@link ExplosionSupplier} was supplied
     */
    public void explode(float centerX, float centerY, float centerZ, float strength, @Nullable Data additionalData) {
        final ExplosionSupplier explosionSupplier = getExplosionSupplier();
        Check.stateCondition(explosionSupplier == null, "Tried to create an explosion with no explosion supplier");
        final Explosion explosion = explosionSupplier.createExplosion(centerX, centerY, centerZ, strength, additionalData);
        explosion.apply(this);
    }

    /**
     * Gets the registered {@link ExplosionSupplier}, or null if none was provided.
     *
     * @return the World's explosion supplier, null if none was provided
     */
    @Nullable
    public ExplosionSupplier getExplosionSupplier() {
        return explosionSupplier;
    }

    /**
     * Registers the {@link ExplosionSupplier} to use in this World.
     *
     * @param supplier the explosion supplier
     */
    public void setExplosionSupplier(@Nullable ExplosionSupplier supplier) {
        this.explosionSupplier = supplier;
    }

    /**
     * Gets the WorldSpace.
     * <p>
     * Used by the pathfinder for entities.
     *
     * @return the WorldSpace
     */
    @NotNull
    public PFWorldSpace getWorldSpace() {
        return worldSpace;
    }
}