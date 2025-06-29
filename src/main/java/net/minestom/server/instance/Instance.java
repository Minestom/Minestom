package net.minestom.server.instance;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.ServerProcess;
import net.minestom.server.Tickable;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.light.Light;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.network.packet.server.play.InitializeWorldBorderPacket;
import net.minestom.server.network.packet.server.play.TimeUpdatePacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.snapshot.*;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Instances are what are called "worlds" in Minecraft, you can add an entity in it using {@link Entity#setInstance(Instance)}.
 * <p>
 * An instance has entities and chunks, each instance contains its own entity list but the
 * chunk implementation has to be defined, see {@link InstanceContainer}.
 * <p>
 * WARNING: when making your own implementation registering the instance manually is required
 * with {@link InstanceManager#registerInstance(Instance)}, and
 * you need to be sure to signal the {@link ThreadDispatcher} of every partition/element changes.
 */
public abstract class Instance implements Block.Getter, Block.Setter,
        Tickable, Schedulable, Snapshotable, EventHandler<InstanceEvent>, Taggable, PacketGroupingAudience, Pointered, Identified {

    // Adventure pointers
    protected static final PointersSupplier<Instance> INSTANCE_POINTERS_SUPPLIER = PointersSupplier.<Instance>builder()
            .resolving(Identity.UUID, Instance::getUuid)
            .build();

    private boolean registered;

    private final RegistryKey<DimensionType> dimensionType;
    private final DimensionType cachedDimensionType; // Cached to prevent self-destruction if the registry is changed, and to avoid the lookups.
    private final String dimensionName;

    // World border of the instance
    private WorldBorder worldBorder;
    private double targetBorderDiameter;
    private long remainingWorldBorderTransitionTicks;

    // Tick since the creation of the instance
    private long worldAge;

    // The time of the instance
    private long time;
    private int timeRate = 1;
    private int timeSynchronizationTicks = ServerFlag.SERVER_TICKS_PER_SECOND;

    // Weather of the instance
    private Weather weather = Weather.CLEAR;
    private Weather transitioningWeather = Weather.CLEAR;
    private int remainingRainTransitionTicks;
    private int remainingThunderTransitionTicks;

    // Field for tick events
    private long lastTickAge = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

    private final EntityTracker entityTracker = new EntityTrackerImpl();

    private final ChunkCache blockRetriever = new ChunkCache(this, null, null);

    // the uuid of this instance
    protected UUID uuid;

    // instance custom data
    protected TagHandler tagHandler = TagHandler.newHandler();
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final EventNode<InstanceEvent> eventNode;

    // the explosion supplier
    private ExplosionSupplier explosionSupplier;

    /**
     * Creates a new instance.
     *
     * @param uuid          the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
     */
    public Instance(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType) {
        this(uuid, dimensionType, dimensionType.key());
    }

    /**
     * Creates a new instance.
     *
     * @param uuid          the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
     */
    public Instance(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType, @NotNull Key dimensionName) {
        this(MinecraftServer.getDimensionTypeRegistry(), uuid, dimensionType, dimensionName);
    }

    /**
     * Creates a new instance.
     *
     * @param uuid          the {@link UUID} of the instance
     * @param dimensionType the {@link DimensionType} of the instance
     */
    public Instance(@NotNull DynamicRegistry<DimensionType> dimensionTypeRegistry, @NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType, @NotNull Key dimensionName) {
        this.uuid = uuid;
        this.dimensionType = dimensionType;
        this.cachedDimensionType = dimensionTypeRegistry.get(dimensionType);
        Check.argCondition(cachedDimensionType == null, "The dimension " + dimensionType + " is not registered! Please add it to the registry (`MinecraftServer.getDimensionTypeRegistry().registry(dimensionType)`).");
        this.dimensionName = dimensionName.asString();

        this.worldBorder = WorldBorder.DEFAULT_BORDER;
        targetBorderDiameter = this.worldBorder.diameter();

        final ServerProcess process = MinecraftServer.process();
        if (process != null) {
            this.eventNode = process.eventHandler().map(this, EventFilter.INSTANCE);
        } else {
            // Local nodes require a server process
            this.eventNode = null;
        }
    }

    /**
     * Schedules a task to be run during the next instance tick.
     *
     * @param callback the task to execute during the next instance tick
     */
    public void scheduleNextTick(@NotNull Consumer<Instance> callback) {
        this.scheduler.scheduleNextTick(() -> callback.accept(this));
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block, true);
    }

    public void setBlock(@NotNull Point blockPosition, @NotNull Block block, boolean doBlockUpdates) {
        setBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(), block, doBlockUpdates);
    }

    public abstract void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates);

    @ApiStatus.Internal
    public boolean placeBlock(@NotNull BlockHandler.Placement placement) {
        return placeBlock(placement, true);
    }

    @ApiStatus.Internal
    public abstract boolean placeBlock(@NotNull BlockHandler.Placement placement, boolean doBlockUpdates);

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player        the {@link Player} who break the block
     * @param blockPosition the position of the broken block
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace) {
        return breakBlock(player, blockPosition, blockFace, true);
    }

    /**
     * Does call {@link net.minestom.server.event.player.PlayerBlockBreakEvent}
     * and send particle packets
     *
     * @param player         the {@link Player} who break the block
     * @param blockPosition  the position of the broken block
     * @param doBlockUpdates true to do block updates, false otherwise
     * @return true if the block has been broken, false if it has been cancelled
     */
    @ApiStatus.Internal
    public abstract boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates);

    /**
     * Forces the generation of a {@link Chunk}, even if no file and {@link Generator} are defined.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a {@link CompletableFuture} completed once the chunk has been loaded
     */
    public abstract @NotNull CompletableFuture<@NotNull Chunk> loadChunk(int chunkX, int chunkZ);

    /**
     * Loads the chunk at the given {@link Point} with a callback.
     *
     * @param point the chunk position
     */
    public @NotNull CompletableFuture<@NotNull Chunk> loadChunk(@NotNull Point point) {
        return loadChunk(point.chunkX(), point.chunkZ());
    }

    /**
     * Loads the chunk if the chunk is already loaded or if
     * {@link #hasEnabledAutoChunkLoad()} returns true.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a {@link CompletableFuture} completed once the chunk has been processed, can be null if not loaded
     */
    public abstract @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(int chunkX, int chunkZ);

    /**
     * Loads a {@link Chunk} (if {@link #hasEnabledAutoChunkLoad()} returns true)
     * at the given {@link Point} with a callback.
     *
     * @param point the chunk position
     * @return a {@link CompletableFuture} completed once the chunk has been processed, null if not loaded
     */
    public @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(@NotNull Point point) {
        return loadOptionalChunk(point.chunkX(), point.chunkZ());
    }

    /**
     * Schedules the removal of a {@link Chunk}, this method does not promise when it will be done.
     * <p>
     * WARNING: during unloading, all entities other than {@link Player} will be removed.
     *
     * @param chunk the chunk to unload
     */
    public abstract void unloadChunk(@NotNull Chunk chunk);

    /**
     * Unloads the chunk at the given position.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     */
    public void unloadChunk(int chunkX, int chunkZ) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        Check.notNull(chunk, "The chunk at {0}:{1} is already unloaded", chunkX, chunkZ);
        unloadChunk(chunk);
    }

    /**
     * Gets the loaded {@link Chunk} at a position.
     * <p>
     * WARNING: this should only return already-loaded chunk, use {@link #loadChunk(int, int)} or similar to load one instead.
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk at the specified position, null if not loaded
     */
    public abstract @Nullable Chunk getChunk(int chunkX, int chunkZ);

    /**
     * @param chunkX the chunk X
     * @param chunkZ this chunk Z
     * @return true if the chunk is loaded
     */
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ) != null;
    }

    /**
     * @param point coordinate of a block or other
     * @return true if the chunk is loaded
     */
    public boolean isChunkLoaded(Point point) {
        return isChunkLoaded(point.chunkX(), point.chunkZ());
    }

    /**
     * Saves the current instance tags.
     * <p>
     * Warning: only the global instance data will be saved, not chunks.
     * You would need to call {@link #saveChunksToStorage()} too.
     *
     * @return the future called once the instance data has been saved
     */
    public abstract @NotNull CompletableFuture<Void> saveInstance();

    /**
     * Saves a {@link Chunk} to permanent storage.
     *
     * @param chunk the {@link Chunk} to save
     * @return future called when the chunk is done saving
     */
    public abstract @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk);

    /**
     * Saves multiple chunks to permanent storage.
     *
     * @return future called when the chunks are done saving
     */
    public abstract @NotNull CompletableFuture<Void> saveChunksToStorage();

    public abstract void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier);

    /**
     * Gets the chunk supplier of the instance.
     *
     * @return the chunk supplier of the instance
     */
    public abstract ChunkSupplier getChunkSupplier();

    /**
     * Gets the generator associated with the instance
     *
     * @return the generator if any
     */
    public abstract @Nullable Generator generator();

    /**
     * Changes the generator of the instance
     *
     * @param generator the new generator, or null to disable generation
     */
    public abstract void setGenerator(@Nullable Generator generator);

    /**
     * Gets all the instance's loaded chunks.
     *
     * @return an unmodifiable containing all the instance chunks
     */
    public abstract @NotNull Collection<@NotNull Chunk> getChunks();

    /**
     * When set to true, chunks will load automatically when requested.
     * Otherwise using {@link #loadChunk(int, int)} will be required to even spawn a player
     *
     * @param enable enable the auto chunk load
     */
    public abstract void enableAutoChunkLoad(boolean enable);

    /**
     * Gets if the instance should auto load chunks.
     *
     * @return true if auto chunk load is enabled, false otherwise
     */
    public abstract boolean hasEnabledAutoChunkLoad();

    /**
     * Determines whether a position in the void.
     *
     * @param point the point in the world
     * @return true if the point is inside the void
     */
    public abstract boolean isInVoid(@NotNull Point point);

    /**
     * Gets if the instance has been registered in {@link InstanceManager}.
     *
     * @return true if the instance has been registered
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Changes the registered field.
     * <p>
     * WARNING: should only be used by {@link InstanceManager}.
     *
     * @param registered true to mark the instance as registered
     */
    protected void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * Gets the instance {@link DimensionType}.
     *
     * @return the dimension of the instance
     */
    public RegistryKey<DimensionType> getDimensionType() {
        return dimensionType;
    }

    @ApiStatus.Internal
    public @NotNull DimensionType getCachedDimensionType() {
        return cachedDimensionType;
    }

    /**
     * Gets the instance dimension name.
     *
     * @return the dimension name of the instance
     */
    public @NotNull String getDimensionName() {
        return dimensionName;
    }

    /**
     * Gets the age of this instance in tick.
     *
     * @return the age of this instance in tick
     */
    public long getWorldAge() {
        return worldAge;
    }

    /**
     * Sets the age of this instance in tick. It will send the age to all players.
     * Will send new age to all players in the instance, unaffected by {@link #getTimeSynchronizationTicks()}
     *
     * @param worldAge the age of this instance in tick
     */
    public void setWorldAge(long worldAge) {
        this.worldAge = worldAge;
        PacketSendingUtils.sendGroupedPacket(getPlayers(), createTimePacket());
    }

    /**
     * Gets the current time in the instance (sun/moon).
     *
     * @return the time in the instance
     */
    public long getTime() {
        return time;
    }

    /**
     * Changes the current time in the instance, from 0 to 24000.
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
     * It does send the new time to all players in the instance, unaffected by {@link #getTimeSynchronizationTicks()}
     *
     * @param time the new time of the instance
     */
    public void setTime(long time) {
        this.time = time;
        PacketSendingUtils.sendGroupedPacket(getPlayers(), createTimePacket());
    }

    /**
     * Gets the rate of the time passing, it is 1 by default
     *
     * @return the time rate of the instance
     */
    public int getTimeRate() {
        return timeRate;
    }

    /**
     * Changes the time rate of the instance
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
     * Gets the rate at which the client is updated with the current instance time
     *
     * @return the client update rate for time related packet
     */
    public int getTimeSynchronizationTicks() {
        return timeSynchronizationTicks;
    }

    /**
     * Changes the natural client time packet synchronization period, defaults to {@link ServerFlag#SERVER_TICKS_PER_SECOND}.
     * <p>
     * Supplying 0 means that the client will never be synchronized with the current natural instance time
     * (time will still change server-side)
     *
     * @param timeSynchronizationTicks the rate to update time in ticks
     */
    public void setTimeSynchronizationTicks(int timeSynchronizationTicks) {
        Check.stateCondition(timeSynchronizationTicks < 0, "The time Synchronization ticks cannot be lower than 0");
        this.timeSynchronizationTicks = timeSynchronizationTicks;
    }

    /**
     * Creates a {@link TimeUpdatePacket} with the current age and time of this instance
     *
     * @return the {@link TimeUpdatePacket} with this instance data
     */
    @ApiStatus.Internal
    public @NotNull TimeUpdatePacket createTimePacket() {
        return new TimeUpdatePacket(worldAge, time, timeRate != 0);
    }

    /**
     * Gets the current state of the instance {@link WorldBorder}.
     *
     * @return the {@link WorldBorder} for the instance of the current tick
     */
    public @NotNull WorldBorder getWorldBorder() {
        return worldBorder;
    }

    /**
     * Set the instance {@link WorldBorder} with a smooth transition.
     *
     * @param worldBorder    the desired final state of the world border
     * @param transitionTime the time in seconds this world border's diameter
     *                       will transition for (0 makes this instant)
     */
    public void setWorldBorder(@NotNull WorldBorder worldBorder, double transitionTime) {
        Check.stateCondition(transitionTime < 0, "Transition time cannot be lower than 0");
        long transitionMilliseconds = (long) (transitionTime * 1000);
        sendNewWorldBorderPackets(worldBorder, transitionMilliseconds);

        this.targetBorderDiameter = worldBorder.diameter();
        long transitionTicks = transitionMilliseconds / MinecraftServer.TICK_MS;
        remainingWorldBorderTransitionTicks = transitionTicks;
        if (transitionTicks == 0) this.worldBorder = worldBorder;
        else this.worldBorder = worldBorder.withDiameter(this.worldBorder.diameter());
    }

    /**
     * Set the instance {@link WorldBorder} with an instant transition.
     * see {@link Instance#setWorldBorder(WorldBorder, double)}.
     */
    public void setWorldBorder(@NotNull WorldBorder worldBorder) {
        setWorldBorder(worldBorder, 0);
    }

    /**
     * Creates the {@link InitializeWorldBorderPacket} sent to players who join this instance.
     */
    public @NotNull InitializeWorldBorderPacket createInitializeWorldBorderPacket() {
        return worldBorder.createInitializePacket(targetBorderDiameter, remainingWorldBorderTransitionTicks * MinecraftServer.TICK_MS);
    }

    private void sendNewWorldBorderPackets(@NotNull WorldBorder newBorder, long transitionMilliseconds) {
        // Only send the relevant border packets
        if (this.worldBorder.diameter() != newBorder.diameter()) {
            if (transitionMilliseconds == 0) sendGroupedPacket(newBorder.createSizePacket());
            else sendGroupedPacket(this.worldBorder.createLerpSizePacket(newBorder.diameter(), transitionMilliseconds));
        }
        if (this.worldBorder.centerX() != newBorder.centerX() || this.worldBorder.centerZ() != newBorder.centerZ()) {
            sendGroupedPacket(newBorder.createCenterPacket());
        }
        if (this.worldBorder.warningTime() != newBorder.warningTime())
            sendGroupedPacket(newBorder.createWarningDelayPacket());
        if (this.worldBorder.warningDistance() != newBorder.warningDistance())
            sendGroupedPacket(newBorder.createWarningReachPacket());
    }

    private @NotNull WorldBorder transitionWorldBorder(long remainingTicks) {
        if (remainingTicks <= 1) return worldBorder.withDiameter(targetBorderDiameter);
        return worldBorder.withDiameter(worldBorder.diameter() + (targetBorderDiameter - worldBorder.diameter()) * (1 / (double) remainingTicks));
    }

    /**
     * Gets the entities in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the entities in the instance
     */
    public @NotNull Set<@NotNull Entity> getEntities() {
        return entityTracker.entities();
    }

    /**
     * Gets an entity based on its id (from {@link Entity#getEntityId()}).
     *
     * @param id the entity id
     * @return the entity having the specified id, null if not found
     */
    public @Nullable Entity getEntityById(int id) {
        return entityTracker.getEntityById(id);
    }

    /**
     * Gets an entity based on its UUID (from {@link Entity#getUuid()}).
     *
     * @param uuid the entity UUID
     * @return the entity having the specified uuid, null if not found
     */
    public @Nullable Entity getEntityByUuid(UUID uuid) {
        return entityTracker.getEntityByUuid(uuid);
    }

    /**
     * Gets a player based on its UUID (from {@link Entity#getUuid()}).
     *
     * @param uuid the player UUID
     * @return the player having the specified uuid, null if not found or not a player
     */
    public @Nullable Player getPlayerByUuid(UUID uuid) {
        Entity entity = entityTracker.getEntityByUuid(uuid);
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    /**
     * Gets the players in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the players in the instance
     */
    @Override
    public @NotNull Set<@NotNull Player> getPlayers() {
        return entityTracker.entities(EntityTracker.Target.PLAYERS);
    }

    /**
     * Gets the creatures in the instance;
     *
     * @return an unmodifiable {@link Set} containing all the creatures in the instance
     */
    @Deprecated
    public @NotNull Set<@NotNull EntityCreature> getCreatures() {
        return entityTracker.entities().stream()
                .filter(EntityCreature.class::isInstance)
                .map(entity -> (EntityCreature) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the experience orbs in the instance.
     *
     * @return an unmodifiable {@link Set} containing all the experience orbs in the instance
     */
    @Deprecated
    public @NotNull Set<@NotNull ExperienceOrb> getExperienceOrbs() {
        return entityTracker.entities().stream()
                .filter(ExperienceOrb.class::isInstance)
                .map(entity -> (ExperienceOrb) entity)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Gets the entities located in the chunk.
     *
     * @param chunk the chunk to get the entities from
     * @return an unmodifiable {@link Set} containing all the entities in a chunk,
     * if {@code chunk} is unloaded, return an empty {@link HashSet}
     */
    public @NotNull Set<@NotNull Entity> getChunkEntities(Chunk chunk) {
        var chunkEntities = entityTracker.chunkEntities(chunk.toPosition(), EntityTracker.Target.ENTITIES);
        return ObjectArraySet.ofUnchecked(chunkEntities.toArray(Entity[]::new));
    }

    /**
     * Gets nearby entities to the given position.
     *
     * @param point position to look at
     * @param range max range from the given point to collect entities at
     * @return entities that are not further than the specified distance from the transmitted position.
     */
    public @NotNull Collection<Entity> getNearbyEntities(@NotNull Point point, double range) {
        List<Entity> result = new ArrayList<>();
        this.entityTracker.nearbyEntities(point, range, EntityTracker.Target.ENTITIES, result::add);
        return result;
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final Block block = blockRetriever.getBlock(x, y, z, condition);
        if (block == null) throw new NullPointerException("Unloaded chunk at " + x + "," + y + "," + z);
        return block;
    }

    /**
     * Sends a {@link BlockActionPacket} for all the viewers of the specific position.
     *
     * @param blockPosition the block position
     * @param actionId      the action id, depends on the block
     * @param actionParam   the action parameter, depends on the block
     * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Block_Action">BlockActionPacket</a> for the action id &amp; param
     */
    public void sendBlockAction(@NotNull Point blockPosition, byte actionId, byte actionParam) {
        final Block block = getBlock(blockPosition);
        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "The chunk at {0} is not loaded!", blockPosition);
        chunk.sendPacketToViewers(new BlockActionPacket(blockPosition, actionId, actionParam, block));
    }

    /**
     * Gets the {@link Chunk} at the given block position, null if not loaded.
     *
     * @param x the X position
     * @param z the Z position
     * @return the chunk at the given position, null if not loaded
     */
    public @Nullable Chunk getChunkAt(double x, double z) {
        return getChunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z));
    }

    /**
     * Gets the {@link Chunk} at the given {@link Point}, null if not loaded.
     *
     * @param point the position
     * @return the chunk at the given position, null if not loaded
     */
    public @Nullable Chunk getChunkAt(@NotNull Point point) {
        return getChunk(point.chunkX(), point.chunkZ());
    }

    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    /**
     * Gets the instance unique id.
     *
     * @return the instance unique id
     */
    public @NotNull UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the instance unique id.
     *
     * @return the instance unique id
     * @deprecated Replace with {@link Instance#getUuid()}
     */
    @Deprecated(forRemoval = true)
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    /**
     * Performs a single tick in the instance, including scheduled tasks from {@link #scheduleNextTick(Consumer)}.
     * <p>
     * Warning: this does not update chunks and entities.
     *
     * @param time the tick time in milliseconds, which may only be used as a delta and has no meaning in real life
     */
    @Override
    public void tick(long time) {
        // Scheduled tasks
        this.scheduler.processTick();
        // Time
        {
            this.worldAge++;
            this.time += timeRate;
            // time needs to be sent to players
            if (timeSynchronizationTicks > 0 && this.worldAge % timeSynchronizationTicks == 0) {
                PacketSendingUtils.sendGroupedPacket(getPlayers(), createTimePacket());
            }

        }
        // Weather
        if (remainingRainTransitionTicks > 0 || remainingThunderTransitionTicks > 0) {
            Weather previousWeather = transitioningWeather;
            transitioningWeather = transitionWeather(remainingRainTransitionTicks, remainingThunderTransitionTicks);
            sendWeatherPackets(previousWeather);
            remainingRainTransitionTicks = Math.max(0, remainingRainTransitionTicks - 1);
            remainingThunderTransitionTicks = Math.max(0, remainingThunderTransitionTicks - 1);
        }
        // Tick event
        {
            // Process tick events
            EventDispatcher.call(new InstanceTickEvent(this, time, lastTickAge));
            // Set last tick age
            this.lastTickAge = time;
        }
        // World border
        if (remainingWorldBorderTransitionTicks > 0) {
            worldBorder = transitionWorldBorder(remainingWorldBorderTransitionTicks);
            if (worldBorder.diameter() == targetBorderDiameter) remainingWorldBorderTransitionTicks = 0;
            else remainingWorldBorderTransitionTicks--;
        }
        // End of tick scheduled tasks
        this.scheduler.processTickEnd();
    }

    /**
     * Gets the weather of this instance
     *
     * @return the instance weather
     */
    public @NotNull Weather getWeather() {
        return weather;
    }

    /**
     * Sets the weather on this instance, transitions over time
     *
     * @param weather         the new weather
     * @param transitionTicks the ticks to transition to new weather
     */
    public void setWeather(@NotNull Weather weather, int transitionTicks) {
        Check.stateCondition(transitionTicks < 1, "Transition ticks cannot be lower than 0");
        this.weather = weather;
        remainingRainTransitionTicks = transitionTicks;
        remainingThunderTransitionTicks = transitionTicks;
    }

    /**
     * Sets the weather of this instance with a fixed transition
     *
     * @param weather the new weather
     */
    public void setWeather(@NotNull Weather weather) {
        this.weather = weather;
        remainingRainTransitionTicks = (int) Math.max(1, Math.abs((this.weather.rainLevel() - transitioningWeather.rainLevel()) / 0.01));
        remainingThunderTransitionTicks = (int) Math.max(1, Math.abs((this.weather.thunderLevel() - transitioningWeather.thunderLevel()) / 0.01));
    }

    private void sendWeatherPackets(@NotNull Weather previousWeather) {
        boolean toggledRain = (transitioningWeather.isRaining() != previousWeather.isRaining());
        if (toggledRain) sendGroupedPacket(transitioningWeather.createIsRainingPacket());
        if (transitioningWeather.rainLevel() != previousWeather.rainLevel())
            sendGroupedPacket(transitioningWeather.createRainLevelPacket());
        if (transitioningWeather.thunderLevel() != previousWeather.thunderLevel())
            sendGroupedPacket(transitioningWeather.createThunderLevelPacket());
    }

    private @NotNull Weather transitionWeather(int remainingRainTransitionTicks, int remainingThunderTransitionTicks) {
        Weather target = weather;
        Weather current = transitioningWeather;
        float rainLevel = current.rainLevel() + (target.rainLevel() - current.rainLevel()) * (1 / (float) Math.max(1, remainingRainTransitionTicks));
        float thunderLevel = current.thunderLevel() + (target.thunderLevel() - current.thunderLevel()) * (1 / (float) Math.max(1, remainingThunderTransitionTicks));
        return new Weather(rainLevel, thunderLevel);
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tagHandler;
    }

    @Override
    public @NotNull Scheduler scheduler() {
        return scheduler;
    }

    @Override
    @ApiStatus.Experimental
    public @NotNull EventNode<InstanceEvent> eventNode() {
        return eventNode;
    }

    @Override
    public @NotNull InstanceSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        final Map<Long, AtomicReference<ChunkSnapshot>> chunksMap = updater.referencesMapLong(getChunks(),
                value -> CoordConversion.chunkIndex(value.getChunkX(), value.getChunkZ()));
        final int[] entities = ArrayUtils.mapToIntArray(entityTracker.entities(), Entity::getEntityId);
        return new SnapshotImpl.Instance(updater.reference(MinecraftServer.process()),
                getDimensionType(), getWorldAge(), getTime(), chunksMap, entities,
                tagHandler.readableCopy());
    }

    /**
     * Plays a {@link Sound} at a given point, except to the excluded player
     *
     * @param excludedPlayer The player in the instance who won't receive the sound
     * @param sound          The sound to play
     * @param point          The point in this instance at which to play the sound
     */
    public void playSoundExcept(@Nullable Player excludedPlayer, @NotNull Sound sound, @NotNull Point point) {
        playSoundExcept(excludedPlayer, sound, point.x(), point.y(), point.z());
    }

    public void playSoundExcept(@Nullable Player excludedPlayer, @NotNull Sound sound, double x, double y, double z) {
        ServerPacket packet = AdventurePacketConvertor.createSoundPacket(sound, x, y, z);
        PacketSendingUtils.sendGroupedPacket(getPlayers(), packet, p -> p != excludedPlayer);
    }

    public void playSoundExcept(@Nullable Player excludedPlayer, @NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        if (emitter != Sound.Emitter.self()) {
            ServerPacket packet = AdventurePacketConvertor.createSoundPacket(sound, emitter);
            PacketSendingUtils.sendGroupedPacket(getPlayers(), packet, p -> p != excludedPlayer);
        } else {
            // if we're playing on self, we need to delegate to each audience member
            for (Audience audience : this.audiences()) {
                if (audience == excludedPlayer) continue;
                audience.playSound(sound, emitter);
            }
        }
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
    public void explode(float centerX, float centerY, float centerZ, float strength, @Nullable CompoundBinaryTag additionalData) {
        final ExplosionSupplier explosionSupplier = getExplosionSupplier();
        Check.stateCondition(explosionSupplier == null, "Tried to create an explosion with no explosion supplier");
        final Explosion explosion = explosionSupplier.createExplosion(centerX, centerY, centerZ, strength, additionalData);
        explosion.apply(this);
    }

    /**
     * Gets the registered {@link ExplosionSupplier}, or null if none was provided.
     *
     * @return the instance explosion supplier, null if none was provided
     */
    public @Nullable ExplosionSupplier getExplosionSupplier() {
        return explosionSupplier;
    }

    /**
     * Registers the {@link ExplosionSupplier} to use in this instance.
     *
     * @param supplier the explosion supplier
     */
    public void setExplosionSupplier(@Nullable ExplosionSupplier supplier) {
        this.explosionSupplier = supplier;
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pointers pointers() {
        return INSTANCE_POINTERS_SUPPLIER.view(this);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Identity identity() {
        return Identity.identity(this.uuid); // Warning, do not pull up until this.uuid is final
    }

    public int getBlockLight(int blockX, int blockY, int blockZ) {
        var chunk = getChunkAt(blockX, blockZ);
        if (chunk == null) return 0;
        Section section = chunk.getSectionAt(blockY);
        Light light = section.blockLight();
        int sectionCoordinate = CoordConversion.globalToChunk(blockY);

        int coordX = CoordConversion.globalToSectionRelative(blockX);
        int coordY = CoordConversion.globalToSectionRelative(blockY);
        int coordZ = CoordConversion.globalToSectionRelative(blockZ);

        if (light.requiresUpdate())
            LightingChunk.relightSection(chunk.getInstance(), chunk.chunkX, sectionCoordinate, chunk.chunkZ);
        return light.getLevel(coordX, coordY, coordZ);
    }

    public int getSkyLight(int blockX, int blockY, int blockZ) {
        var chunk = getChunkAt(blockX, blockZ);
        if (chunk == null) return 0;
        Section section = chunk.getSectionAt(blockY);
        Light light = section.skyLight();
        int sectionCoordinate = CoordConversion.globalToChunk(blockY);

        int coordX = CoordConversion.globalToSectionRelative(blockX);
        int coordY = CoordConversion.globalToSectionRelative(blockY);
        int coordZ = CoordConversion.globalToSectionRelative(blockZ);

        if (light.requiresUpdate())
            LightingChunk.relightSection(chunk.getInstance(), chunk.chunkX, sectionCoordinate, chunk.chunkZ);
        return light.getLevel(coordX, coordY, coordZ);
    }
}