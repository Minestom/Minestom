package net.minestom.server.entity;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.model.IPath;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.pathfinding.PFPathingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityPacket;
import net.minestom.server.network.packet.server.play.SpawnLivingEntityPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.item.ItemStackUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class EntityCreature extends LivingEntity {

    private final PFPathingEntity pathingEntity = new PFPathingEntity(this);
    private HydrazinePathFinder pathFinder;
    private IPath path;
    private Position pathPosition;

    protected List<GoalSelector> goalSelectors = new ArrayList<>();
    protected List<TargetSelector> targetSelectors = new ArrayList<>();
    private GoalSelector currentGoalSelector;

    private Entity target;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    private final ReentrantLock pathLock = new ReentrantLock();


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

        if (getInstance() == null) {
            return;
        }

        // Goal selectors
        {
            // Supplier used to get the next goal selector which should start
            // (null if not found)
            final Supplier<GoalSelector> goalSelectorSupplier = () -> {
                for (GoalSelector goalSelector : goalSelectors) {
                    final boolean start = goalSelector.shouldStart();
                    if (start) {
                        return goalSelector;
                    }
                }
                return null;
            };

            // true if the goal selector changed this tick
            boolean newGoalSelector = false;

            if (currentGoalSelector == null) {
                // No goal selector, get a new one
                this.currentGoalSelector = goalSelectorSupplier.get();
                newGoalSelector = currentGoalSelector != null;
            } else {
                final boolean stop = currentGoalSelector.shouldEnd();
                if (stop) {
                    // The current goal selector stopped, find a new one
                    this.currentGoalSelector.end();
                    this.currentGoalSelector = goalSelectorSupplier.get();
                    newGoalSelector = currentGoalSelector != null;
                }
            }

            // Start the new goal selector
            if (newGoalSelector) {
                this.currentGoalSelector.start();
            }

            // Execute tick for the current goal selector
            if (currentGoalSelector != null) {
                currentGoalSelector.tick(time);
            }
        }


        // Path finding
        {
            if (pathPosition != null) {
                this.pathLock.lock();
                this.path = pathFinder.updatePathFor(pathingEntity);

                if (path != null) {
                    final float speed = getAttributeValue(Attribute.MOVEMENT_SPEED);
                    final Position targetPosition = pathingEntity.getTargetPosition();
                    moveTowards(targetPosition, speed);
                } else {
                    if (pathPosition != null) {
                        this.pathPosition = null;
                        this.pathFinder.reset();
                    }
                }

                this.pathLock.unlock();
            }
        }

        super.update(time);
    }

    @Override
    public void setInstance(Instance instance) {
        super.setInstance(instance);
        this.pathFinder = new HydrazinePathFinder(pathingEntity, instance.getInstanceSpace());
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

        final PlayerConnection playerConnection = player.getPlayerConnection();

        EntityPacket entityPacket = new EntityPacket();
        entityPacket.entityId = getEntityId();

        SpawnLivingEntityPacket spawnLivingEntityPacket = new SpawnLivingEntityPacket();
        spawnLivingEntityPacket.entityId = getEntityId();
        spawnLivingEntityPacket.entityUuid = getUuid();
        spawnLivingEntityPacket.entityType = getEntityType().getId();
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

    /**
     * Get the goal selectors of this entity
     *
     * @return a modifiable list containing the entity goal selectors
     */
    public List<GoalSelector> getGoalSelectors() {
        return goalSelectors;
    }

    /**
     * Get the target selectors of this entity
     *
     * @return a modifiable list containing the entity target selectors
     */
    public List<TargetSelector> getTargetSelectors() {
        return targetSelectors;
    }

    /**
     * Get the entity target
     *
     * @return the entity target
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * Change the entity target
     *
     * @param target the new entity target
     */
    public void setTarget(Entity target) {
        this.target = target;
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
     * @return true if a path has been found
     */
    public boolean setPathTo(Position position) {
        if (position != null && getPathPosition() != null && position.isSimilar(getPathPosition())) {
            // Tried to set path to the same target position
            return false;
        }

        if (pathFinder == null) {
            // Unexpected error
            return false;
        }

        this.pathLock.lock();

        this.pathFinder.reset();
        if (position == null) {
            this.pathLock.unlock();
            return false;
        }

        // Can't path outside of the world border
        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.isInside(position)) {
            this.pathLock.unlock();
            return false;
        }

        // Can't path in an unloaded chunk
        final Chunk chunk = instance.getChunkAt(position);
        if (!ChunkUtils.isLoaded(chunk)) {
            this.pathLock.unlock();
            return false;
        }

        final Position targetPosition = position.clone();

        this.path = pathFinder.initiatePathTo(position.getX(), position.getY(), position.getZ());
        this.pathLock.unlock();
        final boolean success = path != null;
        this.pathPosition = success ? targetPosition : null;

        return success;
    }

    /**
     * Get the target pathfinder position
     *
     * @return the target pathfinder position, null if there is no one
     */
    public Position getPathPosition() {
        return pathPosition;
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
