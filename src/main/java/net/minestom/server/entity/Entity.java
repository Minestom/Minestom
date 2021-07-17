package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Could be a player, a monster, or an object.
 * <p>
 * To create your own entity you probably want to extends {@link LivingEntity} or {@link EntityCreature} instead.
 */
public class Entity implements Viewable, Tickable, TagHandler, PermissionHandler, HoverEventSource<ShowEntity>, Sound.Emitter {

    private static final Map<Integer, Entity> ENTITY_BY_ID = new ConcurrentHashMap<>();
    private static final Map<UUID, Entity> ENTITY_BY_UUID = new ConcurrentHashMap<>();
    private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

    protected Instance instance;
    protected Chunk currentChunk;
    protected Pos position;
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

    private boolean autoViewable;
    private final int id;
    protected final Set<Player> viewers = ConcurrentHashMap.newKeySet();
    private final Set<Player> unmodifiableViewers = Collections.unmodifiableSet(viewers);
    private final NBTCompound nbtCompound = new NBTCompound();
    private final Set<Permission> permissions = new CopyOnWriteArraySet<>();

    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean removed;
    private boolean shouldRemove;
    private long scheduledRemoveTime;

    private final Set<Entity> passengers = new CopyOnWriteArraySet<>();
    protected EntityType entityType; // UNSAFE to change, modify at your own risk

    // Network synchronization, send the absolute position of the entity each X milliseconds
    private static final Duration SYNCHRONIZATION_COOLDOWN = Duration.of(1, TimeUnit.MINUTE);
    private Duration customSynchronizationCooldown;
    private long lastAbsoluteSynchronizationTime;

    protected Metadata metadata = new Metadata(this);
    protected EntityMeta entityMeta;

    private final List<TimedPotion> effects = new CopyOnWriteArrayList<>();

    // list of scheduled tasks to be executed during the next entity tick
    protected final Queue<Consumer<Entity>> nextTick = new ConcurrentLinkedQueue<>();

    // Tick related
    private long ticks;
    private final EntityTickEvent tickEvent = new EntityTickEvent(this);

    private final Acquirable<Entity> acquirable = Acquirable.of(this);

    /**
     * Lock used to support #switchEntityType
     */
    private final Object entityTypeLock = new Object();

    public Entity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = uuid;
        this.position = Pos.ZERO;
        this.lastSyncedPosition = Pos.ZERO;

        setBoundingBox(entityType.getWidth(), entityType.getHeight(), entityType.getWidth());

        this.entityMeta = entityType.getMetaConstructor().apply(this, this.metadata);

        setAutoViewable(true);

        Entity.ENTITY_BY_ID.put(id, this);
        Entity.ENTITY_BY_UUID.put(uuid, this);

        initializeDefaultGravity();
    }

    public Entity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * Schedules a task to be run during the next entity tick.
     * It ensures that the task will be executed in the same thread as the entity (depending of the {@link ThreadProvider}).
     *
     * @param callback the task to execute during the next entity tick
     */
    public void scheduleNextTick(@NotNull Consumer<Entity> callback) {
        this.nextTick.add(callback);
    }

    /**
     * Gets an entity based on its id (from {@link #getEntityId()}).
     * <p>
     * Entity id are unique server-wide.
     *
     * @param id the entity unique id
     * @return the entity having the specified id, null if not found
     */
    @Nullable
    public static Entity getEntity(int id) {
        return Entity.ENTITY_BY_ID.getOrDefault(id, null);
    }

    /**
     * Gets an entity based on its UUID (from {@link #getUuid()}).
     *
     * @param uuid the entity UUID
     * @return the entity having the specified uuid, null if not found
     */
    @Nullable
    public static Entity getEntity(@NotNull UUID uuid) {
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
    @NotNull
    public EntityMeta getEntityMeta() {
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
    public @NotNull CompletableFuture<Void> teleport(@NotNull Pos position, @Nullable long[] chunks) {
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        final Runnable endCallback = () -> {
            refreshPosition(position);
            synchronizePosition(true);
            completableFuture.complete(null);
        };

        if (chunks == null || chunks.length == 0) {
            instance.loadOptionalChunk(position).thenRun(endCallback);
        } else {
            ChunkUtils.optionalLoadAll(instance, chunks, null).thenRun(endCallback);
        }
        return completableFuture;
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

        EntityRotationPacket entityRotationPacket = new EntityRotationPacket();
        entityRotationPacket.entityId = getEntityId();
        entityRotationPacket.yaw = yaw;
        entityRotationPacket.pitch = pitch;
        entityRotationPacket.onGround = onGround;

        EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
        entityHeadLookPacket.entityId = getEntityId();
        entityHeadLookPacket.yaw = yaw;

        sendPacketToViewersAndSelf(entityHeadLookPacket);
        sendPacketToViewersAndSelf(entityRotationPacket);
    }

    /**
     * When set to true, the entity will automatically get new viewers when they come too close.
     * This can be use to have complete control over which player can see it, without having to deal with
     * raw packets.
     * <p>
     * True by default for all entities.
     * When set to false, it is important to mention that the players will not be removed automatically from its viewers
     * list, you would have to do that manually using {@link #addViewer(Player)} and {@link #removeViewer(Player)}..
     *
     * @return true if the entity is automatically viewable for close players, false otherwise
     */
    public boolean isAutoViewable() {
        return autoViewable;
    }

    /**
     * Makes the entity auto viewable or only manually.
     *
     * @param autoViewable should the entity be automatically viewable for close players
     * @see #isAutoViewable()
     */
    public void setAutoViewable(boolean autoViewable) {
        this.autoViewable = autoViewable;
    }

    @Override
    public final boolean addViewer(@NotNull Player player) {
        synchronized (this.entityTypeLock) {
            return addViewer0(player);
        }
    }

    protected boolean addViewer0(@NotNull Player player) {
        if (!this.viewers.add(player)) {
            return false;
        }
        player.viewableEntities.add(this);

        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(getEntityType().getSpawnType().getSpawnPacket(this));
        if (hasVelocity()) {
            playerConnection.sendPacket(getVelocityPacket());
        }
        playerConnection.sendPacket(getMetadataPacket());

        // Passenger
        if (hasPassenger()) {
            playerConnection.sendPacket(getPassengersPacket());
        }

        // Head position
        {
            EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = position.yaw();
            playerConnection.sendPacket(entityHeadLookPacket);
        }

        return true;
    }

    @Override
    public final boolean removeViewer(@NotNull Player player) {
        synchronized (this.entityTypeLock) {
            return removeViewer0(player);
        }
    }

    protected boolean removeViewer0(@NotNull Player player) {
        if (!viewers.remove(player)) {
            return false;
        }
        player.getPlayerConnection().sendPacket(new DestroyEntitiesPacket(getEntityId()));
        player.viewableEntities.remove(this);
        return true;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return unmodifiableViewers;
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
    public void switchEntityType(@NotNull EntityType entityType) {
        synchronized (entityTypeLock) {
            this.entityType = entityType;
            this.metadata = new Metadata(this);
            this.entityMeta = entityType.getMetaConstructor().apply(this, this.metadata);

            Set<Player> viewers = new HashSet<>(getViewers());
            getViewers().forEach(this::removeViewer0);
            viewers.forEach(this::addViewer0);
        }
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
        if (instance == null)
            return;

        // Scheduled remove
        if (scheduledRemoveTime != 0) {
            final boolean finished = time >= scheduledRemoveTime;
            if (finished) {
                remove();
                return;
            }
        }

        // Instant remove
        if (shouldRemove()) {
            remove();
            return;
        }

        // Fix current chunk being null if the entity has been spawned before
        if (currentChunk == null) {
            refreshCurrentChunk(instance.getChunkAt(position));
        }

        // Check if the entity chunk is loaded
        if (!ChunkUtils.isLoaded(currentChunk)) {
            // No update for entities in unloaded chunk
            return;
        }

        // scheduled tasks
        if (!nextTick.isEmpty()) {
            Consumer<Entity> callback;
            while ((callback = nextTick.poll()) != null) {
                callback.accept(this);
            }
        }

        final boolean isNettyClient = PlayerUtils.isNettyClient(this);
        // Entity tick
        {

            // Cache the number of "gravity tick"
            if (!onGround) {
                gravityTickCount++;
            } else {
                gravityTickCount = 0;
            }

            // Velocity
            final boolean noGravity = hasNoGravity();
            boolean applyVelocity;
            // Non-player entities with either velocity or gravity enabled
            applyVelocity = !isNettyClient && (hasVelocity() || !noGravity);
            // Players with a velocity applied (client is responsible for gravity)
            applyVelocity |= isNettyClient && hasVelocity();

            if (applyVelocity) {
                final float tps = MinecraftServer.TICK_PER_SECOND;
                final Pos newPosition;
                final Vec newVelocity;

                final Vec currentVelocity = getVelocity();
                final Vec deltaPos = new Vec(
                        currentVelocity.x() / tps,
                        currentVelocity.y() / tps - (noGravity ? 0 : gravityAcceleration),
                        currentVelocity.z() / tps
                );

                if (this.hasPhysics) {
                    final var physicsResult = CollisionUtils.handlePhysics(this, deltaPos);
                    this.onGround = physicsResult.isOnGround();
                    newPosition = physicsResult.newPosition();
                    newVelocity = physicsResult.newVelocity();
                } else {
                    newVelocity = deltaPos;
                    newPosition = new Pos(
                            position.x() + velocity.x() / tps,
                            position.y() + velocity.y() / tps,
                            position.z() + velocity.z() / tps
                    );
                }

                // World border collision
                final var finalVelocityPosition = CollisionUtils.applyWorldBorder(instance, position, newPosition);
                final Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, finalVelocityPosition);
                if (!ChunkUtils.isLoaded(finalChunk)) {
                    // Entity shouldn't be updated when moving in an unloaded chunk
                    return;
                }

                // Apply the position if changed
                if (!finalVelocityPosition.samePoint(position)) {
                    refreshPosition(finalVelocityPosition, true);
                    if (!isNettyClient) {
                        synchronizePosition(true);
                    }
                }

                // Update velocity
                if (hasVelocity() || !newVelocity.isZero()) {
                    if (onGround && isNettyClient) {
                        // Stop player velocity
                        this.velocity = Vec.ZERO;
                    } else {
                        final Block block = finalChunk.getBlock(position);
                        final double drag = block.registry().friction();

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
                }

                // Verify if velocity packet has to be sent
                if (hasVelocity() || (!isNettyClient && gravityTickCount > 0)) {
                    sendPacketToViewersAndSelf(getVelocityPacket());
                }
            }

            // handle block contacts
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
                        final Block block = chunk.getBlock(x, y, z, BlockGetter.Condition.CACHED);
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

            handleVoid();

            // Call the abstract update method
            update(time);

            ticks++;
            EventDispatcher.call(tickEvent); // reuse tickEvent to avoid recreating it each tick

            // remove expired effects
            if (!effects.isEmpty()) {
                this.effects.removeIf(timedPotion -> {
                    final long potionTime = (long) timedPotion.getPotion().getDuration() * MinecraftServer.TICK_MS;
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
        }

        // Scheduled synchronization
        if (!Cooldown.hasCooldown(time, lastAbsoluteSynchronizationTime, getSynchronizationCooldown())) {
            synchronizePosition(false);
        }

        if (shouldRemove() && !MinecraftServer.isStopping()) {
            remove();
        }
    }

    /**
     * Sends the correct packets to update the entity's position, should be called
     * every tick. The movement is checked inside the method!
     * <p>
     * The following packets are sent to viewers (check are performed in this order):
     * <ol>
     *     <li>{@link EntityTeleportPacket} if {@code distanceX > 8 || distanceY > 8 || distanceZ > 8}
     *      <i>(performed using {@link #synchronizePosition(boolean)})</i></li>
     *     <li>{@link EntityPositionAndRotationPacket} if {@code positionChange && viewChange}</li>
     *     <li>{@link EntityPositionPacket} if {@code positionChange}</li>
     *     <li>{@link EntityRotationPacket} and {@link EntityHeadLookPacket} if {@code viewChange}</li>
     * </ol>
     * In case of a player's position and/or view change an additional {@link PlayerPositionAndLookPacket}
     * is sent to self.
     *
     * @param clientSide {@code true} if the client triggered this action
     */
    protected void sendPositionUpdate(final boolean clientSide) {
        final boolean viewChange = !position.sameView(lastSyncedPosition);
        final double distanceX = Math.abs(position.x() - lastSyncedPosition.x());
        final double distanceY = Math.abs(position.y() - lastSyncedPosition.y());
        final double distanceZ = Math.abs(position.z() - lastSyncedPosition.z());
        final boolean positionChange = (distanceX + distanceY + distanceZ) > 0;

        if (distanceX > 8 || distanceY > 8 || distanceZ > 8) {
            synchronizePosition(true);
            // #synchronizePosition sets sync fields, it's safe to return
            return;
        } else if (positionChange && viewChange) {
            EntityPositionAndRotationPacket positionAndRotationPacket = EntityPositionAndRotationPacket
                    .getPacket(getEntityId(), position, lastSyncedPosition, isOnGround());
            sendPacketToViewers(positionAndRotationPacket);

            // Fix head rotation
            final EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = position.yaw();
            sendPacketToViewersAndSelf(entityHeadLookPacket);
        } else if (positionChange) {
            final EntityPositionPacket entityPositionPacket = EntityPositionPacket
                    .getPacket(getEntityId(), position, lastSyncedPosition, onGround);
            sendPacketToViewers(entityPositionPacket);
        } else if (viewChange) {
            final EntityRotationPacket entityRotationPacket = new EntityRotationPacket();
            entityRotationPacket.entityId = getEntityId();
            entityRotationPacket.yaw = position.yaw();
            entityRotationPacket.pitch = position.pitch();
            entityRotationPacket.onGround = onGround;

            final EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = position.yaw();

            if (clientSide) {
                sendPacketToViewers(entityHeadLookPacket);
                sendPacketToViewers(entityRotationPacket);
            } else {
                sendPacketToViewersAndSelf(entityHeadLookPacket);
                sendPacketToViewersAndSelf(entityRotationPacket);
            }
        } else {
            // Nothing changed, return
            return;
        }

        if (PlayerUtils.isNettyClient(this) && !clientSide) {
            final PlayerPositionAndLookPacket playerPositionAndLookPacket = new PlayerPositionAndLookPacket();
            playerPositionAndLookPacket.flags = 0b111;
            playerPositionAndLookPacket.position = position.sub(lastSyncedPosition);
            playerPositionAndLookPacket.teleportId = ((Player) this).getNextTeleportId();
            ((Player) this).getPlayerConnection().sendPacket(playerPositionAndLookPacket);
        }

        this.lastSyncedPosition = position;
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
    @NotNull
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Gets the entity {@link UUID}.
     *
     * @return the entity unique id
     */
    @NotNull
    public UUID getUuid() {
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
    @NotNull
    public BoundingBox getBoundingBox() {
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
        MinecraftServer.getUpdateManager().getThreadProvider().updateEntity(this);
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
        if (this.instance != null) {
            this.instance.UNSAFE_removeEntity(this);
        }
        this.position = spawnPosition;
        this.isActive = true;
        this.instance = instance;
        return instance.loadOptionalChunk(position).thenAccept(chunk -> {
            Check.notNull(chunk, "Entity has been placed in an unloaded chunk!");
            refreshCurrentChunk(chunk);
            instance.UNSAFE_addEntity(this);
            spawn();
            EventDispatcher.call(new EntitySpawnEvent(this, instance));
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

    /**
     * Gets the distance between two entities.
     *
     * @param entity the entity to get the distance from
     * @return the distance between this and {@code entity}
     */
    public double getDistance(@NotNull Entity entity) {
        return getPosition().distance(entity.getPosition());
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
    @Nullable
    public Entity getVehicle() {
        return vehicle;
    }

    /**
     * Adds a new passenger to this entity.
     *
     * @param entity the new passenger
     * @throws NullPointerException  if {@code entity} is null
     * @throws IllegalStateException if {@link #getInstance()} returns null
     */
    public void addPassenger(@NotNull Entity entity) {
        Check.stateCondition(instance == null, "You need to set an instance using Entity#setInstance");

        if (entity.getVehicle() != null) {
            entity.getVehicle().removePassenger(entity);
        }

        this.passengers.add(entity);
        entity.vehicle = this;

        sendPacketToViewersAndSelf(getPassengersPacket());
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

        if (!passengers.remove(entity))
            return;
        entity.vehicle = null;
        sendPacketToViewersAndSelf(getPassengersPacket());
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
    @NotNull
    public Set<Entity> getPassengers() {
        return Collections.unmodifiableSet(passengers);
    }

    @NotNull
    protected SetPassengersPacket getPassengersPacket() {
        SetPassengersPacket passengersPacket = new SetPassengersPacket();
        passengersPacket.vehicleEntityId = getEntityId();

        int[] passengers = new int[this.passengers.size()];
        int counter = 0;
        for (Entity passenger : this.passengers) {
            passengers[counter++] = passenger.getEntityId();
        }

        passengersPacket.passengersId = passengers;
        return passengersPacket;
    }

    /**
     * Entity statuses can be found <a href="https://wiki.vg/Entity_statuses">here</a>.
     *
     * @param status the status to trigger
     */
    public void triggerStatus(byte status) {
        EntityStatusPacket statusPacket = new EntityStatusPacket();
        statusPacket.entityId = getEntityId();
        statusPacket.status = status;
        sendPacketToViewersAndSelf(statusPacket);
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
    @NotNull
    public Pose getPose() {
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
    @NotNull
    public void setPose(@NotNull Pose pose) {
        this.entityMeta.setPose(pose);
    }

    /**
     * Gets the entity custom name.
     *
     * @return the custom name of the entity, null if there is not
     * @deprecated Use {@link #getCustomName()}
     */
    @Deprecated
    @Nullable
    public JsonMessage getCustomNameJson() {
        return this.entityMeta.getCustomNameJson();
    }

    /**
     * Gets the entity custom name.
     *
     * @return the custom name of the entity, null if there is not
     */
    @Nullable
    public Component getCustomName() {
        return this.entityMeta.getCustomName();
    }

    /**
     * Changes the entity custom name.
     *
     * @param customName the custom name of the entity, null to remove it
     * @deprecated Use {@link #setCustomName(Component)}
     */
    @Deprecated
    public void setCustomName(@Nullable JsonMessage customName) {
        this.entityMeta.setCustomName(customName);
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
     * @param position the new position
     * @see #sendPositionUpdate(boolean)
     */
    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos position, boolean ignoreView) {
        final var previousPosition = this.position;
        this.position = ignoreView ? previousPosition.withCoord(position) : position;
        if (!position.samePoint(previousPosition)) {
            refreshCoordinate(position);
        }
        sendPositionUpdate(true);
    }

    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Pos position) {
        refreshPosition(position, false);
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
        if (hasPassenger()) {
            for (Entity passenger : getPassengers()) {
                passenger.position = passenger.position.withCoord(newPosition);
                passenger.refreshCoordinate(newPosition);
            }
        }
        final Instance instance = getInstance();
        if (instance != null) {
            final int lastChunkX = currentChunk.getChunkX();
            final int lastChunkZ = currentChunk.getChunkZ();
            final int newChunkX = ChunkUtils.getChunkCoordinate(newPosition.x());
            final int newChunkZ = ChunkUtils.getChunkCoordinate(newPosition.z());
            if (lastChunkX != newChunkX || lastChunkZ != newChunkZ) {
                // Entity moved in a new chunk
                final Chunk newChunk = instance.getChunk(newChunkX, newChunkZ);
                Check.notNull(newChunk, "The entity {0} tried to move in an unloaded chunk at {1}", getEntityId(), newPosition);
                instance.UNSAFE_switchEntityChunk(this, currentChunk, newChunk);
                if (this instanceof Player) {
                    // Refresh player view
                    final Player player = (Player) this;
                    player.refreshVisibleChunks(newChunk);
                    player.refreshVisibleEntities(newChunk);
                }
                refreshCurrentChunk(newChunk);
            }
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
    @NotNull
    public List<TimedPotion> getActiveEffects() {
        return Collections.unmodifiableList(effects);
    }

    /**
     * Adds an effect to an entity.
     *
     * @param potion The potion to add
     */
    public void addEffect(@NotNull Potion potion) {
        removeEffect(potion.getEffect());
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
            if (timedPotion.getPotion().getEffect() == effect) {
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
        if (isRemoved())
            return;

        MinecraftServer.getUpdateManager().getThreadProvider().removeEntity(this);
        this.removed = true;
        this.shouldRemove = true;
        Entity.ENTITY_BY_ID.remove(id);
        Entity.ENTITY_BY_UUID.remove(uuid);
        if (instance != null)
            instance.UNSAFE_removeEntity(this);
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
        scheduleRemove(Duration.of(delay, temporalUnit));
    }

    /**
     * Triggers {@link #remove()} after the specified time.
     *
     * @param delay the time before removing the entity,
     *              0 to cancel the removing
     */
    public void scheduleRemove(Duration delay) {
        if (delay.isZero()) { // Cancel the scheduled remove
            this.scheduledRemoveTime = 0;
            return;
        }
        this.scheduledRemoveTime = System.currentTimeMillis() + delay.toMillis();
    }

    /**
     * Gets if the entity removal has been scheduled with {@link #scheduleRemove(Duration)}.
     *
     * @return true if the entity removal has been scheduled
     */
    public boolean isRemoveScheduled() {
        return scheduledRemoveTime != 0;
    }

    protected @NotNull Vec getVelocityForPacket() {
        return this.velocity.mul(8000f / MinecraftServer.TICK_PER_SECOND);
    }

    protected @NotNull EntityVelocityPacket getVelocityPacket() {
        EntityVelocityPacket velocityPacket = new EntityVelocityPacket();
        velocityPacket.entityId = getEntityId();
        Vec velocity = getVelocityForPacket();
        velocityPacket.velocityX = (short) velocity.x();
        velocityPacket.velocityY = (short) velocity.y();
        velocityPacket.velocityZ = (short) velocity.z();
        return velocityPacket;
    }

    /**
     * Gets an {@link EntityMetaDataPacket} sent when adding viewers. Used for synchronization.
     *
     * @return The {@link EntityMetaDataPacket} related to this entity
     */
    @NotNull
    public EntityMetaDataPacket getMetadataPacket() {
        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.entries = metadata.getEntries();
        return metaDataPacket;
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
        final EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.position = position;
        entityTeleportPacket.onGround = isOnGround();
        sendPacketToViewers(entityTeleportPacket);

        this.lastAbsoluteSynchronizationTime = System.currentTimeMillis();
        this.lastSyncedPosition = position;
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
     * @deprecated Replaced by {@link #setCustomSynchronizationCooldown(Duration)}
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public void setCustomSynchronizationCooldown(@Nullable net.minestom.server.utils.time.UpdateOption cooldown) {
        setCustomSynchronizationCooldown(cooldown != null ? Duration.ofMillis(cooldown.toMilliseconds()) : null);
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

    @ApiStatus.Experimental
    public <T extends Entity> @NotNull Acquirable<T> getAcquirable(@NotNull Class<T> clazz) {
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

    /**
     * Sets the Entity's {@link gravityAcceleration} and {@link gravityDragPerTick} fields to
     * the default values according to <a href="https://minecraft.fandom.com/wiki/Entity#Motion_of_entities">Motion of entities</a>
     */
    @SuppressWarnings("JavadocReference")
    private void initializeDefaultGravity() {
        // TODO Add support for these values in the data generator
        // Acceleration
        switch (entityType) {
            // 0
            case ITEM_FRAME:
                this.gravityAcceleration = 0;
                break;
            // 0.03
            case EGG:
            case FISHING_BOBBER:
            case EXPERIENCE_BOTTLE:
            case ENDER_PEARL:
            case POTION:
            case SNOWBALL:
                this.gravityAcceleration = 0.03;
                break;
            // 0.04
            case BOAT:
            case TNT:
            case FALLING_BLOCK:
            case ITEM:
            case MINECART:
                this.gravityAcceleration = 0.04;
                break;
            // 0.05
            case ARROW:
            case SPECTRAL_ARROW:
            case TRIDENT:
                this.gravityAcceleration = 0.05;
                break;
            // 0.06
            case LLAMA_SPIT:
                this.gravityAcceleration = 0.06;
                break;
            // 0.1
            case FIREBALL:
            case WITHER_SKULL:
            case DRAGON_FIREBALL:
                this.gravityAcceleration = 0.1;
                break;
            // 0.08
            default:
                this.gravityAcceleration = 0.08;
                break;
        }

        // Drag
        switch (entityType) {
            // 0
            case BOAT:
                this.gravityDragPerTick = 0;
                break;
            // 0.01
            case LLAMA_SPIT:
            case ENDER_PEARL:
            case POTION:
            case SNOWBALL:
            case EGG:
            case TRIDENT:
            case SPECTRAL_ARROW:
            case ARROW:
                this.gravityDragPerTick = 0.01;
                break;
            // 0.05
            case MINECART:
                this.gravityDragPerTick = 0.05;
                break;
            // 0.08
            case FISHING_BOBBER:
                this.gravityDragPerTick = 0.08;
                break;
            // 0.02
            default:
                this.gravityDragPerTick = 0.02;
                break;
        }
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

    public enum Pose {
        STANDING,
        FALL_FLYING,
        SLEEPING,
        SWIMMING,
        SPIN_ATTACK,
        SNEAKING,
        DYING
    }

    protected boolean shouldRemove() {
        return shouldRemove;
    }
}
