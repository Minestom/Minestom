package net.minestom.server.entity;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.model.IPath;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.PFPathingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityMovementPacket;
import net.minestom.server.network.packet.server.play.SpawnLivingEntityPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class EntityCreature extends LivingEntity implements NavigableEntity {

    // TODO all pathfinding requests should be process in another thread
    private final Object pathLock = new Object();

    private final PFPathingEntity pathingEntity = new PFPathingEntity(this);
    private HydrazinePathFinder pathFinder;
    private IPath path;
    private Position pathPosition;

    protected final List<GoalSelector> goalSelectors = new ArrayList<>();
    protected final List<TargetSelector> targetSelectors = new ArrayList<>();
    private GoalSelector currentGoalSelector;

    private Entity target;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public EntityCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);

        this.mainHandItem = ItemStack.getAirItem();
        this.offHandItem = ItemStack.getAirItem();

        this.helmet = ItemStack.getAirItem();
        this.chestplate = ItemStack.getAirItem();
        this.leggings = ItemStack.getAirItem();
        this.boots = ItemStack.getAirItem();

        heal();
    }

    public EntityCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        this(entityType, spawnPosition);

        if (instance != null) {
            setInstance(instance);
        }
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
        pathFindingTick(getAttributeValue(Attributes.MOVEMENT_SPEED));

        super.update(time);
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
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
    public boolean addViewer(@NotNull Player player) {
        final boolean result = super.addViewer(player);

        final PlayerConnection playerConnection = player.getPlayerConnection();

        EntityMovementPacket entityMovementPacket = new EntityMovementPacket();
        entityMovementPacket.entityId = getEntityId();

        SpawnLivingEntityPacket spawnLivingEntityPacket = new SpawnLivingEntityPacket();
        spawnLivingEntityPacket.entityId = getEntityId();
        spawnLivingEntityPacket.entityUuid = getUuid();
        spawnLivingEntityPacket.entityType = getEntityType().getId();
        spawnLivingEntityPacket.position = getPosition();
        spawnLivingEntityPacket.headPitch = 0;

        playerConnection.sendPacket(entityMovementPacket);
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
     * Gets the goal selectors of this entity.
     *
     * @return a modifiable list containing the entity goal selectors
     */
    @NotNull
    public List<GoalSelector> getGoalSelectors() {
        return goalSelectors;
    }

    /**
     * Gets the target selectors of this entity.
     *
     * @return a modifiable list containing the entity target selectors
     */
    @NotNull
    public List<TargetSelector> getTargetSelectors() {
        return targetSelectors;
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target
     */
    public void setTarget(@NotNull Entity target) {
        this.target = target;
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        this.mainHandItem = itemStack;
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        this.offHandItem = itemStack;
        syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
        syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
        syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
        syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
        syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(@NotNull Entity target, boolean swingHand) {
        if (swingHand)
            swingMainHand();
        EntityAttackEvent attackEvent = new EntityAttackEvent(this, target);
        callEvent(EntityAttackEvent.class, attackEvent);
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     * <p>
     * This does not trigger the hand animation.
     *
     * @param target the entity target
     */
    public void attack(@NotNull Entity target) {
        attack(target, false);
    }

    @Override
    public void pathFindingTick(float speed) {
        synchronized (pathLock){
            NavigableEntity.super.pathFindingTick(speed);
        }
    }

    @Override
    public boolean setPathTo(@Nullable Position position) {
        synchronized (pathLock){
            return NavigableEntity.super.setPathTo(position);
        }
    }

    @Nullable
    @Override
    public Position getPathPosition() {
        return pathPosition;
    }

    @Override
    public void setPathPosition(Position pathPosition) {
        this.pathPosition = pathPosition;
    }

    @Nullable
    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public void setPath(IPath path) {
        this.path = path;
    }

    @NotNull
    @Override
    public PFPathingEntity getPathingEntity() {
        return pathingEntity;
    }

    @Nullable
    @Override
    public HydrazinePathFinder getPathFinder() {
        return pathFinder;
    }

    @NotNull
    @Override
    public Entity getNavigableEntity() {
        return this;
    }

    private ItemStack getEquipmentItem(@NotNull ItemStack itemStack, @NotNull ArmorEquipEvent.ArmorSlot armorSlot) {
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(this, itemStack, armorSlot);
        callEvent(ArmorEquipEvent.class, armorEquipEvent);
        return armorEquipEvent.getArmorItem();
    }
}
