package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
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
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.*;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class Entity implements Viewable, EventHandler, DataContainer {

    private static Map<Integer, Entity> entityById = new ConcurrentHashMap<>();
    private static AtomicInteger lastEntityId = new AtomicInteger();

    // Metadata
    protected static final byte METADATA_BYTE = 0;
    protected static final byte METADATA_VARINT = 1;
    protected static final byte METADATA_FLOAT = 2;
    protected static final byte METADATA_STRING = 3;
    protected static final byte METADATA_CHAT = 4;
    protected static final byte METADATA_OPTCHAT = 5;
    protected static final byte METADATA_SLOT = 6;
    protected static final byte METADATA_BOOLEAN = 7;
    protected static final byte METADATA_POSE = 18;

    protected Instance instance;
    protected Position position;
    protected float lastX, lastY, lastZ;
    protected float lastYaw, lastPitch;
    private int id;

    private BoundingBox boundingBox;

    protected Entity vehicle;
    // Velocity
    protected Vector velocity = new Vector(); // Movement in block per second
    protected float gravityDragPerTick;
    protected float eyeHeight;

    private boolean autoViewable;
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Data data;
    private Set<Entity> passengers = new CopyOnWriteArraySet<>();

    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean shouldRemove;
    private long scheduledRemoveTime;
    private int entityType;
    private long lastUpdate;
    private Map<Class<? extends Event>, List<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();
    protected long lastVelocityUpdateTime; // Reset velocity to 0 after countdown

    // Synchronization
    private long synchronizationDelay = 1500; // In ms
    private long lastSynchronizationTime;

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
    protected String customName = "";
    protected boolean customNameVisible;
    protected boolean silent;
    protected boolean noGravity;
    protected Pose pose = Pose.STANDING;


    private long velocityUpdatePeriod;
    protected boolean onGround;

    // Tick related
    private long ticks;
    private final EntityTickEvent tickEvent = new EntityTickEvent(this);

    public Entity(int entityType, Position spawnPosition) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = UUID.randomUUID();
        this.position = spawnPosition.clone();

        setBoundingBox(0, 0, 0);

        setAutoViewable(true);

        entityById.put(id, this);
        setVelocityUpdatePeriod(5);
    }

    public Entity(int entityType) {
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
     */
    public abstract void update();

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
     * @param time
     * @return
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

    public void teleport(Position position, Runnable callback) {
        Check.notNull(position, "Teleport position cannot be null");
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");

        Runnable runnable = () -> {
            refreshPosition(position.getX(), position.getY(), position.getZ());
            refreshView(position.getYaw(), position.getPitch());
            EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
            entityTeleportPacket.entityId = getEntityId();
            entityTeleportPacket.position = position;
            entityTeleportPacket.onGround = isOnGround();
            sendPacketToViewers(entityTeleportPacket);
        };

        if (instance.hasEnabledAutoChunkLoad()) {
            instance.loadChunk(position, chunk -> {
                runnable.run();
                if (callback != null)
                    callback.run();
            });
        } else {
            if (ChunkUtils.isChunkUnloaded(instance, position.getX(), position.getZ()))
                return;
            runnable.run();
            if (callback != null)
                callback.run();
        }
    }

    public void teleport(Position position) {
        teleport(position, null);
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
        player.viewableEntities.add(this);
        return result;
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

    public void tick(long time) {
        if (instance == null)
            return;

        if (scheduledRemoveTime != 0) { // Any entity with scheduled remove does not update
            boolean finished = time >= scheduledRemoveTime;
            if (finished) {
                remove();
            }
            return;
        }

        if (shouldRemove()) {
            remove();
            return;
        }

        boolean chunkUnloaded = ChunkUtils.isChunkUnloaded(instance, position.getX(), position.getZ());
        if (chunkUnloaded) {
            // No update for entities in unloaded chunk
            return;
        }

        if (shouldUpdate(time)) {
            this.lastUpdate = time;

            // Velocity
            if (!(this instanceof Player)) {
                final float tps = MinecraftServer.TICK_PER_SECOND;
                float newX = position.getX() + velocity.getX() / tps;
                float newY = position.getY() + velocity.getY() / tps;
                float newZ = position.getZ() + velocity.getZ() / tps;

                Position newPosition = new Position(newX, newY, newZ);

                chunkUnloaded = ChunkUtils.isChunkUnloaded(instance, newX, newZ);
                if (chunkUnloaded)
                    return;

                if (!(this instanceof Player) && !noGravity) { // players handle gravity by themselves
                    velocity.setY(velocity.getY() - gravityDragPerTick * tps);
                }

                Vector newVelocityOut = new Vector();
                Vector deltaPos = new Vector(
                        getVelocity().getX() / tps,
                        getVelocity().getY() / tps,
                        getVelocity().getZ() / tps
                );
                onGround = CollisionUtils.handlePhysics(this, deltaPos, newPosition, newVelocityOut);

                refreshPosition(newPosition);
                velocity.copy(newVelocityOut);
                velocity.multiply(tps);

                float drag;
                if (onGround) {
                    drag = 0.5f; // ground drag
                } else {
                    drag = 0.98f; // air drag
                }
                velocity.setX(velocity.getX() * drag);
                velocity.setZ(velocity.getZ() * drag);


                sendSynchronization();
                if (shouldSendVelocityUpdate(time)) {
                    sendPacketToViewers(getVelocityPacket());
                    lastVelocityUpdateTime = time;
                }
            }

            // handle block contacts
            int minX = (int) Math.floor(boundingBox.getMinX());
            int maxX = (int) Math.ceil(boundingBox.getMaxX());
            int minY = (int) Math.floor(boundingBox.getMinY());
            int maxY = (int) Math.ceil(boundingBox.getMaxY());
            int minZ = (int) Math.floor(boundingBox.getMinZ());
            int maxZ = (int) Math.ceil(boundingBox.getMaxZ());
            BlockPosition tmpPosition = new BlockPosition(0, 0, 0); // allow reuse
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        chunkUnloaded = ChunkUtils.isChunkUnloaded(instance, x, z);
                        if (chunkUnloaded)
                            continue;
                        CustomBlock customBlock = instance.getCustomBlock(x, y, z);
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
            update();

            ticks++;
            callEvent(EntityTickEvent.class, tickEvent); // reuse tickEvent to avoid recreating it each tick

            // Scheduled synchronization
            if (time - lastSynchronizationTime >= synchronizationDelay) {
                lastSynchronizationTime = time;
                sendSynchronization();
            }
        }

        if (shouldRemove()) {
            remove();
        }
    }

    /**
     * Returns the number of ticks this entity has been active for
     *
     * @return
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
    public <E extends Event> void addEventCallback(Class<E> eventClass, EventCallback<E> eventCallback) {
        Check.notNull(eventClass, "Event class cannot be null");
        Check.notNull(eventCallback, "Event callback cannot be null");
        List<EventCallback> callbacks = getEventCallbacks(eventClass);
        callbacks.add(eventCallback);
        this.eventCallbacks.put(eventClass, callbacks);
    }

    @Override
    public <E extends Event> List<EventCallback> getEventCallbacks(Class<E> eventClass) {
        Check.notNull(eventClass, "Event class cannot be null");
        return eventCallbacks.getOrDefault(eventClass, new CopyOnWriteArrayList<>());
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

    public int getEntityType() {
        return entityType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isActive() {
        return isActive;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(float x, float y, float z) {
        this.boundingBox = new BoundingBox(this, x, y, z);
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        Check.notNull(instance, "instance cannot be null!");
        Check.stateCondition(!MinecraftServer.getInstanceManager().getInstances().contains(instance),
                "Instances need to be registered with InstanceManager#createInstanceContainer or InstanceManager#createSharedInstance");

        if (this.instance != null) {
            this.instance.removeEntity(this);
        }

        this.isActive = true;
        this.instance = instance;
        instance.addEntity(this);
        spawn();
        EntitySpawnEvent entitySpawnEvent = new EntitySpawnEvent(instance);
        callEvent(EntitySpawnEvent.class, entitySpawnEvent);
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
        callCancellableEvent(EntityVelocityEvent.class, entityVelocityEvent, () -> {
            this.velocity.copy(entityVelocityEvent.getVelocity());
            sendPacketToViewers(getVelocityPacket());
        });
    }

    public void setGravity(float gravityDragPerTick) {
        this.gravityDragPerTick = gravityDragPerTick;
    }

    public float getDistance(Entity entity) {
        Check.notNull(entity, "Entity cannot be null");
        return getPosition().getDistance(entity.getPosition());
    }

    public Entity getVehicle() {
        return vehicle;
    }

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

    public void removePassenger(Entity entity) {
        Check.notNull(entity, "Passenger cannot be null");
        Check.stateCondition(instance == null, "You need to set an instance using Entity#setInstance");

        if (!passengers.remove(entity))
            return;
        entity.vehicle = null;
        sendPacketToViewersAndSelf(getPassengersPacket());
    }

    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    /**
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

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        sendMetadataIndex(0);
    }

    /**
     * @return true if the entity is glowing, false otherwise
     */
    public boolean isGlowing() {
        return glowing;
    }

    /**
     * @param noGravity should the entity ignore gravity
     */
    public void setNoGravity(boolean noGravity) {
        this.noGravity = noGravity;
        sendMetadataIndex(5);
    }

    /**
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

        if (hasPassenger()) {
            for (Entity passenger : getPassengers()) {
                passenger.refreshPosition(x, y, z);
            }
        }

        Instance instance = getInstance();
        if (instance != null) {
            Chunk lastChunk = instance.getChunkAt(lastX, lastZ);
            Chunk newChunk = instance.getChunkAt(x, z);
            if (lastChunk != null && newChunk != null && lastChunk != newChunk) {
                synchronized (instance) {
                    instance.removeEntityFromChunk(this, lastChunk);
                    instance.addEntityToChunk(this, newChunk);
                }
                updateView(lastChunk, newChunk);
            }
        }
    }

    public void refreshPosition(Position position) {
        refreshPosition(position.getX(), position.getY(), position.getZ());
    }

    private void updateView(Chunk lastChunk, Chunk newChunk) {
        boolean isPlayer = this instanceof Player;

        if (isPlayer)
            ((Player) this).onChunkChange(lastChunk, newChunk); // Refresh loaded chunk

        // Refresh entity viewable list
        long[] lastVisibleChunksEntity = ChunkUtils.getChunksInRange(new Position(16 * lastChunk.getChunkX(), 0, 16 * lastChunk.getChunkZ()), MinecraftServer.ENTITY_VIEW_DISTANCE);
        long[] updatedVisibleChunksEntity = ChunkUtils.getChunksInRange(new Position(16 * newChunk.getChunkX(), 0, 16 * newChunk.getChunkZ()), MinecraftServer.ENTITY_VIEW_DISTANCE);

        int[] oldChunksEntity = ArrayUtils.getDifferencesBetweenArray(lastVisibleChunksEntity, updatedVisibleChunksEntity);
        for (int index : oldChunksEntity) {
            int[] chunkPos = ChunkUtils.getChunkCoord(lastVisibleChunksEntity[index]);
            Chunk chunk = instance.getChunk(chunkPos[0], chunkPos[1]);
            if (chunk == null)
                continue;
            instance.getChunkEntities(chunk).forEach(ent -> {
                if (ent instanceof Player) {
                    Player player = (Player) ent;
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

        int[] newChunksEntity = ArrayUtils.getDifferencesBetweenArray(updatedVisibleChunksEntity, lastVisibleChunksEntity);
        for (int index : newChunksEntity) {
            int[] chunkPos = ChunkUtils.getChunkCoord(updatedVisibleChunksEntity[index]);
            Chunk chunk = instance.getChunk(chunkPos[0], chunkPos[1]);
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

    public void refreshView(float yaw, float pitch) {
        this.lastYaw = position.getYaw();
        this.lastPitch = position.getPitch();
        position.setYaw(yaw);
        position.setPitch(pitch);
    }

    /**
     * Ask for a synchronization (position) to happen during next entity update
     */
    public void askSynchronization() {
        this.lastSynchronizationTime = 0;
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
     * @return the current position of the entity
     */
    public Position getPosition() {
        return position;
    }

    public float getEyeHeight() {
        return eyeHeight;
    }

    public void setEyeHeight(float eyeHeight) {
        this.eyeHeight = eyeHeight;
    }

    /**
     * @param position the checked position chunk
     * @return true if the entity is in the same chunk as {@code position}
     */
    public boolean sameChunk(Position position) {
        Check.notNull(position, "Position cannot be null");
        Position pos = getPosition();
        int chunkX1 = ChunkUtils.getChunkCoordinate((int) Math.floor(pos.getX()));
        int chunkZ1 = ChunkUtils.getChunkCoordinate((int) Math.floor(pos.getZ()));

        int chunkX2 = ChunkUtils.getChunkCoordinate((int) Math.floor(position.getX()));
        int chunkZ2 = ChunkUtils.getChunkCoordinate((int) Math.floor(position.getZ()));

        return chunkX1 == chunkX2 && chunkZ1 == chunkZ2;
    }

    public boolean sameChunk(Entity entity) {
        return sameChunk(entity.getPosition());
    }

    /**
     * Remove the entity from the server immediately
     * WARNING: this do not trigger the {@link EntityDeathEvent} event
     */
    public void remove() {
        this.shouldRemove = true;
        entityById.remove(id);
        if (instance != null)
            instance.removeEntity(this);
    }

    /**
     * Trigger {@link #remove()} after the specified time
     *
     * @param delay
     * @param timeUnit to determine the delay unit
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
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            fillMetadataIndex(packet, 0);
            fillMetadataIndex(packet, 1);
            fillMetadataIndex(packet, 5);
            fillMetadataIndex(packet, 6);
        };
    }

    /**
     * Send a {@link EntityMetaDataPacket} containing only the specified index
     * The index is wrote using {@link #fillMetadataIndex(PacketWriter, int)}
     *
     * @param index
     */
    protected void sendMetadataIndex(int index) {
        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.consumer = packet -> {
            fillMetadataIndex(packet, index);
        };

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
    protected void fillMetadataIndex(PacketWriter packet, int index) {
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
            case 5:
                fillNoGravityMetaData(packet);
                break;
            case 6:
                fillPoseMetaData(packet);
                break;
        }
    }

    private void fillStateMetadata(PacketWriter packet) {
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

    private void fillAirTickMetaData(PacketWriter packet) {
        packet.writeByte((byte) 1);
        packet.writeByte(METADATA_VARINT);
        packet.writeVarInt(air);
    }

    private void fillCustomNameMetaData(PacketWriter packet) {
        packet.writeByte((byte) 2);
        packet.writeByte(METADATA_CHAT);
        packet.writeSizedString(customName);
    }

    private void fillNoGravityMetaData(PacketWriter packet) {
        packet.writeByte((byte) 5);
        packet.writeByte(METADATA_BOOLEAN);
        packet.writeBoolean(noGravity);
    }

    private void fillPoseMetaData(PacketWriter packet) {
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

        if (!passengers.isEmpty())
            sendPacketToViewers(getPassengersPacket());
    }

    private boolean shouldUpdate(long time) {
        return (float) (time - lastUpdate) >= MinecraftServer.TICK_MS * 0.9f; // Margin of error
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
