package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LivingEntity extends Entity implements EquipmentHandler {

    // ItemStack pickup
    protected boolean canPickupItem;
    protected Cooldown itemPickupCooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

    protected boolean isDead;

    protected DamageType lastDamageSource;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    private final Map<String, AttributeInstance> attributeModifiers = new ConcurrentHashMap<>();

    // Abilities
    protected boolean invulnerable;

    /**
     * Time at which this entity must be extinguished
     */
    private long fireExtinguishTime;

    /**
     * Last time the fire damage was applied
     */
    private long lastFireDamageTime;

    /**
     * Period, in ms, between two fire damage applications
     */
    private long fireDamagePeriod = 1000L;

    private Team team;

    private int arrowCount;
    private float health = 1F;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public LivingEntity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);
        initEquipments();
    }

    public LivingEntity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    private void initEquipments() {
        this.mainHandItem = ItemStack.AIR;
        this.offHandItem = ItemStack.AIR;

        this.helmet = ItemStack.AIR;
        this.chestplate = ItemStack.AIR;
        this.leggings = ItemStack.AIR;
        this.boots = ItemStack.AIR;
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        this.mainHandItem = getEquipmentItem(itemStack, EquipmentSlot.MAIN_HAND);
        syncEquipment(EquipmentSlot.MAIN_HAND);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        this.offHandItem = getEquipmentItem(itemStack, EquipmentSlot.OFF_HAND);
        syncEquipment(EquipmentSlot.OFF_HAND);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, EquipmentSlot.HELMET);
        syncEquipment(EquipmentSlot.HELMET);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, EquipmentSlot.CHESTPLATE);
        syncEquipment(EquipmentSlot.CHESTPLATE);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, EquipmentSlot.LEGGINGS);
        syncEquipment(EquipmentSlot.LEGGINGS);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, EquipmentSlot.BOOTS);
        syncEquipment(EquipmentSlot.BOOTS);
    }

    private ItemStack getEquipmentItem(@NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
        EntityEquipEvent entityEquipEvent = new EntityEquipEvent(this, itemStack, slot);
        EventDispatcher.call(entityEquipEvent);
        return entityEquipEvent.getEquippedItem();
    }

    @Override
    public void update(long time) {
        if (isOnFire()) {
            if (time > fireExtinguishTime) {
                setOnFire(false);
            } else {
                if (time - lastFireDamageTime > fireDamagePeriod) {
                    damage(DamageType.ON_FIRE, 1.0f);
                    lastFireDamageTime = time;
                }
            }
        }

        // Items picking
        if (canPickupItem() && itemPickupCooldown.isReady(time)) {
            itemPickupCooldown.refreshLastUpdate(time);
            this.instance.getEntityTracker().nearbyEntities(position, expandedBoundingBox.getWidth(),
                    EntityTracker.Target.ITEMS, itemEntity -> {
                        if (this instanceof Player player && !itemEntity.isViewer(player)) return;
                        if (!itemEntity.isPickable()) return;
                        final BoundingBox itemBoundingBox = itemEntity.getBoundingBox();
                        if (expandedBoundingBox.intersect(itemBoundingBox)) {
                            PickupItemEvent pickupItemEvent = new PickupItemEvent(this, itemEntity);
                            EventDispatcher.callCancellable(pickupItemEvent, () -> {
                                final ItemStack item = itemEntity.getItemStack();
                                sendPacketToViewersAndSelf(new CollectItemPacket(itemEntity.getEntityId(), getEntityId(), item.getAmount()));
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
        triggerStatus((byte) 3); // Start death animation status
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
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration in ticks of the effect
     */
    public void setFireForDuration(int duration) {
        setFireForDuration(duration, TimeUnit.SERVER_TICK);
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration     duration of the effect
     * @param temporalUnit unit used to express the duration
     * @see #setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(int duration, TemporalUnit temporalUnit) {
        setFireForDuration(Duration.of(duration, temporalUnit));
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration of the effect
     * @see #setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(Duration duration) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(this, duration);

        // Do not start fire event if the fire needs to be removed (< 0 duration)
        if (duration.toMillis() > 0) {
            EventDispatcher.callCancellable(entityFireEvent, () -> {
                final long fireTime = entityFireEvent.getFireTime(TimeUnit.MILLISECOND);
                setOnFire(true);
                fireExtinguishTime = System.currentTimeMillis() + fireTime;
            });
        } else {
            fireExtinguishTime = System.currentTimeMillis();
        }
    }

    /**
     * Damages the entity by a value, the type of the damage also has to be specified.
     *
     * @param type  the damage type
     * @param value the amount of damage
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(@NotNull DamageType type, float value) {
        if (isDead())
            return false;
        if (isInvulnerable() || isImmune(type)) {
            return false;
        }

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, type, value, type.getSound(this));
        EventDispatcher.callCancellable(entityDamageEvent, () -> {
            // Set the last damage type since the event is not cancelled
            this.lastDamageSource = entityDamageEvent.getDamageType();

            float remainingDamage = entityDamageEvent.getDamage();

            if (entityDamageEvent.shouldAnimate()) {
                sendPacketToViewersAndSelf(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));
            }

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
                sendPacketToViewersAndSelf(new SoundEffectPacket(sound, soundCategory,
                        getPosition(), 1.0f, 1.0f));
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
    public boolean isImmune(@NotNull DamageType type) {
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
     * Changes the entity health, kill it if {@code health} is &gt;= 0 and is not dead yet.
     *
     * @param health the new entity health
     */
    public void setHealth(float health) {
        this.health = Math.min(health, getMaxHealth());
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
    @Nullable
    public DamageType getLastDamageSource() {
        return lastDamageSource;
    }

    /**
     * Gets the entity max health from {@link #getAttributeValue(Attribute)} {@link Attribute#MAX_HEALTH}.
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getAttributeValue(Attribute.MAX_HEALTH);
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attribute#MAX_HEALTH}.
     */
    public void heal() {
        setHealth(getAttributeValue(Attribute.MAX_HEALTH));
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    public @NotNull AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributeModifiers.computeIfAbsent(attribute.key(),
                s -> new AttributeInstance(attribute, this::onAttributeChanged));
    }

    /**
     * Callback used when an attribute instance has been modified.
     *
     * @param attributeInstance the modified attribute instance
     */
    protected void onAttributeChanged(@NotNull AttributeInstance attributeInstance) {
        boolean self = false;
        if (this instanceof Player player) {
            PlayerConnection playerConnection = player.playerConnection;
            // connection null during Player initialization (due to #super call)
            self = playerConnection != null && playerConnection.getConnectionState() == ConnectionState.PLAY;
        }
        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket(getEntityId(), List.of(attributeInstance));
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
    public float getAttributeValue(@NotNull Attribute attribute) {
        AttributeInstance instance = attributeModifiers.get(attribute.key());
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

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        player.sendPacket(new LazyPacket(this::getEquipmentsPacket));
        player.sendPacket(new LazyPacket(this::getPropertiesPacket));
        if (getTeam() != null) player.sendPacket(getTeam().createTeamsCreationPacket());
    }

    @Override
    public void setBoundingBox(double x, double y, double z) {
        super.setBoundingBox(x, y, z);
        this.expandedBoundingBox = getBoundingBox().expand(1, 0.5f, 1);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation).
     */
    public void swingMainHand() {
        sendPacketToViewers(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation).
     */
    public void swingOffHand() {
        sendPacketToViewers(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.SWING_OFF_HAND));
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setNotifyAboutChanges(false);
            meta.setHandActive(isHandActive);
            meta.setActiveHand(offHand ? Player.Hand.OFF : Player.Hand.MAIN);
            meta.setInRiptideSpinAttack(riptideSpinAttack);
            meta.setNotifyAboutChanges(true);
        }
    }

    public boolean isFlyingWithElytra() {
        return this.entityMeta.isFlyingWithElytra();
    }

    public void setFlyingWithElytra(boolean isFlying) {
        this.entityMeta.setFlyingWithElytra(isFlying);
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
     * Gets an {@link EntityPropertiesPacket} for this entity with all of its attributes values.
     *
     * @return an {@link EntityPropertiesPacket} linked to this entity
     */
    protected @NotNull EntityPropertiesPacket getPropertiesPacket() {
        return new EntityPropertiesPacket(getEntityId(), List.copyOf(attributeModifiers.values()));
    }

    @Override
    protected void handleVoid() {
        // Kill if in void
        if (getInstance().isInVoid(this.position)) {
            damage(DamageType.VOID, 10f);
        }
    }

    /**
     * Gets the time in ms between two fire damage applications.
     *
     * @return the time in ms
     * @see #setFireDamagePeriod(Duration)
     */
    public long getFireDamagePeriod() {
        return fireDamagePeriod;
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     * @param temporalUnit     the time unit
     */
    public void setFireDamagePeriod(long fireDamagePeriod, @NotNull TemporalUnit temporalUnit) {
        setFireDamagePeriod(Duration.of(fireDamagePeriod, temporalUnit));
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     */
    public void setFireDamagePeriod(Duration fireDamagePeriod) {
        this.fireDamagePeriod = fireDamagePeriod.toMillis();
    }

    /**
     * Changes the {@link Team} for the entity.
     *
     * @param team The new team
     */
    public void setTeam(Team team) {
        if (this.team == team) return;
        String member = this instanceof Player player ? player.getUsername() : uuid.toString();
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
    public Team getTeam() {
        return team;
    }

    /**
     * Gets the target (not-air) block position of the entity.
     *
     * @param maxDistance The max distance to scan before returning null
     * @return The block position targeted by this entity, null if non are found
     */
    public Point getTargetBlockPosition(int maxDistance) {
        Iterator<Point> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            final Point position = it.next();
            if (!getInstance().getBlock(position).isAir()) return position;
        }
        return null;
    }

    /**
     * Gets {@link net.minestom.server.entity.metadata.EntityMeta} of this entity casted to {@link LivingEntityMeta}.
     *
     * @return null if meta of this entity does not inherit {@link LivingEntityMeta}, casted value otherwise.
     */
    public LivingEntityMeta getLivingEntityMeta() {
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
        strength *= 1 - getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
        super.takeKnockback(strength, x, z);
    }
}
