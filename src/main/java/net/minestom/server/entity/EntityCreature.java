package net.minestom.server.entity;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.model.PathObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.pathfinding.PFPathingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.item.ItemStackUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;

public abstract class EntityCreature extends LivingEntity {

    private PFPathingEntity pathingEntity = new PFPathingEntity(this);
    private HydrazinePathFinder pathFinder;
    private PathObject path;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;


    public EntityCreature(EntityType entityType, Position spawnPosition) {
        super(entityType, spawnPosition);

        this.mainHandItem = ItemStack.getAirItem();
        this.offHandItem = ItemStack.getAirItem();

        this.helmet = ItemStack.getAirItem();
        this.chestplate = ItemStack.getAirItem();
        this.leggings = ItemStack.getAirItem();
        this.boots = ItemStack.getAirItem();

        heal();
    }

    @Override
    public void update(long time) {
        // Path finding
        path = pathFinder.update();
        if (path != null) {
            path.update(pathingEntity);
            if (path.done()) {
                pathFinder.reset();
            } else {
                final float speed = getAttributeValue(Attribute.MOVEMENT_SPEED);
                final Position targetPosition = pathingEntity.getTargetPosition();
                moveTowards(targetPosition, speed);
            }
        } else {
            // TODO not call this every tick (code above with #done() is never called)
            pathFinder.reset();
        }

        super.update(time);
    }

    @Override
    public void setInstance(Instance instance) {
        super.setInstance(instance);
        this.pathFinder = new HydrazinePathFinder(pathingEntity, instance.getInstanceSpace());
    }

    /**
     * @param x          X movement offset
     * @param y          Y movement offset
     * @param z          Z movement offset
     * @param updateView should the entity move its head toward the position?
     */
    public void move(float x, float y, float z, boolean updateView) {
        // TODO: remove ? Entity#tick already performs this behaviour, and syncs it properly
        final Position position = getPosition();
        Position newPosition = new Position();
        // Calculate collisions boxes
        onGround = CollisionUtils.handlePhysics(this, new Vector(x, y, z), newPosition, new Vector());
        // Refresh target position
        final float newX = newPosition.getX();
        final float newY = newPosition.getY();
        final float newZ = newPosition.getZ();

        // Creatures cannot move in unloaded chunk
        if (ChunkUtils.isChunkUnloaded(getInstance(), newX, newZ))
            return;

        final float lastYaw = position.getYaw();
        final float radians = (float) Math.atan2(newZ - position.getZ(), newX - position.getX());

        final float yaw = (float) (radians * (180.0 / Math.PI)) - 90;
        final float pitch = position.getPitch(); // TODO

        final short deltaX = (short) ((newX * 32 - position.getX() * 32) * 128);
        final short deltaY = (short) ((newY * 32 - position.getY() * 32) * 128);
        final short deltaZ = (short) ((newZ * 32 - position.getZ() * 32) * 128);

        if (updateView) {
            EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
            entityPositionAndRotationPacket.entityId = getEntityId();
            entityPositionAndRotationPacket.deltaX = deltaX;
            entityPositionAndRotationPacket.deltaY = deltaY;
            entityPositionAndRotationPacket.deltaZ = deltaZ;
            entityPositionAndRotationPacket.yaw = yaw;
            entityPositionAndRotationPacket.pitch = pitch;
            entityPositionAndRotationPacket.onGround = isOnGround();
            sendPacketToViewers(entityPositionAndRotationPacket);
        } else {
            EntityPositionPacket entityPositionPacket = new EntityPositionPacket();
            entityPositionPacket.entityId = getEntityId();
            entityPositionPacket.deltaX = deltaX;
            entityPositionPacket.deltaY = deltaY;
            entityPositionPacket.deltaZ = deltaZ;
            entityPositionPacket.onGround = isOnGround();
            sendPacketToViewers(entityPositionPacket);
        }

        if (lastYaw != yaw) {
            setView(yaw, pitch);
        }

        refreshPosition(newX, newY, newZ);
    }

    @Override
    public void spawn() {

    }

    @Override
    public void kill() {
        super.kill();

        // Needed for proper death animation (wait for it to finish before destroying the entity)
        scheduleRemove(1000, TimeUnit.MILLISECOND);
    }

    @Override
    public boolean addViewer(Player player) {
        final boolean result = super.addViewer(player);
        if (!result)
            return false;

        PlayerConnection playerConnection = player.getPlayerConnection();

        EntityPacket entityPacket = new EntityPacket();
        entityPacket.entityId = getEntityId();

        SpawnLivingEntityPacket spawnLivingEntityPacket = new SpawnLivingEntityPacket();
        spawnLivingEntityPacket.entityId = getEntityId();
        spawnLivingEntityPacket.entityUuid = getUuid();
        spawnLivingEntityPacket.entityType = getEntityType();
        spawnLivingEntityPacket.position = getPosition();
        spawnLivingEntityPacket.headPitch = 0;

        playerConnection.sendPacket(entityPacket);
        playerConnection.sendPacket(spawnLivingEntityPacket);
        playerConnection.sendPacket(getVelocityPacket());
        playerConnection.sendPacket(getMetadataPacket());

        // Equipments synchronization
        syncEquipments(playerConnection);

        if (hasPassenger()) {
            playerConnection.sendPacket(getPassengersPacket());
        }

        return result;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        this.mainHandItem = ItemStackUtils.notNull(itemStack);
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        this.offHandItem = ItemStackUtils.notNull(itemStack);
        syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
        syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
        syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
        syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
        syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
    }

    /**
     * Call a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(Entity target, boolean swingHand) {
        if (swingHand)
            swingMainHand();
        EntityAttackEvent attackEvent = new EntityAttackEvent(this, target);
        callEvent(EntityAttackEvent.class, attackEvent);
    }

    /**
     * Call a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     * <p>
     * This does not trigger the hand animation
     *
     * @param target the entity target
     */
    public void attack(Entity target) {
        attack(target, false);
    }

    public void jump(float height) {
        // FIXME magic value
        final Vector velocity = new Vector(0, height * 2.5f, 0);
        setVelocity(velocity);
    }

    /**
     * Retrieve the path to {@code position} and ask the entity to follow the path
     * <p>
     * Can be set to null to reset the pathfinder
     * <p>
     * The position is cloned, if you want the entity to continually follow this position object
     * you need to call this when you want the path to update
     *
     * @param position the position to find the path to, null to reset the pathfinder
     */
    public void setPathTo(Position position) {
        this.pathFinder.reset();
        if (position == null) {
            return;
        }
        position = position.clone();
        this.path = pathFinder.initiatePathTo(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     */
    public void moveTowards(Position direction, float speed) {
        Check.notNull(direction, "The direction cannot be null");
        final float currentX = position.getX();
        final float currentZ = position.getZ();
        final float targetX = direction.getX();
        final float targetZ = direction.getZ();
        final float dz = targetZ - currentZ;
        final float dx = targetX - currentX;

        // the purpose of these few lines is to slow down entities when they reach their destination
        float distSquared = dx * dx + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }

        final float radians = (float) Math.atan2(dz, dx);
        final float speedX = (float) (Math.cos(radians) * speed);
        final float speedZ = (float) (Math.sin(radians) * speed);

        lookAlong(dx, direction.getY(), dz);

        // TODO: is a hard set an issue if there are other external forces at play?
        final float tps = MinecraftServer.TICK_PER_SECOND;
        velocity.setX(speedX * tps);
        velocity.setZ(speedZ * tps);
    }

    /**
     * Get the pathing entity
     * <p>
     * Used by the pathfinder
     *
     * @return the pathing entity
     */
    public PFPathingEntity getPathingEntity() {
        return pathingEntity;
    }

    private void lookAlong(float dx, float dy, float dz) {
        final float horizontalAngle = (float) Math.atan2(dz, dx);
        final float yaw = (float) (horizontalAngle * (180.0 / Math.PI)) - 90;
        final float pitch = (float) Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)));

        getPosition().setYaw(yaw);
        getPosition().setPitch(pitch);
    }

    private ItemStack getEquipmentItem(ItemStack itemStack, ArmorEquipEvent.ArmorSlot armorSlot) {
        itemStack = ItemStackUtils.notNull(itemStack);

        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(this, itemStack, armorSlot);
        callEvent(ArmorEquipEvent.class, armorEquipEvent);
        return armorEquipEvent.getArmorItem();
    }
}
