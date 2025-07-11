package net.minestom.server.entity;

import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.*;
import net.minestom.server.collision.*;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.Taggable;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.thread.AcquirableSource;
import net.minestom.server.timer.Schedulable;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.PacketViewableUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.position.PositionUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Could be a player, a monster, or an object.
 * <p>
 * To create your own entity you probably want to extend {@link LivingEntity} or {@link EntityCreature} instead.
 */
public class Entity implements Viewable, Tickable, Schedulable, Snapshotable, EventHandler<EntityEvent>, Taggable,
        HoverEventSource<ShowEntity>, Sound.Emitter, Shape, AcquirableSource<Entity>, DataComponent.Holder, Pointered, Identified {
    // This is somewhat arbitrary, but we don't want to hit the max int ever because it is very easy to
    // overflow while working with a position at the max int (for example, looping over a bounding box)
    private static final int MAX_COORDINATE = 2_000_000_000;

    private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

    // Protected due to PointersSupplier.Builder#parent
    protected static PointersSupplier<Entity> ENTITY_POINTERS_SUPPLIER = PointersSupplier.<Entity>builder()
            .resolving(Identity.DISPLAY_NAME, (entity) -> entity.get(DataComponents.CUSTOM_NAME))
            .resolving(Identity.UUID, Entity::getUuid)
            .build();

    // Certain entities should only have their position packets sent during synchronization
    private static final Set<EntityType> SYNCHRONIZE_ONLY_ENTITIES = Set.of(EntityType.ITEM, EntityType.FALLING_BLOCK,
            EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.TRIDENT, EntityType.LLAMA_SPIT, EntityType.WIND_CHARGE,
            EntityType.FISHING_BOBBER, EntityType.SNOWBALL, EntityType.EGG, EntityType.ENDER_PEARL, EntityType.SPLASH_POTION,
            EntityType.LINGERING_POTION, EntityType.EYE_OF_ENDER, EntityType.DRAGON_FIREBALL, EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL, EntityType.TNT);
    private static final Set<EntityType> ALLOW_BLOCK_PLACEMENT_ENTITIES = Set.of(EntityType.ARROW, EntityType.ITEM,
            EntityType.SNOWBALL, EntityType.EXPERIENCE_BOTTLE, EntityType.EXPERIENCE_ORB, EntityType.SPLASH_POTION,
            EntityType.LINGERING_POTION, EntityType.AREA_EFFECT_CLOUD);
    private static final Set<EntityType> NO_ENTITY_COLLISION_ENTITIES = Set.of(EntityType.TEXT_DISPLAY, EntityType.ITEM_DISPLAY,
            EntityType.BLOCK_DISPLAY);
    private final CachedPacket destroyPacketCache = new CachedPacket(() -> new DestroyEntitiesPacket(getEntityId()));

    protected Instance instance;
    protected Chunk currentChunk;
    protected Pos position; // Should be updated by setPositionInternal only.
    protected Pos previousPosition;
    protected Pos lastSyncedPosition;
    protected boolean onGround;

    protected BoundingBox boundingBox;
    private PhysicsResult previousPhysicsResult = null;

    protected Entity vehicle;

    // Velocity
    protected Vec velocity = Vec.ZERO; // Movement in block per second
    protected boolean lastVelocityWasZero = true;
    protected boolean hasPhysics = true;
    protected boolean collidesWithEntities = true;
    protected boolean preventBlockPlacement = true;

    private Aerodynamics aerodynamics;
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

    protected final EntityView viewEngine = new EntityView(this);
    protected final Set<Player> viewers = viewEngine.set;
    private final TagHandler tagHandler = TagHandler.newHandler();
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final EventNode<EntityEvent> eventNode;

    private final UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    protected boolean removed;

    private final Set<Entity> passengers = new CopyOnWriteArraySet<>();

    private final Set<Entity> leashedEntities = new CopyOnWriteArraySet<>();
    private Entity leashHolder;

    protected EntityType entityType; // UNSAFE to change, modify at your own risk

    // Network synchronization, send the absolute position of the entity every n ticks
    private long synchronizationTicks = ServerFlag.ENTITY_SYNCHRONIZATION_TICKS;
    private long nextSynchronizationTick = synchronizationTicks;

    protected MetadataHolder metadata = new MetadataHolder(this);
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

        this.entityMeta = MetadataHolder.createMeta(entityType, this, this.metadata);

        final RegistryData.EntityEntry registry = entityType.registry();
        setBoundingBox(entityType.registry().boundingBox());

        this.aerodynamics = new Aerodynamics(
                registry.acceleration(),
                registry.horizontalAirResistance(),
                registry.verticalAirResistance());

        final ServerProcess process = MinecraftServer.process();
        if (process != null) {
            this.eventNode = process.eventHandler().map(this, EventFilter.ENTITY);
        } else {
            // Local nodes require a server process
            this.eventNode = null;
        }
        updateCollisions();
    }

    public Entity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    protected void setPositionInternal(@NotNull Pos newPosition) {
        if (newPosition.x() >= MAX_COORDINATE || newPosition.x() <= -MAX_COORDINATE ||
                newPosition.y() >= MAX_COORDINATE || newPosition.y() <= -MAX_COORDINATE ||
                newPosition.z() >= MAX_COORDINATE || newPosition.z() <= -MAX_COORDINATE) {
            newPosition = newPosition.withCoord(
                    MathUtils.clamp(newPosition.x(), -MAX_COORDINATE, MAX_COORDINATE),
                    MathUtils.clamp(newPosition.y(), -MAX_COORDINATE, MAX_COORDINATE),
                    MathUtils.clamp(newPosition.z(), -MAX_COORDINATE, MAX_COORDINATE)
            );
        }
        this.position = newPosition;
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
     * @param time time of the update in milliseconds. This may only be used as a delta and has no meaning in the real world
     */
    public void update(long time) {

    }

    /**
     * Called when a new instance is set.
     */
    public void spawn() {

    }

    /**
     * Called right before an entity is removed
     */
    protected void despawn() {

    }

    public boolean isOnGround() {
        return onGround;
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

    @SuppressWarnings("unchecked") @Override
    public <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.CUSTOM_DATA)
            return (T) tagHandler.asCompound();
        return EntityMeta.getComponent(getEntityMeta(), component);
    }

    public <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.CUSTOM_DATA)
            tagHandler.updateContent((CompoundBinaryTag) value);
        else EntityMeta.setComponent(getEntityMeta(), component, value);
    }

    /**
     * Do a batch edit of this entity's metadata.
     */
    public <TMeta extends EntityMeta> void editEntityMeta(Class<TMeta> metaClass, Consumer<TMeta> editor) {
        entityMeta.setNotifyAboutChanges(false);
        try {
            TMeta casted = metaClass.cast(entityMeta);
            editor.accept(casted);
        } catch (Throwable t) {
            throw new RuntimeException("Error editing entity " + id + " " + entityType.name() + " meta", t);
        } finally {
            entityMeta.setNotifyAboutChanges(true);
        }
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position) {
        return teleport(position, null, RelativeFlags.NONE);
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, @NotNull Vec velocity) {
        return teleport(position, velocity, null, RelativeFlags.NONE);
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks,
                                                     @MagicConstant(flagsFromClass = RelativeFlags.class) int flags) {
        return teleport(position, chunks, flags, true);
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, @NotNull Vec velocity, long @Nullable [] chunks,
                                                     @MagicConstant(flagsFromClass = RelativeFlags.class) int flags) {
        return teleport(position, velocity, chunks, flags, true);
    }

    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, long @Nullable [] chunks,
                                                     @MagicConstant(flagsFromClass = RelativeFlags.class) int flags,
                                                     boolean shouldConfirm) {
        // Use delta coord if not providing a delta velocity (to avoid resetting velocity)
        return teleport(position, Vec.ZERO, chunks, flags | RelativeFlags.DELTA_COORD, shouldConfirm);
    }

    /**
     * Teleports the entity only if the chunk at {@code position} is loaded or if
     * {@link Instance#hasEnabledAutoChunkLoad()} returns true.
     *
     * @param position      the teleport position
     * @param chunks        the chunk indexes to load before teleporting the entity,
     *                      indexes are from {@link CoordConversion#chunkIndex(int, int)},
     *                      can be null or empty to only load the chunk at {@code position}
     * @param flags         flags used to teleport the entity relatively rather than absolutely
     *                      use {@link RelativeFlags} to see available flags
     * @param shouldConfirm if false, the teleportation will be done without confirmation
     * @throws IllegalStateException if you try to teleport an entity before settings its instance
     */
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, @NotNull Vec velocity, long @Nullable [] chunks,
                                                     @MagicConstant(flagsFromClass = RelativeFlags.class) int flags,
                                                     boolean shouldConfirm) {
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");

        EntityTeleportEvent event = new EntityTeleportEvent(this, position, flags);
        EventDispatcher.call(event);

        final Pos globalPosition = PositionUtils.getPositionWithRelativeFlags(this.position, position, flags);
        final Vec globalVelocity = PositionUtils.getVelocityWithRelativeFlags(this.velocity, velocity, flags);

        final Runnable endCallback = () -> {
            this.previousPosition = this.position;
            setPositionInternal(globalPosition);
            this.velocity = globalVelocity;
            refreshCoordinate(globalPosition);
            if (this instanceof Player player)
                player.synchronizePositionAfterTeleport(position, velocity, flags, shouldConfirm);
            else synchronizePosition();
        };

        if (chunks != null && chunks.length > 0) {
            // Chunks need to be loaded before the teleportation can happen
            return ChunkUtils.optionalLoadAll(instance, chunks, null).thenRun(endCallback);
        }
        final Pos currentPosition = this.position;
        if (!currentPosition.sameChunk(globalPosition)) {
            // Ensure that the chunk is loaded
            return instance.loadOptionalChunk(globalPosition).thenRun(endCallback);
        } else {
            // Position is in the same chunk, keep it sync
            endCallback.run();
            return AsyncUtils.empty();
        }
    }

    /**
     * Changes the view of the entity.
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    public void setView(float yaw, float pitch) {
        final Pos currentPosition = this.position;
        if (currentPosition.sameView(yaw, pitch)) return;
        setPositionInternal(currentPosition.withView(yaw, pitch));
        synchronizeView();
    }

    /**
     * Changes the view of the entity so that it looks in a direction to the given position if
     * it is different from the entity's current position.
     *
     * @param point the point to look at.
     */
    public void lookAt(@NotNull Point point) {
        final Pos newPosition = this.position.add(0, getEyeHeight(), 0).withLookAt(point);
        setView(newPosition.yaw(), newPosition.pitch());
    }

    /**
     * Changes the view of the entity so that it looks in a direction to the given entity.
     *
     * @param entity the entity to look at.
     * @throws IllegalArgumentException if the entities are not in the same instance
     */
    public void lookAt(@NotNull Entity entity) {
        Check.argCondition(entity.instance != instance, "Entity cannot look at an entity in another instance");
        lookAt(entity.position.withY(entity.position.y() + entity.getEyeHeight()));
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

    public void updateViewableRule(@Nullable Predicate<Player> predicate) {
        this.viewEngine.viewableOption.updateRule(predicate);
    }

    public void updateViewableRule() {
        this.viewEngine.viewableOption.updateRule();
    }

    /**
     * Gets if surrounding entities are automatically visible by this.
     * True by default.
     *
     * @return true if surrounding entities are visible by this
     */
    public boolean autoViewEntities() {
        return viewEngine.viewerOption.isAuto();
    }

    /**
     * Decides if surrounding entities must be visible.
     *
     * @param autoViewer true to add view surrounding entities, false to remove
     */
    public void setAutoViewEntities(boolean autoViewer) {
        this.viewEngine.viewerOption.updateAuto(autoViewer);
    }

    public void updateViewerRule(@Nullable Predicate<Entity> predicate) {
        this.viewEngine.viewerOption.updateRule(predicate);
    }

    public void updateViewerRule() {
        this.viewEngine.viewerOption.updateRule();
    }

    @Override
    public final boolean addViewer(@NotNull Player player) {
        Check.stateCondition(!isActive(), "Entities must be in an instance before adding viewers");
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
        player.sendPacket(getSpawnPacket());
        if (hasVelocity()) player.sendPacket(getVelocityPacket());
        player.sendPacket(this.getMetadataPacket());
        // Passengers
        final Set<Entity> passengers = this.passengers;
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger != player) passenger.updateNewViewer(player);
            }
            player.sendPacket(getPassengersPacket());
        }
        // Leashes
        if (leashHolder != null && (player.equals(leashHolder) || leashHolder.isViewer(player))) {
            player.sendPacket(getAttachEntityPacket());
        }
        for (Entity entity : leashedEntities) {
            if (entity.isViewer(player)) {
                player.sendPacket(entity.getAttachEntityPacket());
            }
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
                if (passenger != player) passenger.updateOldViewer(player);
            }
        }
        leashedEntities.forEach(entity -> player.sendPacket(new AttachEntityPacket(entity.getEntityId(), -1)));
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
     * Be aware that this only change the visual of the entity, the {@link BoundingBox}
     * will not be modified.
     *
     * @param entityType the new entity type
     */
    public synchronized void switchEntityType(@NotNull EntityType entityType) {
        this.entityType = entityType;
        this.metadata = new MetadataHolder(this);
        this.entityMeta = MetadataHolder.createMeta(entityType, this, this.metadata);

        final RegistryData.EntityEntry registry = entityType.registry();
        this.aerodynamics = aerodynamics.withAirResistance(
                registry.horizontalAirResistance(),
                registry.verticalAirResistance());

        updateCollisions();
        Set<Player> viewers = new HashSet<>(getViewers());
        getViewers().forEach(this::updateOldViewer);
        viewers.forEach(this::updateNewViewer);
    }

    /**
     * Updates the entity, called every tick.
     * <p>
     * Ignored if {@link #getInstance()} returns null.
     *
     * @param time the update time in milliseconds. This may only be used as a delta and has no meaning in the real world.
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
            // handle position and velocity updates
            movementTick();

            // handle block contacts
            touchTick();

            // Call the abstract update method
            update(time);

            ticks++;
            EventDispatcher.call(new EntityTickEvent(this));

            // remove expired effects
            effectTick();
        }
        // Scheduled synchronization
        if (vehicle == null && ticks >= nextSynchronizationTick) {
            synchronizePosition();
            sendPacketToViewers(getVelocityPacket());
        }
        // End of tick scheduled tasks
        this.scheduler.processTickEnd();
    }

    @ApiStatus.Internal
    protected void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle != null) return;

        boolean entityIsPlayer = this instanceof Player;
        boolean entityFlying = entityIsPlayer && ((Player) this).isFlying();
        final Block.Getter chunkCache = new ChunkCache(instance, currentChunk, Block.STONE);
        PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity.div(ServerFlag.SERVER_TICKS_PER_SECOND), boundingBox,
                instance.getWorldBorder(), chunkCache, aerodynamics, hasNoGravity(), hasPhysics, onGround, entityFlying, previousPhysicsResult);
        this.previousPhysicsResult = physicsResult;

        Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
        if (!ChunkUtils.isLoaded(finalChunk)) return;

        velocity = physicsResult.newVelocity().mul(ServerFlag.SERVER_TICKS_PER_SECOND);
        if (!(this instanceof Player)) {
            onGround = physicsResult.isOnGround();
            refreshPosition(physicsResult.newPosition(), true, !SYNCHRONIZE_ONLY_ENTITIES.contains(entityType));
        }
    }

    private void touchTick() {
        if (!hasPhysics) return;

        // TODO do not call every tick (it is pretty expensive)
        final Pos position = this.position;
        final BoundingBox boundingBox = this.boundingBox;
        ChunkCache cache = new ChunkCache(instance, currentChunk);

        final int minX = (int) Math.floor(boundingBox.minX() + position.x());
        final int maxX = (int) Math.ceil(boundingBox.maxX() + position.x());
        final int minY = (int) Math.floor(boundingBox.minY() + position.y());
        final int maxY = (int) Math.ceil(boundingBox.maxY() + position.y());
        final int minZ = (int) Math.floor(boundingBox.minZ() + position.z());
        final int maxZ = (int) Math.ceil(boundingBox.maxZ() + position.z());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final Block block = cache.getBlock(x, y, z, Block.Getter.Condition.CACHED);
                    if (block == null) continue;
                    final BlockHandler handler = block.handler();
                    if (handler != null) {
                        // Move a small amount towards the entity. If the entity is within 0.01 blocks of the block, touch will trigger
                        Vec blockPos = new Vec(x, y, z);
                        Point blockEntityVector = (blockPos.sub(position)).normalize().mul(0.01);
                        if (block.registry().collisionShape().intersectBox(position.sub(blockPos).add(blockEntityVector), boundingBox)) {
                            handler.onTouch(new BlockHandler.Touch(block, instance, new Vec(x, y, z), this));
                        }
                    }
                }
            }
        }
    }

    private void effectTick() {
        final List<TimedPotion> effects = this.effects;
        if (effects.isEmpty()) return;
        effects.removeIf(timedPotion -> {
            long duration = timedPotion.potion().duration();
            if (duration == Potion.INFINITE_DURATION) return false;
            // Remove if the potion should be expired
            if (getAliveTicks() >= timedPotion.startingTicks() + duration) {
                // Send the packet that the potion should no longer be applied
                timedPotion.potion().sendRemovePacket(this);
                EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.potion()));
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
     * Each entity has an unique id (server-wide) which will change after a restart.
     *
     * @return the unique entity id
     * @see Instance#getEntityById(int) to retrieve an entity based on its id
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
     * Returns false just after instantiation, set to true after calling {@link #setInstance(Instance)}.
     *
     * @return true if the entity has been linked to an instance, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the current bounding box (based on pose).
     * Is used to check collision with coordinates or other blocks/entities.
     *
     * @return the entity bounding box
     */
    public @NotNull BoundingBox getBoundingBox() {
        // Check if there is a specific bounding box for this pose
        BoundingBox poseBoundingBox = BoundingBox.fromPose(getPose());
        return poseBoundingBox == null ? boundingBox : poseBoundingBox;
    }

    /**
     * Changes the internal entity standing bounding box.
     * When the pose is not standing, a different bounding box may be used for collision.
     * <p>
     * WARNING: this does not change the entity hit-box which is client-side.
     *
     * @param width  the bounding box X size
     * @param height the bounding box Y size
     * @param depth  the bounding box Z size
     */
    public void setBoundingBox(double width, double height, double depth) {
        setBoundingBox(new BoundingBox(width, height, depth));
    }

    /**
     * Changes the internal entity standing bounding box.
     * When the pose is not standing, a different bounding box may be used for collision.
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
    public @UnknownNullability Instance getInstance() {
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
        setPositionInternal(spawnPosition);
        this.previousPosition = spawnPosition;
        this.lastSyncedPosition = spawnPosition;
        this.previousPhysicsResult = null;
        this.instance = instance;
        return instance.loadOptionalChunk(spawnPosition).thenAccept(chunk -> {
            try {
                Check.notNull(chunk, "Entity has been placed in an unloaded chunk!");
                refreshCurrentChunk(chunk);
                if (this instanceof Player player) {
                    player.sendPacket(instance.createInitializeWorldBorderPacket());
                    player.sendPacket(instance.createTimePacket());
                    player.sendPackets(instance.getWeather().createWeatherPackets());
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
     * @return true if the entity is moving
     */
    public boolean hasVelocity() {
        if (isOnGround()) {
            // if the entity is on the ground and only "moves" downwards, it does not have a velocity.
            return Double.compare(velocity.x(), 0) != 0 || Double.compare(velocity.z(), 0) != 0 || velocity.y() > 0;
        } else {
            // The entity does not have velocity if the velocity is zero
            return !velocity.isZero();
        }
    }

    /**
     * Gets the aerodynamics; how the entity behaves in the air.
     *
     * @return the aerodynamic properties this entity is using
     */
    public @NotNull Aerodynamics getAerodynamics() {
        return aerodynamics;
    }

    /**
     * Sets the aerodynamics; how the entity behaves in the air.
     *
     * @param aerodynamics the new aerodynamic properties
     */
    public void setAerodynamics(@NotNull Aerodynamics aerodynamics) {
        this.aerodynamics = aerodynamics;
    }

    /**
     * Gets the number of tick this entity has been applied gravity.
     *
     * @return the number of tick of which gravity has been consequently applied
     */
    public int getGravityTickCount() {
        return gravityTickCount;
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

    public double getDistanceSquared(@NotNull Point point) {
        return getPosition().distanceSquared(point);
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
        entity.synchronizePosition();
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
        entity.synchronizePosition();
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
     * Gets the entities that this entity is leashing.
     *
     * @return an unmodifiable list containing all the leashed entities
     */
    public @NotNull Set<Entity> getLeashedEntities() {
        return Collections.unmodifiableSet(leashedEntities);
    }

    /**
     * Gets the current leash holder.
     *
     * @return the entity leashing this entity, null if no leash holder
     */
    public @Nullable Entity getLeashHolder() {
        return leashHolder;
    }

    /**
     * Sets the leash holder to this entity.
     *
     * @param entity the new leash holder
     */
    public void setLeashHolder(@Nullable Entity entity) {
        if (leashHolder != null) leashHolder.leashedEntities.remove(this);
        if (entity != null) entity.leashedEntities.add(this);
        this.leashHolder = entity;
        sendPacketToViewersAndSelf(getAttachEntityPacket());
    }

    protected @NotNull AttachEntityPacket getAttachEntityPacket() {
        Entity leashHolder = this.leashHolder;
        return new AttachEntityPacket(getEntityId(), leashHolder != null ? leashHolder.getEntityId() : -1);
    }

    /**
     * Entity statuses can be found <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Entity_statuses">here</a>.
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
        this.entityMeta.setSneaking(sneaking);
        updatePose();
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
    public @NotNull EntityPose getPose() {
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
    public void setPose(@NotNull EntityPose pose) {
        this.entityMeta.setPose(pose);
    }

    protected void updatePose() {
        if (entityMeta.isFlyingWithElytra()) {
            setPose(EntityPose.FALL_FLYING);
        } else if (entityMeta.isSwimming()) {
            setPose(EntityPose.SWIMMING);
        } else if (entityMeta instanceof LivingEntityMeta livingMeta && livingMeta.isInRiptideSpinAttack()) {
            setPose(EntityPose.SPIN_ATTACK);
        } else if (entityMeta.isSneaking()) {
            setPose(EntityPose.SNEAKING);
        } else {
            setPose(EntityPose.STANDING);
        }
    }

    /**
     * Gets the entity custom name.
     *
     * @return the custom name of the entity, null if there is not
     *
     * @deprecated use {@link net.minestom.server.component.DataComponents#CUSTOM_NAME} instead.
     */
    @Deprecated
    public @Nullable Component getCustomName() {
        return this.entityMeta.getCustomName();
    }

    /**
     * Changes the entity custom name.
     *
     * @param customName the custom name of the entity, null to remove it
     *
     * @deprecated use {@link net.minestom.server.component.DataComponents#CUSTOM_NAME} instead.
     */
    @Deprecated
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
    public void refreshPosition(@NotNull final Pos newPosition, boolean ignoreView, boolean sendPackets) {
        final var previousPosition = this.position;
        final Pos position = ignoreView ? previousPosition.withCoord(newPosition) : newPosition;
        if (position.equals(lastSyncedPosition)) return;
        setPositionInternal(position);
        this.previousPosition = previousPosition;
        if (!position.samePoint(previousPosition)) refreshCoordinate(position);
        if (nextSynchronizationTick <= ticks + 1 || !sendPackets) {
            // The entity will be synchronized at the end of its tick
            // not returning here will duplicate position packets
            return;
        }
        // Update viewers
        final boolean viewChange = !position.sameView(lastSyncedPosition);
        final double distanceX = Math.abs(position.x() - lastSyncedPosition.x());
        final double distanceY = Math.abs(position.y() - lastSyncedPosition.y());
        final double distanceZ = Math.abs(position.z() - lastSyncedPosition.z());
        final boolean positionChange = (distanceX + distanceY + distanceZ) > 0;

        final Chunk chunk = getChunk();
        assert chunk != null;
        if (distanceX > 8 || distanceY > 8 || distanceZ > 8) {
            // Send relative 0 velocity to avoid affecting it in this case
            PacketViewableUtils.prepareViewablePacket(chunk, new EntityTeleportPacket(getEntityId(), position,
                    Vec.ZERO, RelativeFlags.DELTA_COORD, isOnGround()), this);
            nextSynchronizationTick = synchronizationTicks + 1;
        } else if (positionChange && viewChange) {
            PacketViewableUtils.prepareViewablePacket(chunk, EntityPositionAndRotationPacket.getPacket(getEntityId(), position,
                    lastSyncedPosition, isOnGround()), this);
            // Fix head rotation
            PacketViewableUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
        } else if (positionChange) {
            // This is a confusing fix for a confusing issue. If rotation is only sent when the entity actually changes, then spawning an entity
            // on the ground causes the entity not to update its rotation correctly. It works fine if the entity is spawned in the air. Very weird.
            PacketViewableUtils.prepareViewablePacket(chunk, EntityPositionAndRotationPacket.getPacket(getEntityId(), position,
                    lastSyncedPosition, onGround), this);
        } else if (viewChange) {
            PacketViewableUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
            PacketViewableUtils.prepareViewablePacket(chunk, EntityPositionAndRotationPacket.getPacket(getEntityId(), position,
                    lastSyncedPosition, isOnGround()), this);
        }
        this.lastSyncedPosition = position;
    }

    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos newPosition, boolean ignoreView) {
        refreshPosition(newPosition, ignoreView, true);
    }

    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos newPosition) {
        refreshPosition(newPosition, false);
    }

    /**
     * Sets the coordinates of the passenger to the coordinates of this vehicle + {@link EntityUtils#getPassengerHeightOffset(Entity, Entity)}
     *
     * @param newPosition the new position of this vehicle
     * @param passenger   the passenger to be moved
     */
    private void updatePassengerPosition(Point newPosition, Entity passenger) {
        final Pos oldPassengerPos = passenger.position;
        final Pos newPassengerPos = oldPassengerPos.withCoord(newPosition.x(),
                newPosition.y() + EntityUtils.getPassengerHeightOffset(this, passenger),
                newPosition.z());
        passenger.setPositionInternal(newPassengerPos);
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
    @ApiStatus.Internal
    protected void refreshCoordinate(Point newPosition) {
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
            if (this instanceof Player player) player.sendChunkUpdates(newChunk);
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
     * Gets the previous entity position.
     *
     * @return the previous position of the entity
     */
    public @NotNull Pos getPreviousPosition() {
        return previousPosition;
    }

    /**
     * Gets the entity eye height.
     *
     * @return the entity eye height
     */
    public double getEyeHeight() {
        return getPose() == EntityPose.SLEEPING ? 0.2 : entityType.registry().eyeHeight();
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
        EventDispatcher.callCancellable(new EntityPotionAddEvent(this, potion), () -> {
            removeEffect(potion.effect());
            this.effects.add(new TimedPotion(potion, getAliveTicks()));
            potion.sendAddPacket(this);
        });
    }

    /**
     * Removes effect from entity, if it has it.
     *
     * @param effect The effect to remove
     */
    public void removeEffect(@NotNull PotionEffect effect) {
        this.effects.removeIf(timedPotion -> {
            if (timedPotion.potion().effect() == effect) {
                timedPotion.potion().sendRemovePacket(this);
                EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.potion()));
                return true;
            }
            return false;
        });
    }

    /**
     * If the entity has the specified effect.
     *
     * @param effect the effect to check
     */
    public boolean hasEffect(@NotNull PotionEffect effect) {
        return this.effects.stream().anyMatch(timedPotion -> timedPotion.potion().effect() == effect);
    }

    /**
     * Gets the TimedPotion of the specified effect.
     *
     * @param effect the effect type
     * @return the effect, null if not found
     */
    public @Nullable TimedPotion getEffect(@NotNull PotionEffect effect) {
        return this.effects.stream().filter(timedPotion -> timedPotion.potion().effect() == effect).findFirst().orElse(null);
    }

    /**
     * Gets the level of the specified effect.
     *
     * @param effect the effect type
     * @return the effect level, -1 if not found
     */
    public int getEffectLevel(@NotNull PotionEffect effect) {
        TimedPotion timedPotion = getEffect(effect);
        return timedPotion == null ? -1 : timedPotion.potion().amplifier();
    }

    /**
     * Removes all the effects currently applied to the entity.
     */
    public void clearEffects() {
        for (TimedPotion timedPotion : effects) {
            timedPotion.potion().sendRemovePacket(this);
            EventDispatcher.call(new EntityPotionRemoveEvent(this, timedPotion.potion()));
        }
        this.effects.clear();
    }

    /**
     * Removes the entity from the server immediately.
     * <p>
     * WARNING: this does not trigger {@link EntityDeathEvent}.
     */
    public void remove() {
        remove(true);
    }

    protected void remove(boolean permanent) {
        if (isRemoved()) return;
        EventDispatcher.call(new EntityDespawnEvent(this));
        try {
            despawn();
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(t);
        }

        // Remove passengers if any (also done with LivingEntity#kill)
        Set<Entity> passengers = getPassengers();
        if (!passengers.isEmpty()) passengers.forEach(this::removePassenger);
        final Entity vehicle = this.vehicle;
        if (vehicle != null) vehicle.removePassenger(this);

        Set<Entity> leashedEntities = getLeashedEntities();
        leashedEntities.forEach(entity -> entity.setLeashHolder(null));

        MinecraftServer.process().dispatcher().removeElement(this);
        this.removed = true;
        if (!permanent) {
            // Reset some state to be ready for re-use
            setPositionInternal(Pos.ZERO);
            this.previousPosition = Pos.ZERO;
            this.lastSyncedPosition = Pos.ZERO;
        }
        Instance currentInstance = this.instance;
        if (currentInstance != null) {
            removeFromInstance(currentInstance);
            this.instance = null;
        }
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
        return this.velocity.mul(8000f / ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    protected @NotNull SpawnEntityPacket getSpawnPacket() {
        int data = 0;
        short velocityX = 0, velocityZ = 0, velocityY = 0;
        if (getEntityMeta() instanceof ObjectDataProvider objectDataProvider) {
            data = objectDataProvider.getObjectData();
            if (objectDataProvider.requiresVelocityPacketAtSpawn()) {
                final var velocity = getVelocityForPacket();
                velocityX = (short) velocity.x();
                velocityY = (short) velocity.y();
                velocityZ = (short) velocity.z();
            }
        }
        final Pos position = getPosition();
        return new SpawnEntityPacket(getEntityId(), getUuid(), getEntityType().id(),
                position, position.yaw(), data, velocityX, velocityY, velocityZ);
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
     * Used to synchronize entity position with viewers by sending a full
     * {@link EntityPositionSyncPacket} to viewers.
     */
    @ApiStatus.Internal
    protected void synchronizePosition() {
        final Pos posCache = this.position;
        final Pos delta = posCache.sub(lastSyncedPosition);
        PacketViewableUtils.prepareViewablePacket(currentChunk, new EntityPositionSyncPacket(getEntityId(), posCache, delta, posCache.yaw(), posCache.pitch(), isOnGround()), this);
        nextSynchronizationTick = ticks + synchronizationTicks;
        this.lastSyncedPosition = posCache;
    }

    private void synchronizeView() {
        sendPacketToViewers(new EntityHeadLookPacket(getEntityId(), position.yaw()));
        sendPacketToViewers(new EntityRotationPacket(getEntityId(), position.yaw(), position.pitch(), onGround));
    }

    /**
     * Asks for a position synchronization to happen during next entity tick.
     */
    public void synchronizeNextTick() {
        this.nextSynchronizationTick = 0;
    }

    /**
     * Returns the current synchronization interval. The default value is {@link ServerFlag#ENTITY_SYNCHRONIZATION_TICKS}
     * but can be overridden per entity with {@link #setSynchronizationTicks(long)}.
     *
     * @return The current synchronization ticks
     */
    public long getSynchronizationTicks() {
        return this.synchronizationTicks;
    }

    /**
     * Set the tick period until this entity's position is synchronized.
     *
     * @param ticks the new synchronization tick period
     */
    public void setSynchronizationTicks(long ticks) {
        this.synchronizationTicks = ticks;
    }

    @Override
    public @NotNull HoverEvent<ShowEntity> asHoverEvent(@NotNull UnaryOperator<ShowEntity> op) {
        return HoverEvent.showEntity(ShowEntity.showEntity(this.entityType, this.uuid));
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
    public @NotNull EntitySnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        final Chunk chunk = currentChunk;
        final int[] viewersId = this.viewEngine.viewableOption.bitSet.toIntArray();
        final int[] passengersId = ArrayUtils.mapToIntArray(passengers, Entity::getEntityId);
        final Entity vehicle = this.vehicle;
        return new SnapshotImpl.Entity(entityType, uuid, id, position, velocity,
                updater.reference(instance), chunk.getChunkX(), chunk.getChunkZ(),
                viewersId, passengersId, vehicle == null ? -1 : vehicle.getEntityId(),
                tagHandler.readableCopy());
    }

    @Override
    public @NotNull EventNode<EntityEvent> eventNode() {
        return eventNode;
    }

    /**
     * Applies knockback to the entity
     *
     * @param strength the strength of the knockback, 0.4 is the vanilla value for a bare hand hit
     * @param x        knockback on x axle, for default knockback use the following formula <pre>sin(attacker.yaw * (pi/180))</pre>
     * @param z        knockback on z axle, for default knockback use the following formula <pre>-cos(attacker.yaw * (pi/180))</pre>
     */
    public void takeKnockback(float strength, final double x, final double z) {
        if (strength > 0) {
            //TODO check possible side effects of unnatural TPS (other than 20TPS)
            strength *= ServerFlag.SERVER_TICKS_PER_SECOND;
            final Vec velocityModifier = new Vec(x, z).normalize().mul(strength);
            final double verticalLimit = .4d * ServerFlag.SERVER_TICKS_PER_SECOND;

            setVelocity(new Vec(velocity.x() / 2d - velocityModifier.x(),
                    onGround ? Math.min(verticalLimit, velocity.y() / 2d + strength) : velocity.y(),
                    velocity.z() / 2d - velocityModifier.z()
            ));
        }
    }

    /**
     * Gets the line of sight of the entity.
     *
     * @param maxDistance The max distance to scan
     * @return A list of {@link Point points} in this entities line of sight
     */
    public List<Point> getLineOfSight(int maxDistance) {
        Instance instance = getInstance();
        if (instance == null) {
            return List.of();
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
     * Raycasts current entity's eye position to target eye position.
     *
     * @param entity    the entity to be checked.
     * @param exactView if set to TRUE, checks whether target is IN the line of sight of the current one;
     *                  otherwise checks if the current entity can rotate so that target will be in its line of sight.
     * @return true if the ray reaches the target bounding box before hitting a block.
     */
    public boolean hasLineOfSight(Entity entity, boolean exactView) {
        Instance instance = getInstance();
        if (instance == null) {
            return false;
        }

        final Pos start = position.withY(position.y() + getEyeHeight());
        final Pos end = entity.position.withY(entity.position.y() + entity.getEyeHeight());
        final Vec direction = exactView ? position.direction() : end.sub(start).asVec().normalize();
        if (!entity.boundingBox.boundingBoxRayIntersectionCheck(start.asVec(), direction, entity.getPosition())) {
            return false;
        }
        return CollisionUtils.isLineOfSightReachingShape(instance, currentChunk, start, end, entity.boundingBox);
    }

    /**
     * @param entity the entity to be checked.
     * @return if the current entity has line of sight to the given one.
     * @see Entity#hasLineOfSight(Entity, boolean)
     */
    public boolean hasLineOfSight(Entity entity) {
        return hasLineOfSight(entity, false);
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

        final Pos start = position.withY(position.y() + getEyeHeight());
        final Vec startAsVec = start.asVec();
        final Predicate<Entity> finalPredicate = e -> e != this
                && e.boundingBox.boundingBoxRayIntersectionCheck(startAsVec, position.direction(), e.getPosition())
                && predicate.test(e)
                && CollisionUtils.isLineOfSightReachingShape(instance, currentChunk, start,
                e.position.withY(e.position.y() + e.getEyeHeight()), e.boundingBox);

        Optional<Entity> nearby = instance.getNearbyEntities(position, range).stream()
                .filter(finalPredicate)
                .min(Comparator.comparingDouble(e -> e.getDistanceSquared(this)));

        return nearby.orElse(null);
    }

    @Override
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face) {
        return false;
    }

    @Override
    public boolean intersectBox(@NotNull Point positionRelative, @NotNull BoundingBox boundingBox) {
        return this.boundingBox.intersectBox(positionRelative, boundingBox);
    }

    @Override
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection, @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        return boundingBox.intersectBoxSwept(rayStart, rayDirection, shapePos, moving, finalResult);
    }

    @Override
    public @NotNull Point relativeStart() {
        return boundingBox.relativeStart();
    }

    @Override
    public @NotNull Point relativeEnd() {
        return boundingBox.relativeEnd();
    }

    public boolean hasEntityCollision() {
        return collidesWithEntities;
    }

    public boolean preventBlockPlacement() {
        // EntityMeta can change at any time, so initializing this during #initCollisions is not an option
        // Can be overridden to allow for custom behaviour
        if (entityMeta instanceof ArmorStandMeta armorStandMeta && armorStandMeta.isMarker()) return false;
        return preventBlockPlacement;
    }

    protected void updateCollisions() {
        preventBlockPlacement = !ALLOW_BLOCK_PLACEMENT_ENTITIES.contains(entityType);
        collidesWithEntities = !NO_ENTITY_COLLISION_ENTITIES.contains(entityType);
    }

    /**
     * Acquires this entity.
     *
     * @param <T> the type of object to be acquired
     * @return the acquirable for this entity
     * @deprecated It's preferred to use {@link AcquirableSource#acquirable()} instead, as it is overridden by
     * subclasses
     */
    @Deprecated
    @ApiStatus.Experimental
    public <T extends Entity> @NotNull Acquirable<T> getAcquirable() {
        return (Acquirable<T>) acquirable;
    }

    @ApiStatus.Experimental
    @Override
    public @NotNull Acquirable<? extends Entity> acquirable() {
        return acquirable;
    }

    @Override
    @Contract(pure = true)
    public @NotNull Identity identity() {
        return Identity.identity(this.uuid); // Unfortunate pollution, if we extended Identity (contains UUID static)
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pointers pointers() {
        return ENTITY_POINTERS_SUPPLIER.view(this);
    }
}
