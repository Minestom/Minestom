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
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.CustomBlock;
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
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Could be a player, a monster, or an object.
 * <p>
 * To create your own entity you probably want to extends {@link LivingEntity} or {@link EntityCreature} instead.
 */
public class Entity implements Viewable, Tickable, EventHandler<EntityEvent>, DataContainer, TagHandler, PermissionHandler, HoverEventSource<ShowEntity>, Sound.Emitter {

    private static final Map<Integer, Entity> ENTITY_BY_ID = new ConcurrentHashMap<>();
    private static final Map<UUID, Entity> ENTITY_BY_UUID = new ConcurrentHashMap<>();
    private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

    protected Instance instance;
    protected Chunk currentChunk;
    protected final Position position;
    /**
     * Used to calculate delta movement
     */
    protected final Position lastPosition;
    /**
     * Used to check if any change made to the {@link Entity#position} field since
     * the last packets sent
     */
    protected final Position lastSyncedPosition;
    protected boolean onGround;

    private BoundingBox boundingBox;

    protected Entity vehicle;

    // Velocity
    protected Vector velocity = new Vector(); // Movement in block per second
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
    private Data data;
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

    // Events
    private final EventNode<EntityEvent> eventNode;

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
        this.position = new Position();
        this.lastPosition = new Position();
        this.lastSyncedPosition = new Position();

        setBoundingBox(entityType.getWidth(), entityType.getHeight(), entityType.getWidth());

        this.entityMeta = entityType.getMetaConstructor().apply(this, this.metadata);

        setAutoViewable(true);

        Entity.ENTITY_BY_ID.put(id, this);
        Entity.ENTITY_BY_UUID.put(uuid, this);

        this.eventNode = EventNode.value("entity-" + uuid, EventFilter.ENTITY, this::equals);

        initializeDefaultGravity();
    }

    public Entity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    @Deprecated
    public Entity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Position spawnPosition) {
        this(entityType, uuid);
        this.position.set(spawnPosition);
        this.lastPosition.set(spawnPosition);
    }

    @Deprecated
    public Entity(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        this(entityType, UUID.randomUUID(), spawnPosition);
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
     * @param callback the optional callback executed, even if auto chunk is not enabled
     * @throws IllegalStateException if you try to teleport an entity before settings its instance
     */
    public void teleport(@NotNull Position position, @Nullable long[] chunks, @Nullable Runnable callback) {
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");

        final Position teleportPosition = position.clone(); // Prevent synchronization issue

        final ChunkCallback endCallback = (chunk) -> {
            refreshPosition(teleportPosition);

            synchronizePosition(true);

            OptionalCallback.execute(callback);
        };

        if (chunks == null || chunks.length == 0) {
            instance.loadOptionalChunk(teleportPosition, endCallback);
        } else {
            ChunkUtils.optionalLoadAll(instance, chunks, null, endCallback);
        }
    }

    public void teleport(@NotNull Position position, @Nullable Runnable callback) {
        teleport(position, null, callback);
    }

    public void teleport(@NotNull Position position) {
        teleport(position, null);
    }

    /**
     * Changes the view of the entity.
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    public void setView(float yaw, float pitch) {
        refreshView(yaw, pitch);

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
     * Changes the view of the entity.
     * Only the yaw and pitch are used.
     *
     * @param position the new view
     */
    public void setView(@NotNull Position position) {
        setView(position.getYaw(), position.getPitch());
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
            entityHeadLookPacket.yaw = position.getYaw();
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
        player.getPlayerConnection().sendPacket(DestroyEntityPacket.of(getEntityId()));
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

        sendPositionUpdate(false);
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
            boolean applyVelocity;
            // Non-player entities with either velocity or gravity enabled
            applyVelocity = !isNettyClient && (hasVelocity() || !hasNoGravity());
            // Players with a velocity applied (client is responsible for gravity)
            applyVelocity |= isNettyClient && hasVelocity();

            if (applyVelocity) {
                final float tps = MinecraftServer.TICK_PER_SECOND;
                final double newX = position.getX() + velocity.getX() / tps;
                final double newY = position.getY() + velocity.getY() / tps;
                final double newZ = position.getZ() + velocity.getZ() / tps;
                Position newPosition = new Position(newX, newY, newZ);

                Vector newVelocityOut = new Vector();

                // Gravity force
                final double gravityY = hasNoGravity() ? 0 : gravityAcceleration;

                final Vector deltaPos = new Vector(
                        getVelocity().getX() / tps,
                        getVelocity().getY() / tps - gravityY,
                        getVelocity().getZ() / tps
                );

                if (this.hasPhysics) {
                    this.onGround = CollisionUtils.handlePhysics(this, deltaPos, newPosition, newVelocityOut);
                } else {
                    newVelocityOut = deltaPos;
                }

                // World border collision
                final Position finalVelocityPosition = CollisionUtils.applyWorldBorder(instance, position, newPosition);
                Chunk finalChunk = currentChunk;
                if (!ChunkUtils.same(position, finalVelocityPosition)) {
                    finalChunk = instance.getChunkAt(finalVelocityPosition);

                    // Entity shouldn't be updated when moving in an unloaded chunk
                    if (!ChunkUtils.isLoaded(finalChunk)) {
                        return;
                    }
                }

                // Apply the position if changed
                if (!finalVelocityPosition.isSimilar(position)) {
                    refreshPosition(finalVelocityPosition.getX(),
                            finalVelocityPosition.getY(),
                            finalVelocityPosition.getZ());
                    sendPositionUpdate(true);
                }


                // Update velocity
                if (hasVelocity() || !newVelocityOut.isZero()) {
                    this.velocity.copy(newVelocityOut);
                    this.velocity.multiply(tps);

                    float drag;
                    if (onGround) {
                        final BlockPosition blockPosition = position.toBlockPosition();
                        final CustomBlock customBlock = finalChunk.getCustomBlock(
                                blockPosition.getX(),
                                blockPosition.getY(),
                                blockPosition.getZ());
                        if (customBlock != null) {
                            // Custom drag
                            drag = customBlock.getDrag(instance, blockPosition);
                        } else {
                            // Default ground drag
                            drag = 0.5f;
                        }

                        // Stop player velocity
                        if (isNettyClient) {
                            this.velocity.zero();
                        }
                    } else {
                        drag = 0.98f; // air drag
                    }

                    this.velocity.setX(velocity.getX() * drag);
                    this.velocity.setZ(velocity.getZ() * drag);
                    if (!hasNoGravity())
                        this.velocity.setY(velocity.getY() * (1-gravityDragPerTick));

                    if (velocity.equals(new Vector())) {
                        this.velocity.zero();
                    }
                }

                // Synchronization and packets...
                if (!isNettyClient) {
                    synchronizePosition(true);
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
            final BlockPosition tmpPosition = new BlockPosition(0, 0, 0); // allow reuse
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Chunk chunk = currentChunk;
                        if (!ChunkUtils.same(currentChunk, x, z)) {
                            chunk = instance.getChunkAt(x, z);
                            if (!ChunkUtils.isLoaded(chunk))
                                continue;
                        }

                        final CustomBlock customBlock = chunk.getCustomBlock(x, y, z);
                        if (customBlock != null) {
                            tmpPosition.setX(x);
                            tmpPosition.setY(y);
                            tmpPosition.setZ(z);
                            // checks that we are actually in the block, and not just here because of a rounding error
                            if (boundingBox.intersect(tmpPosition)) {
                                // TODO: replace with check with custom block bounding box
                                customBlock.handleContact(instance, tmpPosition, this);
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
        final boolean viewChange = !position.hasSimilarView(lastSyncedPosition);
        final double distanceX = Math.abs(position.getX() - lastSyncedPosition.getX());
        final double distanceY = Math.abs(position.getY() - lastSyncedPosition.getY());
        final double distanceZ = Math.abs(position.getZ() - lastSyncedPosition.getZ());
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
            entityHeadLookPacket.yaw = position.getYaw();
            sendPacketToViewersAndSelf(entityHeadLookPacket);
        } else if (positionChange) {
            final EntityPositionPacket entityPositionPacket = EntityPositionPacket
                    .getPacket(getEntityId(), position, lastSyncedPosition, onGround);
            sendPacketToViewers(entityPositionPacket);
        } else if (viewChange) {
            final EntityRotationPacket entityRotationPacket = new EntityRotationPacket();
            entityRotationPacket.entityId = getEntityId();
            entityRotationPacket.yaw = position.getYaw();
            entityRotationPacket.pitch = position.getPitch();
            entityRotationPacket.onGround = onGround;

            final EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = position.getYaw();

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
            playerPositionAndLookPacket.position = position.clone().subtract(lastSyncedPosition.getX(), lastSyncedPosition.getY(), lastSyncedPosition.getZ());
            playerPositionAndLookPacket.teleportId = ((Player) this).getNextTeleportId();
            ((Player) this).getPlayerConnection().sendPacket(playerPositionAndLookPacket);
        }

        lastSyncedPosition.set(position);
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

    @Override
    public @NotNull EventNode<EntityEvent> getEventNode() {
        return eventNode;
    }

    @Override
    public synchronized <V extends EntityEvent> boolean addEventCallback(@NotNull Class<V> eventClass, @NotNull EventCallback<V> eventCallback) {
        if (eventNode.getParent() == null) {
            MinecraftServer.getGlobalEventHandler().addChild(eventNode);
        }
        return EventHandler.super.addEventCallback(eventClass, eventCallback);
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
     * @throws IllegalStateException if {@code instance} has not been registered in {@link InstanceManager}
     */
    public void setInstance(@NotNull Instance instance, @NotNull Position spawnPosition) {
        Check.stateCondition(!instance.isRegistered(),
                "Instances need to be registered, please use InstanceManager#registerInstance or InstanceManager#registerSharedInstance");

        if (this.instance != null) {
            this.instance.UNSAFE_removeEntity(this);
        }

        this.position.set(spawnPosition);
        this.lastPosition.set(position);

        this.isActive = true;
        this.instance = instance;
        refreshCurrentChunk(instance.getChunkAt(position.getX(), position.getZ()));
        instance.UNSAFE_addEntity(this);
        spawn();
        EventDispatcher.call(new EntitySpawnEvent(this, instance));
    }

    /**
     * Changes the entity instance.
     *
     * @param instance the new instance of the entity
     * @throws NullPointerException  if {@code instance} is null
     * @throws IllegalStateException if {@code instance} has not been registered in {@link InstanceManager}
     */
    public void setInstance(@NotNull Instance instance) {
        setInstance(instance, this.position);
    }

    /**
     * Gets the entity current velocity.
     *
     * @return the entity current velocity
     */
    @NotNull
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Changes the entity velocity and calls {@link EntityVelocityEvent}.
     * <p>
     * The final velocity can be cancelled or modified by the event.
     *
     * @param velocity the new entity velocity
     */
    public void setVelocity(@NotNull Vector velocity) {
        EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
        EventDispatcher.callCancellable(entityVelocityEvent, () -> {
            this.velocity.copy(entityVelocityEvent.getVelocity());
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
     * @param gravityDragPerTick      the gravity drag per tick in block
     * @param gravityAcceleration     the gravity acceleration in block
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
        return getPosition().getDistance(entity.getPosition());
    }

    /**
     * Gets the distance squared between two entities.
     *
     * @param entity the entity to get the distance from
     * @return the distance squared between this and {@code entity}
     */
    public double getDistanceSquared(@NotNull Entity entity) {
        return getPosition().getDistanceSquared(entity.getPosition());
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
     * Used to refresh the entity and its passengers position
     * - put the entity in the right instance chunk
     * - update the viewable chunks (load and unload)
     * - add/remove players from the viewers list if {@link #isAutoViewable()} is enabled
     * <p>
     * WARNING: unsafe, should only be used internally in Minestom. Use {@link #teleport(Position)} instead.
     *
     * @param x new position X
     * @param y new position Y
     * @param z new position Z
     */
    private void refreshPosition(double x, double y, double z) {
        position.setX(x);
        position.setY(y);
        position.setZ(z);

        if (hasPassenger()) {
            for (Entity passenger : getPassengers()) {
                passenger.refreshPosition(x, y, z);
            }
        }

        final Instance instance = getInstance();
        if (instance != null) {
            final int lastChunkX = currentChunk.getChunkX();
            final int lastChunkZ = currentChunk.getChunkZ();

            final int newChunkX = ChunkUtils.getChunkCoordinate(x);
            final int newChunkZ = ChunkUtils.getChunkCoordinate(z);

            if (lastChunkX != newChunkX || lastChunkZ != newChunkZ) {
                // Entity moved in a new chunk
                final Chunk newChunk = instance.getChunk(newChunkX, newChunkZ);
                Check.notNull(newChunk, "The entity {0} tried to move in an unloaded chunk at {1};{2}", getEntityId(), x, z);

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

        this.lastPosition.setX(position.getX());
        this.lastPosition.setY(position.getY());
        this.lastPosition.setZ(position.getZ());
    }

    /**
     * Updates internal fields and sends updates
     *
     * @param position the new position
     * @see #refreshPosition(double, double, double)
     * @see #refreshView(float, float)
     * @see #sendPositionUpdate(boolean)
     */
    @ApiStatus.Internal
    public void refreshPosition(@NotNull final Position position) {
        if (!position.isSimilar(this.position))
            refreshPosition(position.getX(), position.getY(), position.getZ());
        refreshView(position.getYaw(), position.getPitch());
        sendPositionUpdate(true);
    }

    /**
     * Updates the entity view internally.
     * <p>
     * Warning: you probably want to use {@link #setView(float, float)}.
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     */
    private void refreshView(final float yaw, final float pitch) {
        lastPosition.setYaw(position.getYaw());
        lastPosition.setPitch(position.getPitch());
        position.setYaw(yaw);
        position.setPitch(pitch);
    }

    /**
     * Gets the entity position.
     *
     * @return the current position of the entity
     */
    @NotNull
    public Position getPosition() {
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

        // Remove passengers if any (also done with LivingEntity#kill)
        if (hasPassenger()) {
            getPassengers().forEach(this::removePassenger);
        }
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
     * @param delay    the time before removing the entity,
     *                 0 to cancel the removing
     * @param temporalUnit the unit of the delay
     */
    public void scheduleRemove(long delay, @NotNull TemporalUnit temporalUnit) {
        scheduleRemove(Duration.of(delay, temporalUnit));
    }

    /**
     * Triggers {@link #remove()} after the specified time.
     *
     * @param delay    the time before removing the entity,
     *                 0 to cancel the removing
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

    @NotNull
    protected Vector getVelocityForPacket() {
        return this.velocity.clone().multiply(8000f / MinecraftServer.TICK_PER_SECOND);
    }

    @NotNull
    protected EntityVelocityPacket getVelocityPacket() {
        EntityVelocityPacket velocityPacket = new EntityVelocityPacket();
        velocityPacket.entityId = getEntityId();
        Vector velocity = getVelocityForPacket();
        velocityPacket.velocityX = (short) velocity.getX();
        velocityPacket.velocityY = (short) velocity.getY();
        velocityPacket.velocityZ = (short) velocity.getZ();
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
        final Position pos = position.clone();
        final EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.position = pos;
        entityTeleportPacket.onGround = isOnGround();
        sendPacketToViewers(entityTeleportPacket);

        this.lastAbsoluteSynchronizationTime = System.currentTimeMillis();
        this.lastSyncedPosition.set(pos);
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
     * @param x knockback on x axle, for default knockback use the following formula <pre>sin(attacker.yaw * (pi/180))</pre>
     * @param z knockback on z axle, for default knockback use the following formula <pre>-cos(attacker.yaw * (pi/180))</pre>
     */
    public void takeKnockback(final float strength, final double x, final double z) {
        if (strength > 0) {
            //TODO check possible side effects of unnatural TPS (other than 20TPS)
            final Vector velocityModifier = new Vector(x, 0d, z).normalize().multiply(strength * MinecraftServer.TICK_PER_SECOND / 2);
            this.velocity.setX(velocity.getX() / 2d - velocityModifier.getX());
            this.velocity.setY(onGround ? Math.min(.4d, velocity.getY() / 2d + strength) * MinecraftServer.TICK_PER_SECOND : velocity.getY());
            this.velocity.setZ(velocity.getZ() / 2d - velocityModifier.getZ());
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
