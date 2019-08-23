package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.Main;
import fr.themode.minestom.Viewable;
import fr.themode.minestom.event.Callback;
import fr.themode.minestom.event.CancellableEvent;
import fr.themode.minestom.event.Event;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.net.packet.server.play.*;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity implements Viewable {

    private static Map<Integer, Entity> entityById = new HashMap<>();
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

    protected Instance instance;
    protected Position position;
    protected double lastX, lastY, lastZ;
    protected float lastYaw, lastPitch;
    private int id;

    protected Entity vehicle;
    private Map<Class<Event>, Callback> eventCallbacks = new ConcurrentHashMap<>();
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private Set<Entity> passengers = new CopyOnWriteArraySet<>();

    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean shouldRemove;
    private long scheduledRemoveTime;
    private int entityType;
    private long lastUpdate;

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

    public Entity(int entityType) {
        this.id = generateId();
        this.entityType = entityType;
        this.uuid = UUID.randomUUID();
        this.position = new Position();

        synchronized (entityById) {
            entityById.put(id, this);
        }
    }

    public static Entity getEntity(int id) {
        synchronized (entityById) {
            return entityById.get(id);
        }
    }

    private static int generateId() {
        return lastEntityId.incrementAndGet();
    }

    public abstract void update();

    public void teleport(Position position) {
        if (isChunkUnloaded(position.getX(), position.getZ()))
            return;

        refreshPosition(position.getX(), position.getY(), position.getZ());
        refreshView(position.getYaw(), position.getPitch());
        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.position = position;
        entityTeleportPacket.onGround = true;
        sendPacketToViewers(entityTeleportPacket);
    }

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
    }

    @Override
    public void removeViewer(Player player) {
        synchronized (viewers) {
            if (!viewers.contains(player))
                return;
            this.viewers.remove(player);
            DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket();
            destroyEntitiesPacket.entityIds = new int[]{getEntityId()};
            player.getPlayerConnection().sendPacket(destroyEntitiesPacket);
        }
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public void tick() {
        if (scheduledRemoveTime != 0) { // Any entity with scheduled remove does not update
            boolean finished = System.currentTimeMillis() >= scheduledRemoveTime;
            if (finished) {
                remove();
            }
            return;
        }
        if (shouldUpdate()) {
            update();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    public <E extends Event> void setEventCallback(Class<E> eventClass, Callback<E> callback) {
        this.eventCallbacks.put((Class<Event>) eventClass, callback);
    }

    public <E extends Event> Callback<E> getEventCallback(Class<E> eventClass) {
        return this.eventCallbacks.getOrDefault(eventClass, null);
    }

    public <E extends Event> void callEvent(Class<E> eventClass, E event) {
        Callback<E> callback = getEventCallback(eventClass);
        if (callback != null)
            getEventCallback(eventClass).run(event);
    }

    public <E extends CancellableEvent> void callCancellableEvent(Class<E> eventClass, E event, Runnable runnable) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            runnable.run();
        }
    }

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

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        if (instance == null)
            throw new IllegalArgumentException("instance cannot be null!");

        if (this.instance != null) {
            this.instance.removeEntity(this);
        }

        this.isActive = true;
        this.instance = instance;
        instance.addEntity(this);
    }

    public float getDistance(Entity entity) {
        return getPosition().getDistance(entity.getPosition());
    }

    public void addPassenger(Entity entity) {
        // TODO if entity already has a vehicle, leave it before?
        this.passengers.add(entity);
        entity.vehicle = this;
        if (instance != null) {
            SetPassengersPacket passengersPacket = new SetPassengersPacket();
            passengersPacket.vehicleEntityId = getEntityId();
            passengersPacket.passengersId = new int[]{entity.getEntityId()}; // TODO all passengers not only the new
            sendPacketToViewers(passengersPacket);
        }
    }

    public boolean hasPassenger() {
        return !passengers.isEmpty();
    }

    public Set<Entity> getPassengers() {
        return Collections.unmodifiableSet(passengers);
    }

    public void triggerStatus(byte status) {
        EntityStatusPacket statusPacket = new EntityStatusPacket();
        statusPacket.entityId = getEntityId();
        statusPacket.status = status;
        sendPacketToViewers(statusPacket);
    }

    public void setOnFire(boolean fire) {
        this.onFire = fire;
        sendMetadataIndex(0);
    }

    public void setNoGravity(boolean noGravity) {
        this.noGravity = noGravity;
        sendMetadataIndex(5);
    }

    public boolean isChunkUnloaded(float x, float z) {
        return getInstance().getChunk((int) Math.floor(x / 16), (int) Math.floor(z / 16)) == null;
    }

    public void refreshPosition(float x, float y, float z) {
        this.lastX = position.getX();
        this.lastY = position.getY();
        this.lastZ = position.getZ();
        position.setX(x);
        position.setY(y);
        position.setZ(z);

        Instance instance = getInstance();
        if (instance != null) {
            Chunk lastChunk = instance.getChunkAt(lastX, lastZ);
            Chunk newChunk = instance.getChunkAt(x, z);
            if (lastChunk != null && newChunk != null && lastChunk != newChunk) {
                synchronized (lastChunk) {
                    synchronized (newChunk) {
                        lastChunk.removeEntity(this);
                        newChunk.addEntity(this);
                    }
                }
            }
        }
    }

    public void refreshView(float yaw, float pitch) {
        this.lastYaw = position.getYaw();
        this.lastPitch = position.getPitch();
        position.setYaw(yaw);
        position.setPitch(pitch);
    }

    public void refreshSneaking(boolean sneaking) {
        this.crouched = sneaking;
        sendMetadataIndex(0);
    }

    public void refreshSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        sendMetadataIndex(0);
    }

    public Position getPosition() {
        return position;
    }

    public void remove() {
        this.shouldRemove = true;
        synchronized (entityById) {
            entityById.remove(id);
        }
    }

    public void scheduleRemove(long delay) {
        if (delay == 0) { // Cancel the scheduled remove
            this.scheduledRemoveTime = 0;
            return;
        }
        this.scheduledRemoveTime = System.currentTimeMillis() + delay;
    }

    public EntityMetaDataPacket getMetadataPacket() {
        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.data = getMetadataBuffer();
        return metaDataPacket;
    }

    public Buffer getMetadataBuffer() {
        Buffer buffer = Buffer.create();
        fillMetadataIndex(buffer, 0);
        fillMetadataIndex(buffer, 1);
        fillMetadataIndex(buffer, 5);
        return buffer;
    }

    protected void sendMetadataIndex(int index) {
        Buffer buffer = Buffer.create();
        fillMetadataIndex(buffer, index);

        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = getEntityId();
        metaDataPacket.data = buffer;
        sendPacketToViewers(metaDataPacket);
        if (this instanceof Player) {
            ((Player) this).getPlayerConnection().sendPacket(metaDataPacket);
        }
    }

    private void fillMetadataIndex(Buffer buffer, int index) {
        switch (index) {
            case 0:
                fillStateMetadata(buffer);
                break;
            case 1:
                fillAirTickMetaData(buffer);
                break;
            case 2:
                fillCustomNameMetaData(buffer);
                break;
            case 5:
                fillNoGravityMetaData(buffer);
                break;
        }
    }

    private void fillStateMetadata(Buffer buffer) {
        buffer.putByte((byte) 0);
        buffer.putByte(METADATA_BYTE);
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
        buffer.putByte(index0);
    }

    private void fillAirTickMetaData(Buffer buffer) {
        buffer.putByte((byte) 1);
        buffer.putByte(METADATA_VARINT);
        Utils.writeVarInt(buffer, air);
    }

    private void fillCustomNameMetaData(Buffer buffer) {
        buffer.putByte((byte) 2);
        buffer.putByte(METADATA_CHAT);
        Utils.writeString(buffer, customName);
    }

    private void fillNoGravityMetaData(Buffer buffer) {
        buffer.putByte((byte) 5);
        buffer.putByte(METADATA_BOOLEAN);
        buffer.putBoolean(noGravity);
    }

    private boolean shouldUpdate() {
        return (float) (System.currentTimeMillis() - lastUpdate) >= Main.TICK_MS * 0.9f; // Margin of error
    }

    public enum Pose {
        STANDING,
        FALL_FLYING,
        SLEEPING,
        SWIMMING,
        SPIN_ATTACK,
        SNEAKING,
        DYING;
    }

    protected boolean shouldRemove() {
        return shouldRemove;
    }
}
