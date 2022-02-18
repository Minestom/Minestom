package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.ViewEngine;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.position.PositionUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import space.vectrix.flare.fastutil.Int2ObjectSyncMap;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Could be a player, a monster, or an object.
 * <p>
 * To create your own entity you probably want to extends {@link LivingEntity} or {@link EntityCreature} instead.
 */
public class Entity implements Viewable, Tickable, Schedulable, TagHandler, PermissionHandler, HoverEventSource<ShowEntity>, Sound.Emitter {

    private static final Int2ObjectSyncMap<Entity> ENTITY_BY_ID = Int2ObjectSyncMap.hashmap();
    private static final Map<UUID, Entity> ENTITY_BY_UUID = new ConcurrentHashMap<>();
    private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

    private final CachedPacket destroyPacketCache = new CachedPacket(() -> new DestroyEntitiesPacket(getEntityId()));

    protected Instance instance;
    protected Chunk currentChunk;
    protected Pos position;
    protected Pos previousPosition;
    protected Pos lastSyncedPosition;
    protected boolean onGround;

    private BoundingBox boundingBox;

    protected Entity vehicle;

    // Velocity
    protected Vec velocity = Vec.ZERO; // Movement in block per second
    protected boolean hasPhysics = true;

    /**
     * The amount of drag applied on the Y axle.
     * <p>
     * Unit: 1/tick
     */
    protected double gravityDragPerTick;
    /**
     * Acceleration on the Y axle due to gravity
     * <p>
     * Unit: blocks/tick
     */
    protected double gravityAcceleration;
    protected int gravityTickCount; // Number of tick where gravity tick was applied

    private final int id;
    // Players must be aware of all surrounding entities
    // General entities should only be aware of surrounding players to update their viewing list
    private final EntityTracker.Target<Entity> trackingTarget = this instanceof Player ?
            EntityTracker.Target.ENTITIES : EntityTracker.Target.class.cast(EntityTracker.Target.PLAYERS);
    protected final EntityTracker.Update<Entity> trackingUpdate = new EntityTracker.Update<>() {
        @Override
        public void add(@NotNull Entity entity) {
            viewEngine.handleAutoViewAddition(entity);
        }

        @Override
        public void remove(@NotNull Entity entity) {
            viewEngine.handleAutoViewRemoval(entity);
        }

        @Override
        public void referenceUpdate(@NotNull Point point, @Nullable EntityTracker tracker) {
            final Instance currentInstance = tracker != null ? instance : null;
            assert currentInstance == null || currentInstance.getEntityTracker() == tracker :
                    "EntityTracker does not match current instance";
            viewEngine.updateTracker(currentInstance, point);
        }
    };

    protected final ViewEngine viewEngine = new ViewEngine(this,
            player -> {
                // Add viewable
                var lock1 = player.getEntityId() < getEntityId() ? player : this;
                var lock2 = lock1 == this ? player : this;
                synchronized (lock1.viewEngine.mutex()) {
                    synchronized (lock2.viewEngine.mutex()) {
                        if (!Entity.this.viewEngine.viewableOption.predicate(player) ||
                                !player.viewEngine.viewerOption.predicate(this)) return;
                        Entity.this.viewEngine.viewableOption.register(player);
                        player.viewEngine.viewerOption.register(this);
                    }
                }
                updateNewViewer(player);
            },
            player -> {
                // Remove viewable
                var lock1 = player.getEntityId() < getEntityId() ? player : this;
                var lock2 = lock1 == this ? player : this;
                synchronized (lock1.viewEngine.mutex()) {
                    synchronized (lock2.viewEngine.mutex()) {
                        Entity.this.viewEngine.viewableOption.unregister(player);
                        player.viewEngine.viewerOption.unregister(this);
                    }
                }
                updateOldViewer(player);
            },
            this instanceof Player player ? entity -> entity.viewEngine.viewableOption.addition.accept(player) : null,
            this instanceof Player player ? entity -> entity.viewEngine.viewableOption.removal.accept(player) : null);
    protected final Set<Player> viewers = viewEngine.asSet();
    private final MutableNBTCompound nbtCompound = new MutableNBTCompound();
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();

    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean removed;

    private final Set<Entity> passengers = new CopyOnWriteArraySet<>();
    protected EntityType entityType; // UNSAFE to change, modify at your own risk

    // Network synchronization, send the absolute position of the entity each X milliseconds
    private static final Duration SYNCHRONIZATION_COOLDOWN = Duration.of(1, TimeUnit.MINUTE);
    private Duration customSynchronizationCooldown;
    private long lastAbsoluteSynchronizationTime;

    protected Metadata metadata = new Metadata(this);
    protected EntityMeta entityMeta;

    private final List<TimedPotion> effects = new CopyOnWriteArrayList<>();

    // Tick related
    private long ticks;

    private final Acquirable<Entity> acquirable = Acquirable.of(this);

    public Entity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = uuid;
        this.position = Pos.ZERO;
        this.previousPosition = Pos.ZERO;
        this.lastSyncedPosition = Pos.ZERO;

        setBoundingBox(entityType.width(), entityType.height(), entityType.width());

        this.entityMeta = EntityTypeImpl.createMeta(entityType, this, this.metadata);

        Entity.ENTITY_BY_ID.put(id, this);
        Entity.ENTITY_BY_UUID.put(uuid, this);

        this.gravityAcceleration = entityType.registry().acceleration();
        this.gravityDragPerTick = entityType.registry().drag();
    }

    public Entity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * Schedules a task to be run during the next entity tick.
     *
     * @param callback the task to execute during the next entity tick
     */
    public void scheduleNextTick(@NotNull Consumer<Entity> callback) {
        this.scheduler.scheduleNextTick(() -> callback.accept(this));
    }

    /**
     * Gets an entity based on its id (from {@link #getEntityId()}).
     * <p>
     * Entity id are unique server-wide.
     *
     * @param id the entity unique id
     * @return the entity having the specified id, null if not found
     */
    public static @Nullable Entity getEntity(int id) {
        return Entity.ENTITY_BY_ID.get(id);
    }

    /**
     * Gets an entity based on its UUID (from {@link #getUuid()}).
     *
     * @param uuid the entity UUID
     * @return the entity having the specified uuid, null if not found
     */
    public static @Nullable Entity getEntity(@NotNull UUID uuid) {
        return Entity.ENTITY_BY_UUID.getOrDefault(uuid, null);
    }


    /**
     * Generate and return a new unique entity id.
     * <p>
     * Useful if you want to spawn entities using packet but don't risk to have duplicated id.
     *
     * @return a newly generated entity id
     */
    public static int generateId() {
        return LAST_ENTITY_ID.incrementAndGet();
    }

    /**
     * Called each tick.
     *
     * @param time time of the update in milliseconds
     */
    public void update(long time) {

    }

    /**
     * Called when a new instance is set.
     */
    public void spawn() {

    }

    public boolean isOnGround() {
        return onGround || EntityUtils.isOnGround(this) /* backup for levitating entities */;
    }

    /**
     * Gets metadata of this entity.
     * You may want to cast it to specific implementation.
     *
     * @return metadata of this entity.
     */
    public @NotNull EntityMeta getEntityMeta() {
        return this.entityMeta;
    }

    /**
     * Teleports the entity only if the chunk at {@code position} is loaded or if
     * {@link Instance#hasEnabledAutoChunkLoad()} returns true.
     *
     * @param position the teleport position
     * @param chunks   the chunk indexes to load before teleporting the entity,
     *                 indexes are from {@link ChunkUtils#getChunkIndex(int, int)},
     *                 can be null or empty to only load the chunk at {@code position}
     * @throws IllegalStateException if you try to teleport an entity before settings its instance
     */
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks) {
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");
        final Runnable endCallback = () -> {
            this.previousPosition = this.position;
            this.position = position;
            refreshCoordinate(position);
            synchronizePosition(true);
        };

        if (chunks != null && chunks.length > 0) {
            // Chunks need to be loaded before the teleportation can happen
            return ChunkUtils.optionalLoadAll(instance, chunks, null).thenRun(endCallback);
        }
        final Pos currentPosition = this.position;
        if (!currentPosition.sameChunk(position)) {
            // Ensure that the chunk is loaded
            return instance.loadOptionalChunk(position).thenRun(endCallback);
        } else {
            // Position is in the same chunk, keep it sync
            endCallback.run();
            return AsyncUtils.empty();
        }
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position) {
        return teleport(position, null);
    }

    /**
     * Changes the view of the entity.
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    public void setView(float yaw, float pitch) {
        this.position = position.withView(yaw, pitch);
        sendPacketToViewersAndSelf(new EntityHeadLookPacket(getEntityId(), yaw));
        sendPacketToViewersAndSelf(new EntityRotationPacket(getEntityId(), yaw, pitch, onGround));
    }

    /**
     * Changes the view of the entity so that it looks in a direction to the given position.
     *
     * @param position the position to look at.
     */
    public void lookAt(@NotNull Pos position) {
        Vec delta = position.sub(getPosition()).asVec().normalize();
        setView(
                PositionUtils.getLookYaw(delta.x(), delta.z()),
                PositionUtils.getLookPitch(delta.x(), delta.y(), delta.z())
        );
    }

    /**
     * Changes the view of the entity so that it looks in a direction to the given entity.
     *
     * @param entity the entity to look at.
     */
    public void lookAt(@NotNull Entity entity) {
        Check.argCondition(entity.instance != instance, "Entity can look at another entity that is within it's own instance");
        lookAt(entity.position);
    }

    /**
     * Gets if this entity is automatically sent to surrounding players.
     * True by default.
     *
     * @return true if the entity is automatically viewable for close players, false otherwise
     */
    public boolean isAutoViewable() {
        return viewEngine.viewableOption.isAuto();
    }

    /**
     * Decides if this entity should be auto-viewable by nearby players.
     *
     * @param autoViewable true to add surrounding players, false to remove
     * @see #isAutoViewable()
     */
    public void setAutoViewable(boolean autoViewable) {
        this.viewEngine.viewableOption.updateAuto(autoViewable);
    }

    @ApiStatus.Experimental
    public void updateViewableRule(@NotNull Predicate<Player> predicate) {
        this.viewEngine.viewableOption.updateRule(predicate);
    }

    @ApiStatus.Experimental
    public void updateViewableRule() {
        this.viewEngine.viewableOption.updateRule();
    }

    /**
     * Gets if surrounding entities are automatically visible by this.
     * True by default.
     *
     * @return true if surrounding entities are visible by this
     */
    @ApiStatus.Experimental
    public boolean autoViewEntities() {
        return viewEngine.viewerOption.isAuto();
    }

    /**
     * Decides if surrounding entities must be visible.
     *
     * @param autoViewer true to add view surrounding entities, false to remove
     */
    @ApiStatus.Experimental
    public void setAutoViewEntities(boolean autoViewer) {
        this.viewEngine.viewerOption.updateAuto(autoViewer);
    }

    @ApiStatus.Experimental
    public void updateViewerRule(@NotNull Predicate<Entity> predicate) {
        this.viewEngine.viewerOption.updateRule(predicate);
    }

    @ApiStatus.Experimental
    public void updateViewerRule() {
        this.viewEngine.viewerOption.updateRule();
    }

    @Override
    public final boolean addViewer(@NotNull Player player) {
        if (!viewEngine.manualAdd(player)) return false;
        updateNewViewer(player);
        return true;
    }

    @Override
    public final boolean removeViewer(@NotNull Player player) {
        if (!viewEngine.manualRemove(player)) return false;
        updateOldViewer(player);
        return true;
    }

    /**
     * Called when a new viewer must be shown.
     * Method can be subject to deadlocking if the target's viewers are also accessed.
     *
     * @param player the player to send the packets to
     */
    @ApiStatus.Internal
    public void updateNewViewer(@NotNull Player player) {
        player.sendPacket(getEntityType().registry().spawnType().getSpawnPacket(this));
        if (hasVelocity()) player.sendPacket(getVelocityPacket());
        player.sendPacket(new LazyPacket(this::getMetadataPacket));
        // Passengers
        final Set<Entity> passengers = this.passengers;
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger != player) passenger.viewEngine.viewableOption.addition.accept(player);
            }
            player.sendPacket(getPassengersPacket());
        }
        // Head position
        player.sendPacket(new EntityHeadLookPacket(getEntityId(), position.yaw()));
    }

    /**
     * Called when a viewer must be destroyed.
     * Method can be subject to deadlocking if the target's viewers are also accessed.
     *
     * @param player the player to send the packets to
     */
    @ApiStatus.Internal
    public void updateOldViewer(@NotNull Player player) {
        final Set<Entity> passengers = this.passengers;
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger != player) passenger.viewEngine.viewableOption.removal.accept(player);
            }
        }
        player.sendPacket(destroyPacketCache);
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return viewers;
    }

    /**
     * Gets if this entity's viewers (surrounding players) can be predicted from surrounding chunks.
     */
    public boolean hasPredictableViewers() {
        return viewEngine.hasPredictableViewers();
    }

    /**
     * Changes the entity type of this entity.
     * <p>
     * Works by changing the internal entity type field and by calling {@link #removeViewer(Player)}
     * followed by {@link #addViewer(Player)} to all current viewers.
     * <p>
     * Be aware that this only change the visual of the entity, the {@link net.minestom.server.collision.BoundingBox}
     * will not be modified.
     *
     * @param entityType the new entity type
     */
    public synchronized void switchEntityType(@NotNull EntityType entityType) {
        this.entityType = entityType;
        this.metadata = new Metadata(this);
        this.entityMeta = EntityTypeImpl.createMeta(entityType, this, this.metadata);

        Set<Player> viewers = new HashSet<>(getViewers());
        getViewers().forEach(this::updateOldViewer);
        viewers.forEach(this::updateNewViewer);
    }

    @NotNull
    @Override
    public Set<Permission> getAllPermissions() {
        return permissions;
    }

    /**
     * Updates the entity, called every tick.
     * <p>
     * Ignored if {@link #getInstance()} returns null.
     *
     * @param time the update time in milliseconds
     */
    @Override
    public void tick(long time) {
        if (instance == null || isRemoved() || !ChunkUtils.isLoaded(currentChunk))
            return;

        // scheduled tasks
        this.scheduler.processTick();
        if (isRemoved()) return;

        // Entity tick
        {
            // Cache the number of "gravity tick"
            velocityTick();

            // handle block contacts
            touchTick();

            handleVoid();

            // Call the abstract update method
            update(time);

            ticks++;
            EventDispatcher.call(new EntityTickEvent(this));

            // remove expired effects
            effectTick(time);
        }
        // Scheduled synchronization
        if (!Cooldown.hasCooldown(time, lastAbsoluteSynchronizationTime, getSynchronizationCooldown())) {
            synchronizePosition(false);
        }
    }

    private void velocityTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (PlayerUtils.isSocketClient(this)) return;
        if (vehicle != null) return;

        final boolean noGravity = hasNoGravity();
        final boolean hasVelocity = hasVelocity();
        if (!hasVelocity && noGravity) {
            return;
        }
        final float tps = MinecraftServer.TICK_PER_SECOND;
        final Vec currentVelocity = getVelocity();
        final Vec deltaPos = new Vec(
                currentVelocity.x() / tps,
                currentVelocity.y() / tps - (noGravity ? 0 : gravityAcceleration),
                currentVelocity.z() / tps
        );

        final Pos newPosition;
        final Vec newVelocity;
        if (this.hasPhysics) {
            final var physicsResult = CollisionUtils.handlePhysics(this, deltaPos);
            this.onGround = physicsResult.isOnGround();
            newPosition = physicsResult.newPosition();
            newVelocity = physicsResult.newVelocity();
        } else {
            newVelocity = deltaPos;
            newPosition = position.add(currentVelocity.div(20));
        }

        // World border collision
        final var finalVelocityPosition = CollisionUtils.applyWorldBorder(instance, position, newPosition);
        if (finalVelocityPosition.samePoint(position)) {
            this.velocity = Vec.ZERO;
            if (hasVelocity) {
                sendPacketToViewers(getVelocityPacket());
            }
            return;
        }
        final Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, finalVelocityPosition);
        if (!ChunkUtils.isLoaded(finalChunk)) {
            // Entity shouldn't be updated when moving in an unloaded chunk
            return;
        }

        if (entityType == EntityTypes.ITEM || entityType == EntityType.FALLING_BLOCK) {
            // TODO find other exceptions
            this.previousPosition = this.position;
            this.position = finalVelocityPosition;
            refreshCoordinate(finalVelocityPosition);
        } else {
            refreshPosition(finalVelocityPosition, true);
        }

        // Update velocity
        if (hasVelocity || !newVelocity.isZero()) {
            final double airDrag = this instanceof LivingEntity ? 0.91 : 0.98;
            final double drag = this.onGround ?
                    finalChunk.getBlock(position).registry().friction() : airDrag;
            this.velocity = newVelocity
                    // Convert from block/tick to block/sec
                    .mul(tps)
                    // Apply drag
                    .apply((x, y, z) -> new Vec(
                            x * drag,
                            !noGravity ? y * (1 - gravityDragPerTick) : y,
                            z * drag
                    ))
                    // Prevent infinitely decreasing velocity
                    .apply(Vec.Operator.EPSILON);
        }
        // Verify if velocity packet has to be sent
        if (hasVelocity || gravityTickCount > 0) {
            sendPacketToViewers(getVelocityPacket());
        }
    }

    private void touchTick() {
        // TODO do not call every tick (it is pretty expensive)
        final int minX = (int) Math.floor(boundingBox.getMinX());
        final int maxX = (int) Math.ceil(boundingBox.getMaxX());
        final int minY = (int) Math.floor(boundingBox.getMinY());
        final int maxY = (int) Math.ceil(boundingBox.getMaxY());
        final int minZ = (int) Math.floor(boundingBox.getMinZ());
        final int maxZ = (int) Math.ceil(boundingBox.getMaxZ());
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final Chunk chunk = ChunkUtils.retrieve(instance, currentChunk, x, z);
                    if (!ChunkUtils.isLoaded(chunk))
                        continue;
                    final Block block = chunk.getBlock(x, y, z, Block.Getter.Condition.CACHED);
                    if (block == null)
                        continue;
                    final BlockHandler handler = block.handler();
                    if (handler != null) {
                        // checks that we are actually in the block, and not just here because of a rounding error
                        if (boundingBox.intersectWithBlock(x, y, z)) {
                            // TODO: replace with check with custom block bounding box
                            handler.onTouch(new BlockHandler.Touch(block, instance, new Vec(x, y, z), this));
                        }
                    }
                }
            }
        }
    }

    private void effectTick(long time) {
        final List<TimedPotion> effects = this.effects;
        if (effects.isEmpty()) return;
        effects.removeIf(timedPotion -> {
            final long potionTime = (long) timedPotion.getPotion().duration() * MinecraftServer.TICK_MS;
            // Remove if the potion should be expired
            if (time >= timedPotion.getStartingTime() + potionTime) {
                // Send the packet that the potion should no longer be applied
                timedPotion.getPotion().sendRemovePacket(this);
                EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.getPotion()));
                return true;
            }
            return false;
        });
    }

    /**
     * Gets the number of ticks this entity has been active for.
     *
     * @return the number of ticks this entity has been active for
     */
    public long getAliveTicks() {
        return ticks;
    }

    /**
     * How does this entity handle being in the void?
     */
    protected void handleVoid() {
        // Kill if in void
        if (getInstance().isInVoid(this.position)) {
            remove();
        }
    }

    /**
     * Each entity has an unique id (server-wide) which will change after a restart.
     *
     * @return the unique entity id
     * @see Entity#getEntity(int) to retrive an entity based on its id
     */
    public int getEntityId() {
        return id;
    }

    /**
     * Returns the entity type.
     *
     * @return the entity type
     */
    public @NotNull EntityType getEntityType() {
        return entityType;
    }

    /**
     * Gets the entity {@link UUID}.
     *
     * @return the entity unique id
     */
    public @NotNull UUID getUuid() {
        return uuid;
    }

    /**
     * Changes the internal entity UUID, mostly unsafe.
     *
     * @param uuid the new entity uuid
     */
    public void setUuid(@NotNull UUID uuid) {
        // Refresh internal map
        Entity.ENTITY_BY_UUID.remove(this.uuid);
        Entity.ENTITY_BY_UUID.put(uuid, this);
        this.uuid = uuid;
    }

    /**
     * Returns false just after instantiation, set to true after calling {@link #setInstance(Instance)}.
     *
     * @return true if the entity has been linked to an instance, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Is used to check collision with coordinates or other blocks/entities.
     *
     * @return the entity bounding box
     */
    public @NotNull BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Changes the internal entity bounding box.
     * <p>
     * WARNING: this does not change the entity hit-box which is client-side.
     *
     * @param x the bounding box X size
     * @param y the bounding box Y size
     * @param z the bounding box Z size
     */
    public void setBoundingBox(double x, double y, double z) {
        this.boundingBox = new BoundingBox(this, x, y, z);
    }

    /**
     * Changes the internal entity bounding box.
     * <p>
     * WARNING: this does not change the entity hit-box which is client-side.
     *
     * @param boundingBox the new bounding box
     */
    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Convenient method to get the entity current chunk.
     *
     * @return the entity chunk, can be null even if unlikely
     */
    public @Nullable Chunk getChunk() {
        return currentChunk;
    }

    @ApiStatus.Internal
    protected void refreshCurrentChunk(Chunk currentChunk) {
        this.currentChunk = currentChunk;
        MinecraftServer.process().dispatcher().updateElement(this, currentChunk);
    }

    /**
     * Gets the entity current instance.
     *
     * @return the entity instance, can be null if the entity doesn't have an instance yet
     */
    public @Nullable Instance getInstance() {
        return instance;
    }

    /**
     * Changes the entity instance, i.e. spawns it.
     *
     * @param instance      the new instance of the entity
     * @param spawnPosition the spawn position for the entity.
     * @return a {@link CompletableFuture} called once the entity's instance has been set,
     * this is due to chunks needing to load
     * @throws IllegalStateException if {@code instance} has not been registered in {@link InstanceManager}
     */
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        Check.stateCondition(!instance.isRegistered(),
                "Instances need to be registered, please use InstanceManager#registerInstance or InstanceManager#registerSharedInstance");
        final Instance previousInstance = this.instance;
        if (Objects.equals(previousInstance, instance)) {
            return teleport(spawnPosition); // Already in the instance, teleport to spawn point
        }
        AddEntityToInstanceEvent event = new AddEntityToInstanceEvent(instance, this);
        EventDispatcher.call(event);
        if (event.isCancelled()) return null; // TODO what to return?

        if (previousInstance != null) removeFromInstance(previousInstance);

        this.isActive = true;
        this.position = spawnPosition;
        this.previousPosition = spawnPosition;
        this.instance = instance;
        return instance.loadOptionalChunk(spawnPosition).thenAccept(chunk -> {
            try {
                Check.notNull(chunk, "Entity has been placed in an unloaded chunk!");
                refreshCurrentChunk(chunk);
                if (this instanceof Player player) {
                    instance.getWorldBorder().init(player);
                    player.sendPacket(instance.createTimePacket());
                }
                instance.getEntityTracker().register(this, spawnPosition, trackingTarget, trackingUpdate);
                spawn();
                EventDispatcher.call(new EntitySpawnEvent(this, instance));
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }

    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Point spawnPosition) {
        return setInstance(instance, Pos.fromPoint(spawnPosition));
    }

    /**
     * Changes the entity instance.
     *
     * @param instance the new instance of the entity
     * @return a {@link CompletableFuture} called once the entity's instance has been set,
     * this is due to chunks needing to load
     * @throws NullPointerException  if {@code instance} is null
     * @throws IllegalStateException if {@code instance} has not been registered in {@link InstanceManager}
     */
    public CompletableFuture<Void> setInstance(@NotNull Instance instance) {
        return setInstance(instance, this.position);
    }

    private void removeFromInstance(Instance instance) {
        EventDispatcher.call(new RemoveEntityFromInstanceEvent(instance, this));
        instance.getEntityTracker().unregister(this, trackingTarget, trackingUpdate);
        this.viewEngine.forManuals(this::removeViewer);
    }

    /**
     * Gets the entity current velocity.
     *
     * @return the entity current velocity
     */
    public @NotNull Vec getVelocity() {
        return velocity;
    }

    /**
     * Changes the entity velocity and calls {@link EntityVelocityEvent}.
     * <p>
     * The final velocity can be cancelled or modified by the event.
     *
     * @param velocity the new entity velocity
     */
    public void setVelocity(@NotNull Vec velocity) {
        EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
        EventDispatcher.callCancellable(entityVelocityEvent, () -> {
            this.velocity = entityVelocityEvent.getVelocity();
            sendPacketToViewersAndSelf(getVelocityPacket());
        });
    }

    /**
     * Gets if the entity currently has a velocity applied.
     *
     * @return true if velocity is not set to 0
     */
    public boolean hasVelocity() {
        return !velocity.isZero();
    }

    /**
     * Gets the gravity drag per tick.
     *
     * @return the gravity drag per tick in block
     */
    public double getGravityDragPerTick() {
        return gravityDragPerTick;
    }

    /**
     * Gets the gravity acceleration.
     *
     * @return the gravity acceleration in block
     */
    public double getGravityAcceleration() {
        return gravityAcceleration;
    }

    /**
     * Gets the number of tick this entity has been applied gravity.
     *
     * @return the number of tick of which gravity has been consequently applied
     */
    public int getGravityTickCount() {
        return gravityTickCount;
    }

    /**
     * Changes the gravity of the entity.
     *
     * @param gravityDragPerTick  the gravity drag per tick in block
     * @param gravityAcceleration the gravity acceleration in block
     * @see <a href="https://minecraft.gamepedia.com/Entity#Motion_of_entities">Entities motion</a>
     */
    public void setGravity(double gravityDragPerTick, double gravityAcceleration) {
        this.gravityDragPerTick = gravityDragPerTick;
        this.gravityAcceleration = gravityAcceleration;
    }

    public double getDistance(@NotNull Point point) {
        return getPosition().distance(point);
    }

    /**
     * Gets the distance between two entities.
     *
     * @param entity the entity to get the distance from
     * @return the distance between this and {@code entity}
     */
    public double getDistance(@NotNull Entity entity) {
        return getDistance(entity.getPosition());
    }

    /**
     * Gets the distance squared between two entities.
     *
     * @param entity the entity to get the distance from
     * @return the distance squared between this and {@code entity}
     */
    public double getDistanceSquared(@NotNull Entity entity) {
        return getPosition().distanceSquared(entity.getPosition());
    }

    /**
     * Gets the entity vehicle or null.
     *
     * @return the entity vehicle, or null if there is not any
     */
    public @Nullable Entity getVehicle() {
        return vehicle;
    }

    /**
     * Adds a new passenger to this entity.
     *
     * @param entity the new passenger
     * @throws NullPointerException  if {@code entity} is null
     * @throws IllegalStateException if {@link #getInstance()} returns null or the passenger cannot be added
     */
    public void addPassenger(@NotNull Entity entity) {
        final Instance currentInstance = this.instance;
        Check.stateCondition(currentInstance == null, "You need to set an instance using Entity#setInstance");
        Check.stateCondition(entity == getVehicle(), "Cannot add the entity vehicle as a passenger");
        final Entity vehicle = entity.getVehicle();
        if (vehicle != null) vehicle.removePassenger(entity);
        if (!currentInstance.equals(entity.getInstance()))
            entity.setInstance(currentInstance, position).join();
        this.passengers.add(entity);
        entity.vehicle = this;
        sendPacketToViewersAndSelf(getPassengersPacket());
        // Updates the position of the new passenger, and then teleports the passenger
        updatePassengerPosition(position, entity);
        entity.synchronizePosition(false);
    }

    /**
     * Removes a passenger to this entity.
     *
     * @param entity the passenger to remove
     * @throws NullPointerException  if {@code entity} is null
     * @throws IllegalStateException if {@link #getInstance()} returns null
     */
    public void removePassenger(@NotNull Entity entity) {
        Check.stateCondition(instance == null, "You need to set an instance using Entity#setInstance");
        if (!passengers.remove(entity)) return;
        entity.vehicle = null;
        sendPacketToViewersAndSelf(getPassengersPacket());
        entity.synchronizePosition(false);
    }

    /**
     * Gets if the entity has any passenger.
     *
     * @return true if the entity has any passenger, false otherwise
     */
    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    /**
     * Gets the entity passengers.
     *
     * @return an unmodifiable list containing all the entity passengers
     */
    public @NotNull Set<@NotNull Entity> getPassengers() {
        return Collections.unmodifiableSet(passengers);
    }

    protected @NotNull SetPassengersPacket getPassengersPacket() {
        return new SetPassengersPacket(getEntityId(), passengers.stream().map(Entity::getEntityId).toList());
    }

    /**
     * Entity statuses can be found <a href="https://wiki.vg/Entity_statuses">here</a>.
     *
     * @param status the status to trigger
     */
    public void triggerStatus(byte status) {
        sendPacketToViewersAndSelf(new EntityStatusPacket(getEntityId(), status));
    }

    /**
     * Gets if the entity is on fire.
     *
     * @return true if the entity is in fire, false otherwise
     */
    public boolean isOnFire() {
        return this.entityMeta.isOnFire();
    }

    /**
     * Sets the entity in fire visually.
     * <p>
     * WARNING: if you want to apply damage or specify a duration,
     * see {@link LivingEntity#setFireForDuration(int, TemporalUnit)}.
     *
     * @param fire should the entity be set in fire
     */
    public void setOnFire(boolean fire) {
        this.entityMeta.setOnFire(fire);
    }

    /**
     * Gets if the entity is sneaking.
     * <p>
     * WARNING: this can be bypassed by hacked client, this is only what the client told the server.
     *
     * @return true if the player is sneaking
     */
    public boolean isSneaking() {
        return this.entityMeta.isSneaking();
    }

    /**
     * Makes the entity sneak.
     * <p>
     * WARNING: this will not work for the client itself.
     *
     * @param sneaking true to make the entity sneak
     */
    public void setSneaking(boolean sneaking) {
        setPose(sneaking ? Pose.SNEAKING : Pose.STANDING);
        this.entityMeta.setSneaking(sneaking);
    }

    /**
     * Gets if the player is sprinting.
     * <p>
     * WARNING: this can be bypassed by hacked client, this is only what the client told the server.
     *
     * @return true if the player is sprinting
     */
    public boolean isSprinting() {
        return this.entityMeta.isSprinting();
    }

    /**
     * Makes the entity sprint.
     * <p>
     * WARNING: this will not work on the client itself.
     *
     * @param sprinting true to make the entity sprint
     */
    public void setSprinting(boolean sprinting) {
        this.entityMeta.setSprinting(sprinting);
    }

    /**
     * Gets if the entity is invisible or not.
     *
     * @return true if the entity is invisible, false otherwise
     */
    public boolean isInvisible() {
        return this.entityMeta.isInvisible();
    }

    /**
     * Changes the internal invisible value and send a {@link EntityMetaDataPacket}
     * to make visible or invisible the entity to its viewers.
     *
     * @param invisible true to set the entity invisible, false otherwise
     */
    public void setInvisible(boolean invisible) {
        this.entityMeta.setInvisible(invisible);
    }

    /**
     * Gets if the entity is glowing or not.
     *
     * @return true if the entity is glowing, false otherwise
     */
    public boolean isGlowing() {
        return this.entityMeta.isHasGlowingEffect();
    }

    /**
     * Sets or remove the entity glowing effect.
     *
     * @param glowing true to make the entity glows, false otherwise
     */
    public void setGlowing(boolean glowing) {
        this.entityMeta.setHasGlowingEffect(glowing);
    }

    /**
     * Gets the current entity pose.
     *
     * @return the entity pose
     */
    public @NotNull Pose getPose() {
        return this.entityMeta.getPose();
    }

    /**
     * Changes the entity pose.
     * <p>
     * The internal {@code crouched} and {@code swimming} field will be
     * updated accordingly.
     *
     * @param pose the new entity pose
     */
    public void setPose(@NotNull Pose pose) {
        this.entityMeta.setPose(pose);
    }

    /**
     * Gets the entity custom name.
     *
     * @return the custom name of the entity, null if there is not
     */
    public @Nullable Component getCustomName() {
        return this.entityMeta.getCustomName();
    }

    /**
     * Changes the entity custom name.
     *
     * @param customName the custom name of the entity, null to remove it
     */
    public void setCustomName(@Nullable Component customName) {
        this.entityMeta.setCustomName(customName);
    }

    /**
     * Gets the custom name visible metadata field.
     *
     * @return true if the custom name is visible, false otherwise
     */
    public boolean isCustomNameVisible() {
        return this.entityMeta.isCustomNameVisible();
    }

    /**
     * Changes the internal custom name visible field and send a {@link EntityMetaDataPacket}
     * to update the entity state to its viewers.
     *
     * @param customNameVisible true to make the custom name visible, false otherwise
     */
    public void setCustomNameVisible(boolean customNameVisible) {
        this.entityMeta.setCustomNameVisible(customNameVisible);
    }

    public boolean isSilent() {
        return this.entityMeta.isSilent();
    }

    public void setSilent(boolean silent) {
        this.entityMeta.setSilent(silent);
    }

    /**
     * Gets the noGravity metadata field.
     *
     * @return true if the entity ignore gravity, false otherwise
     */
    public boolean hasNoGravity() {
        return this.entityMeta.isHasNoGravity();
    }

    /**
     * Changes the noGravity metadata field and change the gravity behaviour accordingly.
     *
     * @param noGravity should the entity ignore gravity
     */
    public void setNoGravity(boolean noGravity) {
        this.entityMeta.setHasNoGravity(noGravity);
    }

    /**
     * Updates internal fields and sends updates.
     *
     * @param newPosition the new position
     */
    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos newPosition, boolean ignoreView) {
        final var previousPosition = this.position;
        final Pos position = ignoreView ? previousPosition.withCoord(newPosition) : newPosition;
        if (position.equals(lastSyncedPosition)) return;
        this.position = position;
        this.previousPosition = previousPosition;
        if (!position.samePoint(previousPosition)) {
            refreshCoordinate(position);
            // Update player velocity
            if (PlayerUtils.isSocketClient(this)) {
                // Calculate from client
                this.velocity = position.sub(previousPosition).asVec().mul(MinecraftServer.TICK_PER_SECOND);
            }
        }
        // Update viewers
        final boolean viewChange = !position.sameView(lastSyncedPosition);
        final double distanceX = Math.abs(position.x() - lastSyncedPosition.x());
        final double distanceY = Math.abs(position.y() - lastSyncedPosition.y());
        final double distanceZ = Math.abs(position.z() - lastSyncedPosition.z());
        final boolean positionChange = (distanceX + distanceY + distanceZ) > 0;

        final Chunk chunk = getChunk();
        if (distanceX > 8 || distanceY > 8 || distanceZ > 8) {
            PacketUtils.prepareViewablePacket(chunk, new EntityTeleportPacket(getEntityId(), position, isOnGround()), this);
            this.lastAbsoluteSynchronizationTime = System.currentTimeMillis();
        } else if (positionChange && viewChange) {
            PacketUtils.prepareViewablePacket(chunk, EntityPositionAndRotationPacket.getPacket(getEntityId(), position,
                    lastSyncedPosition, isOnGround()), this);
            // Fix head rotation
            PacketUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
        } else if (positionChange) {
            PacketUtils.prepareViewablePacket(chunk, EntityPositionPacket.getPacket(getEntityId(), position, lastSyncedPosition, onGround), this);
        } else if (viewChange) {
            PacketUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
            PacketUtils.prepareViewablePacket(chunk, new EntityRotationPacket(getEntityId(), position.yaw(), position.pitch(), onGround), this);
        }
        this.lastSyncedPosition = position;
    }

    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos newPosition) {
        refreshPosition(newPosition, false);
    }

    /**
     * @return The height offset for passengers of this vehicle
     */
    private double getPassengerHeightOffset() {
        // TODO: Move this logic elsewhere
        if (entityType == EntityType.BOAT) {
            return -0.1;
        } else if (entityType == EntityType.MINECART) {
            return 0.0;
        } else {
            return entityType.height() * 0.75;
        }
    }

    /**
     * Sets the X,Z coordinate of the passenger to the X,Z coordinate of this vehicle
     * and sets the Y coordinate of the passenger to the Y coordinate of this vehicle + {@link #getPassengerHeightOffset()}
     *
     * @param newPosition The X,Y,Z position of this vehicle
     * @param passenger   The passenger to be moved
     */
    private void updatePassengerPosition(Point newPosition, Entity passenger) {
        final Pos oldPassengerPos = passenger.position;
        final Pos newPassengerPos = oldPassengerPos.withCoord(newPosition.x(),
                newPosition.y() + getPassengerHeightOffset(),
                newPosition.z());
        passenger.position = newPassengerPos;
        passenger.previousPosition = oldPassengerPos;
        passenger.refreshCoordinate(newPassengerPos);
    }

    /**
     * Used to refresh the entity and its passengers position
     * - put the entity in the right instance chunk
     * - update the viewable chunks (load and unload)
     * - add/remove players from the viewers list if {@link #isAutoViewable()} is enabled
     * <p>
     * WARNING: unsafe, should only be used internally in Minestom. Use {@link #teleport(Pos)} instead.
     *
     * @param newPosition the new position
     */
    private void refreshCoordinate(Point newPosition) {
        // Passengers update
        final Set<Entity> passengers = getPassengers();
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                updatePassengerPosition(newPosition, passenger);
            }
        }
        // Handle chunk switch
        final Instance instance = getInstance();
        assert instance != null;
        instance.getEntityTracker().move(this, newPosition, trackingTarget, trackingUpdate);
        final int lastChunkX = currentChunk.getChunkX();
        final int lastChunkZ = currentChunk.getChunkZ();
        final int newChunkX = newPosition.chunkX();
        final int newChunkZ = newPosition.chunkZ();
        if (lastChunkX != newChunkX || lastChunkZ != newChunkZ) {
            // Entity moved in a new chunk
            final Chunk newChunk = instance.getChunk(newChunkX, newChunkZ);
            Check.notNull(newChunk, "The entity {0} tried to move in an unloaded chunk at {1}", getEntityId(), newPosition);
            if (this instanceof Player player) { // Update visible chunks
                player.sendPacket(new UpdateViewPositionPacket(newChunkX, newChunkZ));
                ChunkUtils.forDifferingChunksInRange(newChunkX, newChunkZ, lastChunkX, lastChunkZ,
                        MinecraftServer.getChunkViewDistance(), player.chunkAdder, player.chunkRemover);
            }
            refreshCurrentChunk(newChunk);
        }
    }

    /**
     * Gets the entity position.
     *
     * @return the current position of the entity
     */
    public @NotNull Pos getPosition() {
        return position;
    }

    /**
     * Gets the entity eye height.
     * <p>
     * Default to {@link BoundingBox#getHeight()}x0.85
     *
     * @return the entity eye height
     */
    public double getEyeHeight() {
        return boundingBox.getHeight() * 0.85;
    }

    /**
     * Gets all the potion effect of this entity.
     *
     * @return an unmodifiable list of all this entity effects
     */
    public @NotNull List<@NotNull TimedPotion> getActiveEffects() {
        return Collections.unmodifiableList(effects);
    }

    /**
     * Adds an effect to an entity.
     *
     * @param potion The potion to add
     */
    public void addEffect(@NotNull Potion potion) {
        removeEffect(potion.effect());
        this.effects.add(new TimedPotion(potion, System.currentTimeMillis()));
        potion.sendAddPacket(this);
        EventDispatcher.call(new EntityPotionAddEvent(this, potion));
    }

    /**
     * Removes effect from entity, if it has it.
     *
     * @param effect The effect to remove
     */
    public void removeEffect(@NotNull PotionEffect effect) {
        this.effects.removeIf(timedPotion -> {
            if (timedPotion.getPotion().effect() == effect) {
                timedPotion.getPotion().sendRemovePacket(this);
                EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.getPotion()));
                return true;
            }
            return false;
        });
    }

    /**
     * Removes all the effects currently applied to the entity.
     */
    public void clearEffects() {
        for (TimedPotion timedPotion : effects) {
            timedPotion.getPotion().sendRemovePacket(this);
            EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.getPotion()));
        }
        this.effects.clear();
    }

    /**
     * Removes the entity from the server immediately.
     * <p>
     * WARNING: this does not trigger {@link EntityDeathEvent}.
     */
    public void remove() {
        if (isRemoved()) return;
        // Remove passengers if any (also done with LivingEntity#kill)
        Set<Entity> passengers = getPassengers();
        if (!passengers.isEmpty()) passengers.forEach(this::removePassenger);
        final Entity vehicle = this.vehicle;
        if (vehicle != null) vehicle.removePassenger(this);
        MinecraftServer.process().dispatcher().removeElement(this);
        this.removed = true;
        Entity.ENTITY_BY_ID.remove(id);
        Entity.ENTITY_BY_UUID.remove(uuid);
        Instance currentInstance = this.instance;
        if (currentInstance != null) removeFromInstance(currentInstance);
    }

    /**
     * Gets if this entity has been removed.
     *
     * @return true if this entity is removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Triggers {@link #remove()} after the specified time.
     *
     * @param delay        the time before removing the entity,
     *                     0 to cancel the removing
     * @param temporalUnit the unit of the delay
     */
    public void scheduleRemove(long delay, @NotNull TemporalUnit temporalUnit) {
        if (temporalUnit == TimeUnit.SERVER_TICK) {
            scheduleRemove(TaskSchedule.tick((int) delay));
        } else {
            scheduleRemove(Duration.of(delay, temporalUnit));
        }
    }

    /**
     * Triggers {@link #remove()} after the specified time.
     *
     * @param delay the time before removing the entity
     */
    public void scheduleRemove(Duration delay) {
        scheduleRemove(TaskSchedule.duration(delay));
    }

    private void scheduleRemove(TaskSchedule schedule) {
        this.scheduler.buildTask(this::remove).delay(schedule).schedule();
    }

    protected @NotNull Vec getVelocityForPacket() {
        return this.velocity.mul(8000f / MinecraftServer.TICK_PER_SECOND);
    }

    protected @NotNull EntityVelocityPacket getVelocityPacket() {
        return new EntityVelocityPacket(getEntityId(), getVelocityForPacket());
    }

    /**
     * Gets an {@link EntityMetaDataPacket} sent when adding viewers. Used for synchronization.
     *
     * @return The {@link EntityMetaDataPacket} related to this entity
     */
    public @NotNull EntityMetaDataPacket getMetadataPacket() {
        return new EntityMetaDataPacket(getEntityId(), metadata.getEntries());
    }

    /**
     * Used to synchronize entity position with viewers by sending an
     * {@link EntityTeleportPacket} to viewers, in case of a player this is
     * overridden in order to send an additional {@link PlayerPositionAndLookPacket}
     * to itself.
     *
     * @param includeSelf if {@code true} and this is a {@link Player} an additional {@link PlayerPositionAndLookPacket}
     *                    will be sent to the player itself
     */
    @ApiStatus.Internal
    protected void synchronizePosition(boolean includeSelf) {
        final Pos posCache = this.position;
        final ServerPacket packet = new EntityTeleportPacket(getEntityId(), posCache, isOnGround());
        PacketUtils.prepareViewablePacket(currentChunk, packet, this);
        this.lastAbsoluteSynchronizationTime = System.currentTimeMillis();
        this.lastSyncedPosition = posCache;
    }

    /**
     * Asks for a synchronization (position) to happen during next entity tick.
     */
    public void askSynchronization() {
        this.lastAbsoluteSynchronizationTime = 0;
    }

    /**
     * Set custom cooldown for position synchronization.
     *
     * @param cooldown custom cooldown for position synchronization.
     */
    public void setCustomSynchronizationCooldown(@Nullable Duration cooldown) {
        this.customSynchronizationCooldown = cooldown;
    }

    @Override
    public @NotNull HoverEvent<ShowEntity> asHoverEvent(@NotNull UnaryOperator<ShowEntity> op) {
        return HoverEvent.showEntity(ShowEntity.of(this.entityType, this.uuid));
    }

    private Duration getSynchronizationCooldown() {
        return Objects.requireNonNullElse(this.customSynchronizationCooldown, SYNCHRONIZATION_COOLDOWN);
    }

    @ApiStatus.Experimental
    public <T extends Entity> @NotNull Acquirable<T> getAcquirable() {
        return (Acquirable<T>) acquirable;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbtCompound);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tag.write(nbtCompound, value);
    }

    @Override
    public @NotNull Scheduler scheduler() {
        return scheduler;
    }

    /**
     * Applies knockback to the entity
     *
     * @param strength the strength of the knockback, 0.4 is the vanilla value for a bare hand hit
     * @param x        knockback on x axle, for default knockback use the following formula <pre>sin(attacker.yaw * (pi/180))</pre>
     * @param z        knockback on z axle, for default knockback use the following formula <pre>-cos(attacker.yaw * (pi/180))</pre>
     */
    public void takeKnockback(final float strength, final double x, final double z) {
        if (strength > 0) {
            //TODO check possible side effects of unnatural TPS (other than 20TPS)
            final Vec velocityModifier = new Vec(x, z)
                    .normalize()
                    .mul(strength * MinecraftServer.TICK_PER_SECOND / 2);
            setVelocity(new Vec(velocity.x() / 2d - velocityModifier.x(),
                    onGround ? Math.min(.4d, velocity.y() / 2d + strength) * MinecraftServer.TICK_PER_SECOND : velocity.y(),
                    velocity.z() / 2d - velocityModifier.z()
            ));
        }
    }


    /**
     * Gets the line of sight of the entity.
     *
     * @param maxDistance The max distance to scan
     * @return A list of {@link Point poiints} in this entities line of sight
     */
    public List<Point> getLineOfSight(int maxDistance) {
        Instance instance = getInstance();
        if (instance == null) {
            return Collections.emptyList();
        }

        List<Point> blocks = new ArrayList<>();
        var it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            final Point position = it.next();
            if (!instance.getBlock(position).isAir()) blocks.add(position);
        }
        return blocks;
    }

    /**
     * Checks whether the current entity has line of sight to the given one.
     * If so, it doesn't mean that the given entity is IN line of sight of the current,
     * but the current one can rotate so that it will be true.
     *
     * @param entity the entity to be checked.
     * @return if the current entity has line of sight to the given one.
     */
    public boolean hasLineOfSight(Entity entity) {
        Instance instance = getInstance();
        if (instance == null) {
            return false;
        }

        final Vec start = getPosition().asVec().add(0D, getEyeHeight(), 0D);
        final Vec end = entity.getPosition().asVec().add(0D, getEyeHeight(), 0D);
        final Vec direction = end.sub(start);
        final int maxDistance = (int) Math.ceil(direction.length());

        var it = new BlockIterator(start, direction.normalize(), 0D, maxDistance);
        while (it.hasNext()) {
            Block block = instance.getBlock(it.next());
            if (!block.isAir() && !block.isLiquid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets first entity on the line of sight of the current one that matches the given predicate.
     *
     * @param range     max length of the line of sight of the current entity to be checked.
     * @param predicate optional predicate
     * @return resulting entity whether there're any, null otherwise.
     */
    public @Nullable Entity getLineOfSightEntity(double range, Predicate<Entity> predicate) {
        Instance instance = getInstance();
        if (instance == null) {
            return null;
        }

        Vec start = new Vec(position.x(), position.y() + getEyeHeight(), position.z());
        Vec end = start.add(position.direction().mul(range));

        List<Entity> nearby = instance.getNearbyEntities(position, range).stream()
                .filter(e -> e != this && e.boundingBox.intersect(start, end) && predicate.test(e)).toList();
        if (nearby.isEmpty()) {
            return null;
        }

        Vec direction = end.sub(start);
        int maxDistance = (int) Math.ceil(direction.length());
        double maxVisibleDistanceSquared = direction.lengthSquared();

        var iterator = new BlockIterator(start, direction.normalize(), 0D, maxDistance);
        while (iterator.hasNext()) {
            Point blockPos = iterator.next();
            Block block = instance.getBlock(blockPos);
            if (!block.isAir() && !block.isLiquid()) {
                maxVisibleDistanceSquared = blockPos.distanceSquared(position);
                break;
            }
        }

        Entity result = null;
        double minDistanceSquared = 0D;
        for (Entity entity : nearby) {
            double distanceSquared = entity.getDistanceSquared(this);
            if (result == null || minDistanceSquared > distanceSquared) {
                result = entity;
                minDistanceSquared = distanceSquared;
            }
        }
        if (minDistanceSquared < maxVisibleDistanceSquared) {
            return result;
        } else {
            return null;
        }
    }

    public enum Pose {
        STANDING,
        FALL_FLYING,
        SLEEPING,
        SWIMMING,
        SPIN_ATTACK,
        SNEAKING,
        DYING
    }
}
