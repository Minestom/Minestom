package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireExtinguishEvent;
import net.minestom.server.event.entity.EntitySetFireEvent;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LivingEntity extends Entity implements EquipmentHandler {

    private static final AttributeModifier SPRINTING_SPEED_MODIFIER = new AttributeModifier(NamespaceID.from("minecraft:sprinting"), 0.3, AttributeOperation.MULTIPLY_TOTAL);

    /**
     * IDs of modifiers that are protected from removal by methods like {@link AttributeInstance#clearModifiers()}.
     */
    @ApiStatus.Internal
    public static final Set<NamespaceID> PROTECTED_MODIFIERS = Set.of(SPRINTING_SPEED_MODIFIER.id());

    // ItemStack pickup
    protected boolean canPickupItem;
    protected Cooldown itemPickupCooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

    protected boolean isDead;

    protected Damage lastDamage;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    private final Map<String, AttributeInstance> attributeModifiers = new ConcurrentHashMap<>();
    private final Collection<AttributeInstance> unmodifiableModifiers =
            Collections.unmodifiableCollection(attributeModifiers.values());

    // Abilities
    protected boolean invulnerable;

    /**
     * Ticks until this entity must be extinguished
     */
    private int remainingFireTicks;

    private Team team;

    private int arrowCount;
    private float health = 1F;

    // Equipments
    private ItemStack mainHandItem = ItemStack.AIR;
    private ItemStack offHandItem = ItemStack.AIR;

    private ItemStack helmet = ItemStack.AIR;
    private ItemStack chestplate = ItemStack.AIR;
    private ItemStack leggings = ItemStack.AIR;
    private ItemStack boots = ItemStack.AIR;
    private ItemStack bodyEquipment = ItemStack.AIR;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public LivingEntity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
    }

    public LivingEntity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);

        // We must set the sprinting attribute serverside because when we resend modifiers it overwrites what
        // the client has, meaning if they are sprinting and we send no modifiers, they will no longer be
        // getting the speed boost of sprinting.
        final AttributeInstance speed = getAttribute(Attribute.MOVEMENT_SPEED);
        if (sprinting) speed.addModifier(SPRINTING_SPEED_MODIFIER);
        else speed.removeModifier(SPRINTING_SPEED_MODIFIER);
    }

    @Override
    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> mainHandItem;
            case OFF_HAND -> offHandItem;
            case BOOTS -> boots;
            case LEGGINGS -> leggings;
            case CHESTPLATE -> chestplate;
            case HELMET -> helmet;
            case BODY -> bodyEquipment;
        };
    }

    @Override
    public void setEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack) {
        ItemStack oldItem = getEquipment(slot);
        ItemStack newItem = slotChangeEvent(itemStack, slot);

        switch (slot) {
            case MAIN_HAND -> mainHandItem = newItem;
            case OFF_HAND -> offHandItem = newItem;
            case BOOTS -> boots = newItem;
            case LEGGINGS -> leggings = newItem;
            case CHESTPLATE -> chestplate = newItem;
            case HELMET -> helmet = newItem;
            case BODY -> bodyEquipment = newItem;
        }

        syncEquipment(slot);
        updateEquipmentAttributes(oldItem, newItem, slot);
    }

    private ItemStack slotChangeEvent(@NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
        EntityEquipEvent entityEquipEvent = new EntityEquipEvent(this, itemStack, slot);
        EventDispatcher.call(entityEquipEvent);
        return entityEquipEvent.getEquippedItem();
    }


    /**
     * Updates the current attributes of the living entity based on
     *
     * @param oldItemStack The ItemStack that has been removed, modifiers on this stack will be removed from the entity
     * @param newItemStack The ItemStack that has been added, modifiers on this stack will be added to the entity
     * @param slot         The slot that changed, this will determine what modifiers are actually changed
     */
    @ApiStatus.Internal
    public void updateEquipmentAttributes(@NotNull ItemStack oldItemStack, @NotNull ItemStack newItemStack, @NotNull EquipmentSlot slot) {
        AttributeList oldAttributes = oldItemStack.get(ItemComponent.ATTRIBUTE_MODIFIERS);
        // Remove old attributes
        if (oldAttributes != null) {
            for (AttributeList.Modifier modifier : oldAttributes.modifiers()) {
                // If the modifier currently modifies the slot we are updating
                if (modifier.slot().contains(slot)) {
                    AttributeInstance attributeInstance = getAttribute(modifier.attribute());
                    attributeInstance.removeModifier(modifier.modifier().id());
                }
            }
        }
        AttributeList newAttributes = newItemStack.get(ItemComponent.ATTRIBUTE_MODIFIERS);
        // Add new attributes
        if (newAttributes != null) {
            for (AttributeList.Modifier modifier : newAttributes.modifiers()) {
                // If the modifier currently modifies the slot we are updating
                if (modifier.slot().contains(slot)) {
                    AttributeInstance attributeInstance = getAttribute(modifier.attribute());
                    attributeInstance.addModifier(modifier.modifier());
                }
            }
        }
    }

    @Override
    public void update(long time) {
        // Fire
        if (remainingFireTicks > 0 && --remainingFireTicks == 0) {
            EventDispatcher.callCancellable(new EntityFireExtinguishEvent(this, true), () -> entityMeta.setOnFire(false));
        }

        // Items picking
        if (canPickupItem() && itemPickupCooldown.isReady(time)) {
            itemPickupCooldown.refreshLastUpdate(time);
            final Point loweredPosition = position.sub(0, .5, 0);
            this.instance.getEntityTracker().nearbyEntities(position, expandedBoundingBox.width(),
                    EntityTracker.Target.ITEMS, itemEntity -> {
                        if (this instanceof Player player && !itemEntity.isViewer(player)) return;
                        if (!itemEntity.isPickable()) return;
                        if (expandedBoundingBox.intersectEntity(loweredPosition, itemEntity)) {
                            PickupItemEvent pickupItemEvent = new PickupItemEvent(this, itemEntity);
                            EventDispatcher.callCancellable(pickupItemEvent, () -> {
                                final ItemStack item = itemEntity.getItemStack();
                                sendPacketToViewersAndSelf(new CollectItemPacket(itemEntity.getEntityId(), getEntityId(), item.amount()));
                                itemEntity.remove();
                            });
                        }
                    });
        }
    }

    /**
     * Gets the amount of arrows in the entity.
     *
     * @return the arrow count
     */
    public int getArrowCount() {
        return this.arrowCount;
    }

    /**
     * Changes the amount of arrow stuck in the entity.
     *
     * @param arrowCount the arrow count
     */
    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setArrowCount(arrowCount);
        }
    }

    /**
     * Gets if the entity is invulnerable.
     *
     * @return true if the entity is invulnerable
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }

    /**
     * Makes the entity vulnerable or invulnerable.
     *
     * @param invulnerable should the entity be invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    /**
     * Kills the entity, trigger the {@link EntityDeathEvent} event.
     */
    public void kill() {
        refreshIsDead(true); // So the entity isn't killed over and over again
        triggerStatus((byte) EntityStatuses.LivingEntity.PLAY_DEATH_SOUND); // Start death animation status
        setPose(EntityPose.DYING);
        setHealth(0);

        // Reset velocity
        this.velocity = Vec.ZERO;

        // Remove passengers if any
        if (hasPassenger()) {
            getPassengers().forEach(this::removePassenger);
        }

        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(this);
        EventDispatcher.call(entityDeathEvent);
    }

    /**
     * Gets the amount of ticks this entity is on fire for.
     *
     * @return the remaining duration of fire in ticks, 0 if not on fire
     */
    public int getFireTicks() {
        return remainingFireTicks;
    }

    /**
     * Sets this entity on fire for the given ticks.
     *
     * @param ticks duration of fire in ticks
     */
    public void setFireTicks(int ticks) {
        int fireTicks = Math.max(0, ticks);
        if (fireTicks > 0) {
            EntitySetFireEvent entitySetFireEvent = new EntitySetFireEvent(this, ticks);
            EventDispatcher.call(entitySetFireEvent);
            if (entitySetFireEvent.isCancelled()) return;

            fireTicks = Math.max(0, entitySetFireEvent.getFireTicks());
            if (fireTicks > 0) {
                remainingFireTicks = fireTicks;
                entityMeta.setOnFire(true);
                return;
            }
        }

        if (remainingFireTicks != 0) {
            EntityFireExtinguishEvent entityFireExtinguishEvent = new EntityFireExtinguishEvent(this, false);
            EventDispatcher.callCancellable(entityFireExtinguishEvent, () -> entityMeta.setOnFire(false));
        }

        remainingFireTicks = fireTicks;
    }

    public boolean damage(@NotNull DynamicRegistry.Key<DamageType> type, float amount) {
        return damage(new Damage(type, null, null, null, amount));
    }

    /**
     * Damages the entity by a value, the type of the damage also has to be specified.
     *
     * @param damage the damage to be applied
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(@NotNull Damage damage) {
        if (isDead())
            return false;
        if (isInvulnerable() || isImmune(damage.getType())) {
            return false;
        }

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, damage, damage.getSound(this));
        EventDispatcher.callCancellable(entityDamageEvent, () -> {
            // Set the last damage type since the event is not cancelled
            this.lastDamage = entityDamageEvent.getDamage();

            float remainingDamage = entityDamageEvent.getDamage().getAmount();

            if (entityDamageEvent.shouldAnimate()) {
                sendPacketToViewersAndSelf(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));
            }

            sendPacketToViewersAndSelf(new DamageEventPacket(getEntityId(), damage.getTypeId(), damage.getAttacker() == null ? 0 : damage.getAttacker().getEntityId() + 1, damage.getSource() == null ? 0 : damage.getSource().getEntityId() + 1, damage.getSourcePosition()));

            // Additional hearts support
            if (this instanceof Player player) {
                final float additionalHearts = player.getAdditionalHearts();
                if (additionalHearts > 0) {
                    if (remainingDamage > additionalHearts) {
                        remainingDamage -= additionalHearts;
                        player.setAdditionalHearts(0);
                    } else {
                        player.setAdditionalHearts(additionalHearts - remainingDamage);
                        remainingDamage = 0;
                    }
                }
            }

            // Set the final entity health
            setHealth(getHealth() - remainingDamage);

            // play damage sound
            final SoundEvent sound = entityDamageEvent.getSound();
            if (sound != null) {
                Source soundCategory;
                if (this instanceof Player) {
                    soundCategory = Source.PLAYER;
                } else {
                    // TODO: separate living entity categories
                    soundCategory = Source.HOSTILE;
                }
                sendPacketToViewersAndSelf(new SoundEffectPacket(sound, soundCategory, getPosition(), 1.0f, 1.0f, 0));
            }
        });

        return !entityDamageEvent.isCancelled();
    }

    /**
     * Is this entity immune to the given type of damage?
     *
     * @param type the type of damage
     * @return true if this entity is immune to the given type of damage
     */
    public boolean isImmune(@NotNull DynamicRegistry.Key<DamageType> type) {
        return false;
    }

    /**
     * Gets the entity health.
     *
     * @return the entity health
     */
    public float getHealth() {
        return this.health;
    }

    /**
     * Changes the entity health, kill it if {@code health} is &lt;= 0 and is not dead yet.
     *
     * @param health the new entity health
     */
    public void setHealth(float health) {
        this.health = Math.min(health, (float) getAttributeValue(Attribute.MAX_HEALTH));
        if (this.health <= 0 && !isDead) {
            kill();
        }
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setHealth(this.health);
        }
    }

    /**
     * Gets the last damage source which damaged of this entity.
     *
     * @return the last damage source, null if not any
     */
    public @Nullable Damage getLastDamageSource() {
        return lastDamage;
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attribute#MAX_HEALTH}.
     */
    public void heal() {
        setHealth((float) getAttributeValue(Attribute.MAX_HEALTH));
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    public @NotNull AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributeModifiers.computeIfAbsent(attribute.name(),
                s -> new AttributeInstance(attribute, this::onAttributeChanged));
    }

    /**
     * Retrieves all {@link AttributeInstance}s on this entity.
     *
     * @return a collection of all attribute instances on this entity
     */
    public @NotNull @UnmodifiableView Collection<AttributeInstance> getAttributes() {
        return unmodifiableModifiers;
    }

    /**
     * Callback used when an attribute instance has been modified.
     *
     * @param attributeInstance the modified attribute instance
     */
    protected void onAttributeChanged(@NotNull AttributeInstance attributeInstance) {
        if (!shouldSendAttributes()) return;

        boolean self = false;
        if (this instanceof Player player) {
            PlayerConnection playerConnection = player.playerConnection;
            // connection null during Player initialization (due to #super call)
            self = playerConnection != null && playerConnection.getConnectionState() == ConnectionState.PLAY;
        }
        EntityAttributesPacket propertiesPacket = new EntityAttributesPacket(getEntityId(), List.of(
                new EntityAttributesPacket.Property(
                        attributeInstance.attribute(),
                        attributeInstance.getBaseValue(),
                        attributeInstance.getModifiers())
        ));
        if (self) {
            sendPacketToViewersAndSelf(propertiesPacket);
        } else {
            sendPacketToViewers(propertiesPacket);
        }
    }

    /**
     * Retrieves the attribute value.
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public double getAttributeValue(@NotNull Attribute attribute) {
        AttributeInstance instance = attributeModifiers.get(attribute.name());
        return (instance != null) ? instance.getValue() : attribute.defaultValue();
    }

    /**
     * Gets if the entity is dead or not.
     *
     * @return true if the entity is dead
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Gets if the entity is able to pickup items.
     *
     * @return true if the entity is able to pickup items
     */
    public boolean canPickupItem() {
        return canPickupItem;
    }

    /**
     * When set to false, the entity will not be able to pick {@link ItemEntity} on the ground.
     *
     * @param canPickupItem can the entity pickup item
     */
    public void setCanPickupItem(boolean canPickupItem) {
        this.canPickupItem = canPickupItem;
    }

    /**
     * Check if this entity should send an {@link EntityAttributesPacket}. This is true for players and entities whose
     * spawn type is {@code LIVING}, but false for others.
     *
     * @return true if this entity needs to send attributes, false otherwise
     */
    protected boolean shouldSendAttributes() {
        final EntitySpawnType spawnType = this.entityType.registry().spawnType();

        // sending attributes for a non-living entity disconnects clients
        return spawnType == EntitySpawnType.PLAYER || spawnType == EntitySpawnType.LIVING;
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        player.sendPacket(new LazyPacket(this::getEquipmentsPacket));

        if (shouldSendAttributes())
            player.sendPacket(new LazyPacket(this::getPropertiesPacket));
    }

    @Override
    public void setBoundingBox(BoundingBox boundingBox) {
        super.setBoundingBox(boundingBox);
        this.expandedBoundingBox = boundingBox.expand(1, .5, 1);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation).
     */
    public void swingMainHand() {
        swingMainHand(false);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation).
     */
    public void swingOffHand() {
        swingOffHand(false);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation).
     *
     * @param fromClient if true, broadcast only to viewers
     */
    @ApiStatus.Internal
    public void swingMainHand(boolean fromClient) {
        swingHand(fromClient, EntityAnimationPacket.Animation.SWING_MAIN_ARM);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation).
     *
     * @param fromClient if true, broadcast only to viewers
     */
    @ApiStatus.Internal
    public void swingOffHand(boolean fromClient) {
        swingHand(fromClient, EntityAnimationPacket.Animation.SWING_OFF_HAND);
    }

    private void swingHand(boolean fromClient, EntityAnimationPacket.Animation animation) {
        EntityAnimationPacket packet = new EntityAnimationPacket(getEntityId(), animation);
        if (fromClient) {
            sendPacketToViewers(packet);
        } else {
            sendPacketToViewersAndSelf(packet);
        }
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setNotifyAboutChanges(false);
            meta.setHandActive(isHandActive);
            meta.setActiveHand(offHand ? PlayerHand.OFF : PlayerHand.MAIN);
            meta.setInRiptideSpinAttack(riptideSpinAttack);
            meta.setNotifyAboutChanges(true);

            updatePose(); // Riptide spin attack has a pose
        }
    }

    public boolean isFlyingWithElytra() {
        return this.entityMeta.isFlyingWithElytra();
    }

    public void setFlyingWithElytra(boolean isFlying) {
        this.entityMeta.setFlyingWithElytra(isFlying);
        updatePose();
    }

    /**
     * Used to change the {@code isDead} internal field.
     *
     * @param isDead the new field value
     */
    protected void refreshIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    /**
     * Gets an {@link EntityAttributesPacket} for this entity with all of its attributes values.
     *
     * @return an {@link EntityAttributesPacket} linked to this entity
     */
    protected @NotNull EntityAttributesPacket getPropertiesPacket() {
        List<EntityAttributesPacket.Property> properties = new ArrayList<>();
        for (AttributeInstance instance : attributeModifiers.values()) {
            properties.add(new EntityAttributesPacket.Property(instance.attribute(), instance.getBaseValue(), instance.getModifiers()));
        }
        return new EntityAttributesPacket(getEntityId(), properties);
    }

    /**
     * Changes the {@link Team} for the entity.
     *
     * @param team The new team
     */
    public void setTeam(@Nullable Team team) {
        if (this.team == team) return;
        String member = this instanceof Player player ? player.getUsername() : getUuid().toString();
        if (this.team != null) {
            this.team.removeMember(member);
        }
        this.team = team;
        if (team != null) {
            team.addMember(member);
        }
    }

    /**
     * Gets the {@link Team} of the entity.
     *
     * @return the {@link Team}
     */
    public @Nullable Team getTeam() {
        return team;
    }

    /**
     * Gets the target (not-air) block position of the entity.
     *
     * @param maxDistance The max distance to scan before returning null
     * @return The block position targeted by this entity, null if non are found
     */
    public @Nullable Point getTargetBlockPosition(int maxDistance) {
        Iterator<Point> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            final Point position = it.next();
            if (!getInstance().getBlock(position).isAir()) return position;
        }
        return null;
    }

    /**
     * Gets {@link EntityMeta} of this entity casted to {@link LivingEntityMeta}.
     *
     * @return null if meta of this entity does not inherit {@link LivingEntityMeta}, casted value otherwise.
     */
    public @Nullable LivingEntityMeta getLivingEntityMeta() {
        if (this.entityMeta instanceof LivingEntityMeta) {
            return (LivingEntityMeta) this.entityMeta;
        }
        return null;
    }

    /**
     * Applies knockback
     * <p>
     * Note: The strength is reduced based on knockback resistance
     *
     * @param strength the strength of the knockback, 0.4 is the vanilla value for a bare hand hit
     * @param x        knockback on x axle, for default knockback use the following formula <pre>sin(attacker.yaw * (pi/180))</pre>
     * @param z        knockback on z axle, for default knockback use the following formula <pre>-cos(attacker.yaw * (pi/180))</pre>
     */
    @Override
    public void takeKnockback(float strength, final double x, final double z) {
        strength *= (float) (1 - getAttributeValue(Attribute.KNOCKBACK_RESISTANCE));
        super.takeKnockback(strength, x, z);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    @Override
    public @NotNull Acquirable<? extends LivingEntity> acquirable() {
        return (Acquirable<? extends LivingEntity>) super.acquirable();
    }
}
