package net.minestom.server.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//TODO: Default attributes registration (and limitation ?)
public abstract class LivingEntity extends Entity implements EquipmentHandler {

    // Item pickup
    protected boolean canPickupItem;
    protected UpdateOption itemPickupCooldown = new UpdateOption(5, TimeUnit.TICK);
    private long lastItemPickupCheckTime;

    protected boolean isDead;

    private float health;
    protected DamageType lastDamageSource;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    private final Map<String, AttributeInstance> attributeModifiers = new ConcurrentHashMap<>(Attribute.values().length);

    private boolean isHandActive;
    private boolean offHand;
    private boolean riptideSpinAttack;
    // The number of arrows in entity
    private int arrowCount;

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

    public LivingEntity(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setupAttributes();
        setGravity(0.02f, 0.08f, 3.92f);
    }

    public LivingEntity(@NotNull EntityType entityType) {
        this(entityType, new Position());
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
        if (canPickupItem() && !CooldownUtils.hasCooldown(time, lastItemPickupCheckTime, itemPickupCooldown)) {
            this.lastItemPickupCheckTime = time;

            final Chunk chunk = getChunk(); // TODO check surrounding chunks
            final Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {

                    // Do not pickup if not visible
                    if (this instanceof Player && !entity.isViewer((Player) this))
                        continue;

                    final ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable())
                        continue;

                    final BoundingBox itemBoundingBox = itemEntity.getBoundingBox();
                    if (expandedBoundingBox.intersect(itemBoundingBox)) {
                        if (itemEntity.shouldRemove() || itemEntity.isRemoveScheduled())
                            continue;
                        final ItemStack item = itemEntity.getItemStack();
                        PickupItemEvent pickupItemEvent = new PickupItemEvent(this, item);
                        callCancellableEvent(PickupItemEvent.class, pickupItemEvent, () -> {
                            CollectItemPacket collectItemPacket = new CollectItemPacket();
                            collectItemPacket.collectedEntityId = itemEntity.getEntityId();
                            collectItemPacket.collectorEntityId = getEntityId();
                            collectItemPacket.pickupItemCount = item.getAmount();
                            sendPacketToViewersAndSelf(collectItemPacket);
                            entity.remove();
                        });
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 7);
            fillMetadataIndex(packet, 8);
            fillMetadataIndex(packet, 11);
        };
    }

    @Override
    protected void fillMetadataIndex(@NotNull BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_BYTE);
            byte activeHandValue = 0;
            if (isHandActive) {
                activeHandValue += 1;
                if (offHand)
                    activeHandValue += 2;
                if (riptideSpinAttack)
                    activeHandValue += 4;
            }
            packet.writeByte(activeHandValue);
        } else if (index == 8) {
            packet.writeByte((byte) 8);
            packet.writeByte(METADATA_FLOAT);
            packet.writeFloat(health);
        } else if (index == 11) {
            packet.writeByte((byte) 11);
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(arrowCount);
        }
    }

    /**
     * Gets the amount of arrows in the entity.
     *
     * @return the arrow count
     */
    public int getArrowCount() {
        return arrowCount;
    }

    /**
     * Changes the amount of arrow stuck in the entity.
     *
     * @param arrowCount the arrow count
     */
    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
        sendMetadataIndex(11);
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
        velocity.zero();

        // Remove passengers if any
        if (hasPassenger()) {
            getPassengers().forEach(this::removePassenger);
        }

        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(this);
        callEvent(EntityDeathEvent.class, entityDeathEvent);
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration in ticks of the effect
     */
    public void setFireForDuration(int duration) {
        setFireForDuration(duration, TimeUnit.TICK);
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration of the effect
     * @param unit     unit used to express the duration
     * @see #setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(int duration, TimeUnit unit) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(this, duration, unit);

        // Do not start fire event if the fire needs to be removed (< 0 duration)
        if (duration > 0) {
            callCancellableEvent(EntityFireEvent.class, entityFireEvent, () -> {
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

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, type, value);
        callCancellableEvent(EntityDamageEvent.class, entityDamageEvent, () -> {
            // Set the last damage type since the event is not cancelled
            this.lastDamageSource = entityDamageEvent.getDamageType();

            float remainingDamage = entityDamageEvent.getDamage();

            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = getEntityId();
            entityAnimationPacket.animation = EntityAnimationPacket.Animation.TAKE_DAMAGE;
            sendPacketToViewersAndSelf(entityAnimationPacket);

            // Additional hearts support
            if (this instanceof Player) {
                final Player player = (Player) this;
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
            final Sound sound = type.getSound(this);
            if (sound != null) {
                SoundCategory soundCategory;
                if (this instanceof Player) {
                    soundCategory = SoundCategory.PLAYERS;
                } else {
                    // TODO: separate living entity categories
                    soundCategory = SoundCategory.HOSTILE;
                }

                SoundEffectPacket damageSoundPacket =
                        SoundEffectPacket.create(soundCategory, sound,
                                getPosition(),
                                1.0f, 1.0f);
                sendPacketToViewersAndSelf(damageSoundPacket);
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
        return health;
    }

    /**
     * Changes the entity health, kill it if {@code health} is &gt;= 0 and is not dead yet.
     *
     * @param health the new entity health
     */
    public void setHealth(float health) {
        health = Math.min(health, getMaxHealth());

        this.health = health;
        if (this.health <= 0 && !isDead) {
            kill();
        }
        sendMetadataIndex(8); // Health metadata index
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
     * Gets the entity max health from {@link #getAttributeValue(Attribute)} {@link Attributes#MAX_HEALTH}.
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getAttributeValue(Attributes.MAX_HEALTH);
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attributes#MAX_HEALTH}.
     */
    public void heal() {
        setHealth(getAttributeValue(Attributes.MAX_HEALTH));
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    @NotNull
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributeModifiers.computeIfAbsent(attribute.getKey(),
                s -> new AttributeInstance(attribute, this::onAttributeChanged));
    }

    /**
     * Callback used when an attribute instance has been modified.
     *
     * @param instance the modified attribute instance
     */
    protected void onAttributeChanged(@NotNull AttributeInstance instance) {
    }

    /**
     * Retrieves the attribute value.
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public float getAttributeValue(@NotNull Attribute attribute) {
        AttributeInstance instance = attributeModifiers.get(attribute.getKey());
        return (instance != null) ? instance.getValue() : attribute.getDefaultValue();
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
    public void setBoundingBox(float x, float y, float z) {
        super.setBoundingBox(x, y, z);
        this.expandedBoundingBox = getBoundingBox().expand(1, 0.5f, 1);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation).
     */
    public void swingMainHand() {
        EntityAnimationPacket animationPacket = new EntityAnimationPacket();
        animationPacket.entityId = getEntityId();
        animationPacket.animation = EntityAnimationPacket.Animation.SWING_MAIN_ARM;
        sendPacketToViewers(animationPacket);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation).
     */
    public void swingOffHand() {
        EntityAnimationPacket animationPacket = new EntityAnimationPacket();
        animationPacket.entityId = getEntityId();
        animationPacket.animation = EntityAnimationPacket.Animation.SWING_OFF_HAND;
        sendPacketToViewers(animationPacket);
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        this.isHandActive = isHandActive;
        this.offHand = offHand;
        this.riptideSpinAttack = riptideSpinAttack;

        sendPacketToViewers(getMetadataPacket());
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
    @NotNull
    protected EntityPropertiesPacket getPropertiesPacket() {
        // Get all the attributes which should be sent to the client
        final AttributeInstance[] instances = attributeModifiers.values().stream()
                .filter(i -> i.getAttribute().isShared())
                .toArray(AttributeInstance[]::new);


        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket();
        propertiesPacket.entityId = getEntityId();

        EntityPropertiesPacket.Property[] properties = new EntityPropertiesPacket.Property[instances.length];
        for (int i = 0; i < properties.length; ++i) {
            EntityPropertiesPacket.Property property = new EntityPropertiesPacket.Property();

            final float value = instances[i].getBaseValue();

            property.instance = instances[i];
            property.attribute = instances[i].getAttribute();
            property.value = value;

            properties[i] = property;
        }

        propertiesPacket.properties = properties;
        return propertiesPacket;
    }

    /**
     * Sets all the attributes to {@link Attribute#getDefaultValue()}
     */
    private void setupAttributes() {
        for (Attribute attribute : Attribute.values()) {
            final AttributeInstance attributeInstance = new AttributeInstance(attribute, this::onAttributeChanged);
            this.attributeModifiers.put(attribute.getKey(), attributeInstance);
        }
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
     * @see #setFireDamagePeriod(long, TimeUnit)
     */
    public long getFireDamagePeriod() {
        return fireDamagePeriod;
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     * @param timeUnit         the time unit
     */
    public void setFireDamagePeriod(long fireDamagePeriod, @NotNull TimeUnit timeUnit) {
        fireDamagePeriod = timeUnit.toMilliseconds(fireDamagePeriod);
        this.fireDamagePeriod = fireDamagePeriod;
    }

    /**
     * Changes the {@link Team} for the entity.
     *
     * @param team The new team
     */
    public void setTeam(Team team) {
        if (this.team == team) return;

        String member;

        if (this instanceof Player) {
            Player player = (Player) this;
            member = player.getUsername();
        } else {
            member = this.uuid.toString();
        }

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
     * Gets the line of sight in {@link BlockPosition} of the entity.
     *
     * @param maxDistance The max distance to scan
     * @return A list of {@link BlockPosition} in this entities line of sight
     */
    public List<BlockPosition> getLineOfSight(int maxDistance) {
        List<BlockPosition> blocks = new ArrayList<>();
        Iterator<BlockPosition> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            BlockPosition position = it.next();
            if (Block.fromStateId(getInstance().getBlockStateId(position)) != Block.AIR) blocks.add(position);
        }
        return blocks;
    }

    /**
     * Gets the target (not-air) {@link BlockPosition} of the entity.
     *
     * @param maxDistance The max distance to scan before returning null
     * @return The {@link BlockPosition} targeted by this entity, null if non are found
     */
    public BlockPosition getTargetBlockPosition(int maxDistance) {
        Iterator<BlockPosition> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            BlockPosition position = it.next();
            if (Block.fromStateId(getInstance().getBlockStateId(position)) != Block.AIR) return position;
        }
        return null;
    }

}
