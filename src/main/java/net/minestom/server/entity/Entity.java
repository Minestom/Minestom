package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.entity.EntityUtils;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Entity implements Viewable, EventHandler, DataContainer {

    private static final Map<Integer, Entity> entityById = new ConcurrentHashMap<>();
    private static final AtomicInteger lastEntityId = new AtomicInteger();

    // Metadata
    protected static final byte METADATA_BYTE = 0;
    protected static final byte METADATA_VARINT = 1;
    protected static final byte METADATA_FLOAT = 2;
    protected static final byte METADATA_STRING = 3;
    protected static final byte METADATA_CHAT = 4;
    protected static final byte METADATA_OPTCHAT = 5;
    protected static final byte METADATA_SLOT = 6;
    protected static final byte METADATA_BOOLEAN = 7;
    protected static final byte METADATA_ROTATION = 8;
    protected static final byte METADATA_POSITION = 9;
    protected static final byte METADATA_PARTICLE = 15;
    protected static final byte METADATA_POSE = 18;

    protected Instance instance;
    protected Position position;
    protected float lastX, lastY, lastZ;
    protected float cacheX, cacheY, cacheZ; // Used to synchronize with #getPosition
    protected float lastYaw, lastPitch;
    protected float cacheYaw, cachePitch;
    protected boolean onGround;

    private BoundingBox boundingBox;

    protected Entity vehicle;

    // Velocity
    protected Vector velocity = new Vector(); // Movement in block per second
    protected long lastVelocityUpdateTime; // Reset velocity to 0 after countdown
    private long velocityUpdatePeriod;

    protected float gravityDragPerTick;
    protected float eyeHeight;

    private boolean autoViewable;
    private final int id;
    private Data data;
    private final Set<Player> viewers = new CopyOnWriteArraySet<>();

    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean removed;
    private boolean shouldRemove;
    private long scheduledRemoveTime;

    private final Set<Entity> passengers = new CopyOnWriteArraySet<>();
    private long lastUpdate;
    private final EntityType entityType;

    // Network synchronization
    private static final long SYNCHRONIZATION_DELAY = 1500; // In ms
    private long lastSynchronizationTime;

    // Events
    private final Map<Class<? extends Event>, Collection<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();

    // Metadata
    protected boolean onFire;
    protected boolean crouched;
    protected boolean UNUSED_METADATA;
    protected boolean sprinting;
    protected boolean swimming;
    protected boolean invisible;
    protected boolean glowing;
    protected boolean usingElytra;
    protected int air = 300;
    protected ColoredText customName;
    protected boolean customNameVisible;
    protected boolean silent;
    protected boolean noGravity;
    protected Pose pose = Pose.STANDING;

    protected final List<Consumer<Entity>> nextTick = Collections.synchronizedList(new ArrayList<>());

    // Tick related
    private long ticks;
    private final EntityTickEvent tickEvent = new EntityTickEvent(this);

    public Entity(EntityType entityType, Position spawnPosition) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = UUID.randomUUID();
        this.position = spawnPosition.clone();

        setBoundingBox(0, 0, 0);

        setAutoViewable(true);

        entityById.put(id, this);
        setVelocityUpdatePeriod(5);
    }

    public void scheduleNextTick(Consumer<Entity> callback) {
        nextTick.add(callback);
    }

    public Entity(EntityType entityType) {
        this(entityType, new Position());
    }

    /**
     * @param id the entity unique id ({@link #getEntityId()})
     * @return the entity having the specified id, null if not found
     */
    public static Entity getEntity(int id) {
        return entityById.getOrDefault(id, null);
    }

    private static int generateId() {
        return lastEntityId.incrementAndGet();
    }

    /**
     * Called each tick
     *
     * @param time the time of update in milliseconds
     */
    public abstract void update(long time);

    /**
     * Called when a new instance is set
     */
    public abstract void spawn();

    public boolean isOnGround() {
        return onGround || EntityUtils.isOnGround(this) /* backup for levitating entities */;
    }

    /**
     * Checks if now is a good time to send a velocity update packet
     *
     * @param time the current time in milliseconds
     * @return true if the velocity update packet should be send
     */
    protected boolean shouldSendVelocityUpdate(long time) {
        return (time - lastVelocityUpdateTime) >= velocityUpdatePeriod;
    }

    /**
     * Gets the period, in ms, between two velocity update packets
     *
     * @return period, in ms, between two velocity update packets
     */
    public long getVelocityUpdatePeriod() {
        return velocityUpdatePeriod;
    }

    /**
     * Sets the period, in ms, between two velocity update packets
     *
     * @param velocityUpdatePeriod period, in ms, between two velocity update packets
     */
    public void setVelocityUpdatePeriod(long velocityUpdatePeriod) {
        this.velocityUpdatePeriod = velocityUpdatePeriod;
    }

    /**
     * Teleport the entity only if the chunk at {@code position} is loaded or if
     * {@link Instance#hasEnabledAutoChunkLoad()} returns true
     *
     * @param position the teleport position
     * @param callback the callback executed, even if auto chunk is not enabled
     */
    public void teleport(Position position, Runnable callback) {
        Check.notNull(position, "Teleport position cannot be null");
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");

        final Runnable runnable = () -> {
            if (!this.position.isSimilar(position)) {
                refreshPosition(position.getX(), position.getY(), position.getZ());
            }
            if (!this.position.hasSimilarView(position)) {
                refreshView(position.getYaw(), position.getPitch());
            }
            sendSynchronization();
            if (callback != null)
                callback.run();
        };

        if (instance.hasEnabledAutoChunkLoad()) {
            instance.loadChunk(position, chunk -> runnable.run());
        } else {
            runnable.run();
        }
    }

    public void teleport(Position position) {
        teleport(position, null);
    }

    /**
     * Change the view of the entity
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
     * Change the view of the entity
     * Only the yaw and pitch is used
     *
     * @param position the new view
     */
    public void setView(Position position) {
        setView(position.getYaw(), position.getPitch());
    }

    /**
     * When set to true, the entity will automatically get new viewers when they come too close
     * This can be use to complete control over which player can see it, without having to deal with
     * raw packets
     * <p>
     * True by default for all entities
     * When set to false, it is important to mention that the players will not be removed automatically from its viewers
     * list, you would have to do that manually when being too far
     *
     * @return true if the entity is automatically viewable for close players, false otherwise
     */
    public boolean isAutoViewable() {
        return autoViewable;
    }

    /**
     * @param autoViewable should the entity be automatically viewable for close players
     */
    public void setAutoViewable(boolean autoViewable) {
        this.autoViewable = autoViewable;
    }

    @Override
    public boolean addViewer(Player player) {
        Check.notNull(player, "Viewer cannot be null");
        boolean result = this.viewers.add(player);
        if (!result)
            return false;
        player.viewableEntities.add(this);
        return true;
    }

    @Override
    public boolean removeViewer(Player player) {
        Check.notNull(player, "Viewer cannot be null");
        if (!viewers.remove(player))
            return false;

        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
        destroyEntitiesPacket.entityIds = new int[]{getEntityId()};
        player.getPlayerConnection().sendPacket(destroyEntitiesPacket);
        player.viewableEntities.remove(this);
        return true;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    /**
     * Update the entity, called every tick
     *
     * @param time the update time in milliseconds
     */
    public void tick(long time) {
        if (instance == null)
            return;

        if (scheduledRemoveTime != 0) { // Any entity with scheduled remove does not update
            final boolean finished = time >= scheduledRemoveTime;
            if (finished) {
                remove();
            }
            return;
        }

        if (shouldRemove()) {
            remove();
            return;
        }

        BlockPosition blockPosition = position.toBlockPosition();
        if (!ChunkUtils.isLoaded(instance, position.getX(), position.getZ()) || !ChunkUtils.isLoaded(instance, blockPosition.getX(), blockPosition.getZ())) {
            // No update for entities in unloaded chunk
            return;
        }

        synchronized (nextTick) {
            for (final Consumer<Entity> e : nextTick) {
                e.accept(this);
            }
            nextTick.clear();
        }
        // Synchronization with updated fields in #getPosition()
        {
            // X/Y/Z axis
            if (cacheX != position.getX() ||
                    cacheY != position.getY() ||
                    cacheZ != position.getZ()) {
                teleport(position);
            }
            // Yaw/Pitch
            if (cacheYaw != position.getYaw() ||
                    cachePitch != position.getPitch()) {
                setView(position);
            }
        }

        if (shouldUpdate(time)) {
            this.lastUpdate = time;

            // Velocity
            final boolean applyVelocity = !PlayerUtils.isNettyClient(this) ||
                    (PlayerUtils.isNettyClient(this) && hasVelocity());
            if (applyVelocity) {
                final float tps = MinecraftServer.TICK_PER_SECOND;
                float newX = position.getX() + velocity.getX() / tps;
                float newY = position.getY() + velocity.getY() / tps;
                float newZ = position.getZ() + velocity.getZ() / tps;

                Position newPosition = new Position(newX, newY, newZ);

                if (!noGravity) {
                    velocity.setY(velocity.getY() - gravityDragPerTick * tps);
                }

                Vector newVelocityOut = new Vector();
                final Vector deltaPos = new Vector(
                        getVelocity().getX() / tps,
                        getVelocity().getY() / tps,
                        getVelocity().getZ() / tps
                );
                onGround = CollisionUtils.handlePhysics(this, deltaPos, newPosition, newVelocityOut);

                // Check chunk
                if (!ChunkUtils.isLoaded(instance, newPosition.getX(), newPosition.getZ())) {
                    return;
                }

                // World border collision
                {
                    final WorldBorder worldBorder = instance.getWorldBorder();
                    final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
                    switch (collisionAxis) {
                        case NONE:
                            // Apply velocity + gravity
                            refreshPosition(newPosition);
                            break;
                        case BOTH:
                            // Apply Y velocity/gravity
                            refreshPosition(position.getX(), newPosition.getY(), position.getZ());
                            break;
                        case X:
                            // Apply Y/Z velocity/gravity
                            refreshPosition(position.getX(), newPosition.getY(), newPosition.getZ());
                            break;
                        case Z:
                            // Apply X/Y velocity/gravity
                            refreshPosition(newPosition.getX(), newPosition.getY(), position.getZ());
                            break;
                    }
                }

                velocity.copy(newVelocityOut);
                velocity.multiply(tps);

                float drag;
                if (onGround) {
                    final CustomBlock customBlock =
                            instance.getCustomBlock(blockPosition);
                    if (customBlock != null) {
                        // Custom drag
                        drag = customBlock.getDrag(instance, blockPosition);
                    } else {
                        // Default ground drag
                        drag = 0.5f;
                    }

                    // Stop player velocity
                    if (PlayerUtils.isNettyClient(this)) {
                        velocity.zero();
                    }
                } else {
                    drag = 0.98f; // air drag
                }

                velocity.setX(velocity.getX() * drag);
                velocity.setZ(velocity.getZ() * drag);

                sendSynchronization();

                if (shouldSendVelocityUpdate(time)) {
                    sendVelocityPacket();
                    lastVelocityUpdateTime = time;
                }
            }

            // handle block contacts
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
                        final Chunk chunk = instance.getChunkAt(x, z);
                        if (!ChunkUtils.isLoaded(chunk))
                            continue;

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
            callEvent(EntityTickEvent.class, tickEvent); // reuse tickEvent to avoid recreating it each tick
        }

        // Scheduled synchronization
        if (time - lastSynchronizationTime >= SYNCHRONIZATION_DELAY) {
            lastSynchronizationTime = time;
            sendSynchronization();
        }

        if (shouldRemove()) {
            remove();
        }
    }

    /**
     * Equivalent to <code>sendPacketsToViewers(getVelocityPacket());</code>
     */
    public void sendVelocityPacket() {
        sendPacketsToViewers(getVelocityPacket());
    }

    /**
     * Get the number of ticks this entity has been active for
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
    public Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap() {
        return eventCallbacks;
    }

    @Override
    public <E extends Event> void callEvent(Class<E> eventClass, E event) {
        EventHandler.super.callEvent(eventClass, event);

        // Call the same event for the current entity instance
        if (instance != null) {
            instance.callEvent(eventClass, event);
        }
    }

    /**
     * Each entity has an unique id which will change after a restart
     * All entities can be retrieved by calling {@link Entity#getEntity(int)}
     *
     * @return the unique entity id
     */
    public int getEntityId() {
        return id;
    }

    /**
     * Return the entity type id, can convert using {@link EntityType#fromId(int)}
     *
     * @return the entity type id
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Get the entity UUID
     *
     * @return the entity UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Change the internal entity UUID, mostly unsafe
     *
     * @param uuid the new entity uuid
     */
    protected void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Return false just after instantiation, set to true after calling {@link #setInstance(Instance)}
     *
     * @return true if the entity has been linked to an instance, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Is used to check collision with coordinates or other blocks/entities
     *
     * @return the entity bounding box
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Change the internal entity bounding box
     * <p>
     * WARNING: this does not change the entity hit-box which is client-side
     *
     * @param x the bounding box X size
     * @param y the bounding box Y size
     * @param z the bounding box Z size
     */
    public void setBoundingBox(float x, float y, float z) {
        this.boundingBox = new BoundingBox(this, x, y, z);
    }

    /**
     * Convenience method to get the entity current chunk
     *
     * @return the entity chunk
     */
    public Chunk getChunk() {
        return instance.getChunkAt(lastX, lastZ);
    }

    /**
     * Get the entity current instance
     *
     * @return the entity instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * Change the entity instance
     *
     * @param instance the new instance of the entity
     * @throws NullPointerException  if {@code instance} is null
     * @throws IllegalStateException if {@code instance} has not been registered in {@link InstanceManager}
     */
    public void setInstance(Instance instance) {
        Check.notNull(instance, "instance cannot be null!");
        Check.stateCondition(!instance.isRegistered(),
                "Instances need to be registered, please use InstanceManager#registerInstance or InstanceManager#registerSharedInstance");

        if (this.instance != null) {
            this.instance.removeEntity(this);
        }

        this.isActive = true;
        this.instance = instance;
        instance.addEntity(this);
        spawn();
        EntitySpawnEvent entitySpawnEvent = new EntitySpawnEvent(this, instance);
        callEvent(EntitySpawnEvent.class, entitySpawnEvent);
    }

    /**
     * Get the entity current velocity
     *
     * @return the entity current velocity
     */
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Change the entity velocity and calls {@link EntityVelocityEvent}.
     * <p>
     * The final velocity can be cancelled or modified by the event
     *
     * @param velocity the new entity velocity
     */
    public void setVelocity(Vector velocity) {
        EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
        callCancellableEvent(EntityVelocityEvent.class, entityVelocityEvent, () -> {
            this.velocity.copy(entityVelocityEvent.getVelocity());
            sendPacketToViewersAndSelf(getVelocityPacket());
        });
    }

    /**
     * Get if the entity currently has a velocity applied
     *
     * @return true if velocity is not set to 0
     */
    public boolean hasVelocity() {
        return velocity.getX() != 0 ||
                velocity.getY() != 0 ||
                velocity.getZ() != 0;
    }

    /**
     * Change the gravity of the entity
     *
     * @param gravityDragPerTick the gravity drag per tick
     */
    public void setGravity(float gravityDragPerTick) {
        this.gravityDragPerTick = gravityDragPerTick;
    }

    /**
     * Get the distance between two entities
     *
     * @param entity the entity to get the distance from
     * @return the distance between this and {@code entity}
     */
    public float getDistance(Entity entity) {
        Check.notNull(entity, "Entity cannot be null");
        return getPosition().getDistance(entity.getPosition());
    }

    /**
     * Get the entity vehicle or null
     *
     * @return the entity vehicle, or null if there is not any
     */
    public Entity getVehicle() {
        return vehicle;
    }

    /**
     * Add a new passenger to this entity
     *
     * @param entity the new passenger
     * @throws NullPointerException  if {@code entity} is null
     * @throws IllegalStateException if {@link #getInstance()} returns null
     */
    public void addPassenger(Entity entity) {
        Check.notNull(entity, "Passenger cannot be null");
        Check.stateCondition(instance == null, "You need to set an instance using Entity#setInstance");

        if (entity.getVehicle() != null) {
            entity.getVehicle().removePassenger(entity);
        }

        this.passengers.add(entity);
        entity.vehicle = this;

        sendPacketToViewersAndSelf(getPassengersPacket());
    }

    /**
     * Remove a passenger to this entity
     *
     * @param entity the passenger to remove
     * @throws NullPointerException  if {@code entity} is null
     * @throws IllegalStateException if {@link #getInstance()} returns null
     */
    public void removePassenger(Entity entity) {
        Check.notNull(entity, "Passenger cannot be null");
        Check.stateCondition(instance == null, "You need to set an instance using Entity#setInstance");

        if (!passengers.remove(entity))
            return;
        entity.vehicle = null;
        sendPacketToViewersAndSelf(getPassengersPacket());
    }

    /**
     * Get if the entity has any passenger
     *
     * @return true if the entity has any passenger, false otherwise
     */
    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    /**
     * Get the entity passengers
     *
     * @return an unmodifiable list containing all the entity passengers
     */
    public Set<Entity> getPassengers() {
        return Collections.unmodifiableSet(passengers);
    }

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
     * Entity statuses can be find <a href="https://wiki.vg/Entity_statuses">here</a>
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
     * Get if the entity is on fire
     *
     * @return true if the entity is in fire, false otherwise
     */
    public boolean isOnFire() {
        return onFire;
    }

    /**
     * Set the entity in fire visually
     * <p>
     * WARNING: if you want to apply damage or specify a duration,
     * see {@link LivingEntity#setFireForDuration(int, TimeUnit)}
     *
     * @param fire should the entity be set in fire
     */
    public void setOnFire(boolean fire) {
        this.onFire = fire;
        sendMetadataIndex(0);
    }

    /**
     * Get if the entity is invisible or not
     *
     * @return true if the entity is invisible, false otherwise
     */
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Change the internal invisible value and send a {@link EntityMetaDataPacket}
     * to make visible or invisible the entity to its viewers
     *
     * @param invisible true to set the entity invisible, false otherwise
     */
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        sendMetadataIndex(0);
    }

    /**
     * Get if the entity is glowing or not
     *
     * @return true if the entity is glowing, false otherwise
     */
    public boolean isGlowing() {
        return glowing;
    }

    /**
     * Set or remove the entity glowing effect
     *
     * @param glowing true to make the entity glows, false otherwise
     */
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        sendMetadataIndex(0);
    }

    /**
     * Get the entity custom name
     *
     * @return the custom name of the entity, null if there is not
     */
    public ColoredText getCustomName() {
        return customName;
    }

    /**
     * Change the entity custom name
     *
     * @param customName the custom name of the entity, null to remove it
     */
    public void setCustomName(ColoredText customName) {
        this.customName = customName;
        sendMetadataIndex(2);
    }

    /**
     * Get the custom name visible metadata field
     *
     * @return true if the custom name is visible, false otherwise
     */
    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    /**
     * Change the internal custom name visible field and send a {@link EntityMetaDataPacket}
     * to update the entity state to its viewers
     *
     * @param customNameVisible true to make the custom name visible, false otherwise
     */
    public void setCustomNameVisible(boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
        sendMetadataIndex(3);
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
        sendMetadataIndex(4);
    }

    /**
     * Change the noGravity metadata field and change the gravity behaviour accordingly
     *
     * @param noGravity should the entity ignore gravity
     */
    public void setNoGravity(boolean noGravity) {
        this.noGravity = noGravity;
        sendMetadataIndex(5);
    }

    /**
     * Get the noGravity metadata field
     *
     * @return true if the entity ignore gravity, false otherwise
     */
    public boolean hasNoGravity() {
        return noGravity;
    }

    /**
     * Used to refresh the entity and its passengers position
     * - put the entity in the right instance chunk
     * - update the viewable chunks (load and unload)
     * - add/remove players from the viewers list if {@link #isAutoViewable()} is enabled
     * <p>
     * WARNING: unsafe, should only be used internally in Minestom
     *
     * @param x new position X
     * @param y new position Y
     * @param z new position Z
     */
    public void refreshPosition(float x, float y, float z) {
        this.lastX = position.getX();
        this.lastY = position.getY();
        this.lastZ = position.getZ();
        position.setX(x);
        position.setY(y);
        position.setZ(z);
        this.cacheX = x;
        this.cacheY = y;
        this.cacheZ = z;

        if (hasPassenger()) {
            for (Entity passenger : getPassengers()) {
                passenger.refreshPosition(x, y, z);
            }
        }

        final Instance instance = getInstance();
        if (instance != null) {
            final Chunk lastChunk = instance.getChunkAt(lastX, lastZ);
            final Chunk newChunk = instance.getChunkAt(x, z);
            if (lastChunk != null && newChunk != null && lastChunk != newChunk) {
                synchronized (instance) {
                    instance.removeEntityFromChunk(this, lastChunk);
                    instance.addEntityToChunk(this, newChunk);
                }
                updateView(lastChunk, newChunk);
            }
        }
    }

    /**
     * @param position the new position
     * @see #refreshPosition(float, float, float)
     */
    public void refreshPosition(Position position) {
        refreshPosition(position.getX(), position.getY(), position.getZ());
    }

    private void updateView(Chunk lastChunk, Chunk newChunk) {
        final boolean isPlayer = this instanceof Player;

        if (isPlayer)
            ((Player) this).onChunkChange(newChunk); // Refresh loaded chunk

        // Refresh entity viewable list
        final long[] lastVisibleChunksEntity = ChunkUtils.getChunksInRange(new Position(16 * lastChunk.getChunkX(), 0, 16 * lastChunk.getChunkZ()), MinecraftServer.ENTITY_VIEW_DISTANCE);
        final long[] updatedVisibleChunksEntity = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), MinecraftServer.ENTITY_VIEW_DISTANCE);

        final int[] oldChunksEntity = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunksEntity, updatedVisibleChunksEntity);
        for (int index : oldChunksEntity) {
            final long chunkIndex = lastVisibleChunksEntity[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk == null)
                continue;
            instance.getChunkEntities(chunk).forEach(ent -> {
                if (ent instanceof Player) {
                    final Player player = (Player) ent;
                    if (isAutoViewable())
                        removeViewer(player);
                    if (isPlayer) {
                        player.removeViewer((Player) this);
                    }
                } else if (isPlayer) {
                    ent.removeViewer((Player) this);
                }
            });
        }

        final int[] newChunksEntity = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunksEntity, lastVisibleChunksEntity);
        for (int index : newChunksEntity) {
            final long chunkIndex = updatedVisibleChunksEntity[index];
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk == null)
                continue;
            instance.getChunkEntities(chunk).forEach(ent -> {
                if (ent instanceof Player) {
                    Player player = (Player) ent;
                    if (isAutoViewable())
                        addViewer(player);
                    if (this instanceof Player) {
                        player.addViewer((Player) this);
                    }
                } else if (isPlayer) {
                    ent.addViewer((Player) this);
                }
            });
        }
    }

    /**
     * Update the entity view internally
     * <p>
     * Warning: you probably want to use {@link #setView(float, float)}
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     */
    public void refreshView(float yaw, float pitch) {
        this.lastYaw = position.getYaw();
        this.lastPitch = position.getPitch();
        position.setYaw(yaw);
        position.setPitch(pitch);
        this.cacheYaw = yaw;
        this.cachePitch = pitch;
    }

    public void refreshSneaking(boolean sneaking) {
        this.crouched = sneaking;
        this.pose = sneaking ? Pose.SNEAKING : Pose.STANDING;
        sendMetadataIndex(0);
        sendMetadataIndex(6);
    }

    public void refreshSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        sendMetadataIndex(0);
    }

    /**
     * Get the entity position
     *
     * @return the current position of the entity
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Get the entity eye height
     *
     * @return the entity eye height
     */
    public float getEyeHeight() {
        return eyeHeight;
    }

    /**
     * Change the entity eye height
     *
     * @param eyeHeight the entity eye height
     */
    public void setEyeHeight(float eyeHeight) {
        this.eyeHeight = eyeHeight;
    }

    /**
     * Get if this entity is in the same chunk as the specified position
     *
     * @param position the checked position chunk
     * @return true if the entity is in the same chunk as {@code position}
     */
    public boolean sameChunk(Position position) {
        Check.notNull(position, "Position cannot be null");
        final Position pos = getPosition();
        final int chunkX1 = ChunkUtils.getChunkCoordinate((int) Math.floor(pos.getX()));
        final int chunkZ1 = ChunkUtils.getChunkCoordinate((int) Math.floor(pos.getZ()));

        final int chunkX2 = ChunkUtils.getChunkCoordinate((int) Math.floor(position.getX()));
        final int chunkZ2 = ChunkUtils.getChunkCoordinate((int) Math.floor(position.getZ()));

        return chunkX1 == chunkX2 && chunkZ1 == chunkZ2;
    }

    /**
     * Get if the entity is in the same chunk as another
     *
     * @param entity the entity to check
     * @return true if both entities are in the same chunk, false otherwise
     */
    public boolean sameChunk(Entity entity) {
        return sameChunk(entity.getPosition());
    }

    /**
     * Remove the entity from the server immediately
     * WARNING: this do not trigger the {@link EntityDeathEvent} event
     */
    public void remove() {
        this.removed = true;
        this.shouldRemove = true;
        entityById.remove(id);
        if (instance != null)
            instance.removeEntity(this);
    }

    /**
     * Get if this entity has been removed
     *
     * @return true if this entity is removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Trigger {@link #remove()} after the specified time
     *
     * @param delay    the time before removing the entity
     * @param timeUnit the unit of the delay
     */
    public void scheduleRemove(long delay, TimeUnit timeUnit) {
        delay = timeUnit.toMilliseconds(delay);

        if (delay == 0) { // Cancel the scheduled remove
            this.scheduledRemoveTime = 0;
            return;
        }
        this.scheduledRemoveTime = System.currentTimeMillis() + delay;
    }

    /**
     * Get if the entity removal is scheduled
     *
     * @return true if {@link #scheduleRemove(long, TimeUnit)} has been called, false otherwise
     */
    public boolean isRemoveScheduled() {
        return scheduledRemoveTime != 0;
    }

    protected EntityVelocityPacket getVelocityPacket() {
        final float strength = 8000f / MinecraftServer.TICK_PER_SECOND;
        EntityVelocityPacket velocityPacket = new EntityVelocityPacket();
        velocityPacket.entityId = getEntityId();
        velocityPacket.velocityX = (short) (velocity.getX() * strength);
        velocityPacket.velocityY = (short) (velocity.getY() * strength);
        velocityPacket.velocityZ = (short) (velocity.getZ() * strength);
        return velocityPacket;
    }

    /**
     * Used to sync entities together, and sent when adding viewers
     *
     * @return The {@link EntityMetaDataPacket} related to this entity
     */
    public EntityMetaDataPacket getMetadataPacket() {
        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.consumer = getMetadataConsumer();
        return metaDataPacket;
    }

    /**
     * Should be override when wanting to add a new metadata index
     *
     * @return The consumer used to write {@link EntityMetaDataPacket} in {@link #getMetadataPacket()}
     */
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            fillMetadataIndex(packet, 0);
            fillMetadataIndex(packet, 1);
            fillMetadataIndex(packet, 2);
            fillMetadataIndex(packet, 3);
            fillMetadataIndex(packet, 4);
            fillMetadataIndex(packet, 5);
            fillMetadataIndex(packet, 6);
        };
    }

    /**
     * Send a {@link EntityMetaDataPacket} containing only the specified index
     * The index is wrote using {@link #fillMetadataIndex(BinaryWriter, int)}
     *
     * @param index the metadata index
     */
    protected void sendMetadataIndex(int index) {
        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.consumer = packet -> fillMetadataIndex(packet, index);

        sendPacketToViewersAndSelf(metaDataPacket);
    }

    /**
     * Used to fill/write a specific metadata index
     * The proper use to add a new metadata index is to override this and add your case
     * Then you can also override {@link #getMetadataConsumer()} and fill your newly added index
     *
     * @param packet the packet writer
     * @param index  the index to fill/write
     */
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        switch (index) {
            case 0:
                fillStateMetadata(packet);
                break;
            case 1:
                fillAirTickMetaData(packet);
                break;
            case 2:
                fillCustomNameMetaData(packet);
                break;
            case 3:
                fillCustomNameVisibleMetaData(packet);
                break;
            case 4:
                fillSilentMetaData(packet);
                break;
            case 5:
                fillNoGravityMetaData(packet);
                break;
            case 6:
                fillPoseMetaData(packet);
                break;
        }
    }

    private void fillStateMetadata(BinaryWriter packet) {
        packet.writeByte((byte) 0);
        packet.writeByte(METADATA_BYTE);
        byte index0 = 0;
        if (onFire)
            index0 += 1;
        if (crouched)
            index0 += 2;
        if (UNUSED_METADATA)
            index0 += 4;
        if (sprinting)
            index0 += 8;
        if (swimming)
            index0 += 16;
        if (invisible)
            index0 += 32;
        if (glowing)
            index0 += 64;
        if (usingElytra)
            index0 += 128;
        packet.writeByte(index0);
    }

    private void fillAirTickMetaData(BinaryWriter packet) {
        packet.writeByte((byte) 1);
        packet.writeByte(METADATA_VARINT);
        packet.writeVarInt(air);
    }

    private void fillCustomNameMetaData(BinaryWriter packet) {
        boolean hasCustomName = customName != null;

        packet.writeByte((byte) 2);
        packet.writeByte(METADATA_OPTCHAT);
        packet.writeBoolean(hasCustomName);
        if (hasCustomName) {
            packet.writeSizedString(customName.toString());
        }
    }

    private void fillCustomNameVisibleMetaData(BinaryWriter packet) {
        packet.writeByte((byte) 3);
        packet.writeByte(METADATA_BOOLEAN);
        packet.writeBoolean(customNameVisible);
    }

    private void fillSilentMetaData(BinaryWriter packet) {
        packet.writeByte((byte) 4);
        packet.writeByte(METADATA_BOOLEAN);
        packet.writeBoolean(silent);
    }

    private void fillNoGravityMetaData(BinaryWriter packet) {
        packet.writeByte((byte) 5);
        packet.writeByte(METADATA_BOOLEAN);
        packet.writeBoolean(noGravity);
    }

    private void fillPoseMetaData(BinaryWriter packet) {
        packet.writeByte((byte) 6);
        packet.writeByte(METADATA_POSE);
        packet.writeVarInt(pose.ordinal());
    }

    protected void sendSynchronization() {
        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.position = getPosition();
        entityTeleportPacket.onGround = isOnGround();
        sendPacketToViewers(entityTeleportPacket);
    }

    /**
     * Ask for a synchronization (position) to happen during next entity tick
     */
    public void askSynchronization() {
        this.lastSynchronizationTime = 0;
    }

    private boolean shouldUpdate(long time) {
        return (float) (time - lastUpdate) >= MinecraftServer.TICK_MS * 0.9f; // Margin of error
    }

    private enum Pose {
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
