package net.minestom.server.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Set;
import java.util.function.Consumer;

public abstract class LivingEntity extends Entity implements EquipmentHandler {

    protected boolean canPickupItem;
    protected boolean isDead;

    private float health;
    protected DamageType lastDamageSource;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    private final float[] attributeValues = new float[Attribute.values().length];

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

    public LivingEntity(EntityType entityType, Position spawnPosition) {
        super(entityType, spawnPosition);
        setupAttributes();
        setGravity(0.02f);
    }

    public LivingEntity(EntityType entityType) {
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
        if (canPickupItem()) {
            final Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
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
                        PickupItemEvent pickupItemEvent = new PickupItemEvent(item);
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
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
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
     * Get the amount of arrows in the entity
     *
     * @return the arrow count
     */
    public int getArrowCount() {
        return arrowCount;
    }

    /**
     * Change the amount of arrow stuck in the entity
     *
     * @param arrowCount the arrow count
     */
    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
        sendMetadataIndex(11);
    }

    /**
     * Get if the entity is invulnerable
     *
     * @return true if the entity is invulnerable
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }

    /**
     * Make the entity vulnerable or invulnerable
     *
     * @param invulnerable should the entity be invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    /**
     * Kill the entity, trigger the {@link EntityDeathEvent} event
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
     * Sets fire to this entity for a given duration
     *
     * @param duration duration in ticks of the effect
     */
    public void setFireForDuration(int duration) {
        setFireForDuration(duration, TimeUnit.TICK);
    }

    /**
     * Sets fire to this entity for a given duration
     *
     * @param duration duration of the effect
     * @param unit     unit used to express the duration
     */
    public void setFireForDuration(int duration, TimeUnit unit) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(this, duration, unit);

        // Do not start fire event if the fire needs to be removed (< 0 duration)
        if (duration > 0) {
            callCancellableEvent(EntityFireEvent.class, entityFireEvent, () -> {
                long fireTime = entityFireEvent.getFireTime(TimeUnit.MILLISECOND);
                setOnFire(true);
                fireExtinguishTime = System.currentTimeMillis() + fireTime;
            });
        } else {
            fireExtinguishTime = System.currentTimeMillis();
        }
    }

    /**
     * Damage the entity by a value, the type of the damage also has to be specified
     *
     * @param type  the damage type
     * @param value the amount of damage
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(DamageType type, float value) {
        if (isDead())
            return false;
        if (isInvulnerable() || isImmune(type)) {
            return false;
        }

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, type, value);
        callCancellableEvent(EntityDamageEvent.class, entityDamageEvent, () -> {
            float damage = entityDamageEvent.getDamage();

            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = getEntityId();
            entityAnimationPacket.animation = EntityAnimationPacket.Animation.TAKE_DAMAGE;
            sendPacketToViewersAndSelf(entityAnimationPacket);

            // Additional hearts support
            if (this instanceof Player) {
                final Player player = (Player) this;
                final float additionalHearts = player.getAdditionalHearts();
                if (additionalHearts > 0) {
                    if (damage > additionalHearts) {
                        damage -= additionalHearts;
                        player.setAdditionalHearts(0);
                    } else {
                        player.setAdditionalHearts(additionalHearts - damage);
                        damage = 0;
                    }
                }
            }

            // Set the final entity health
            setHealth(getHealth() - damage);

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

                SoundEffectPacket damageSoundPacket = SoundEffectPacket.create(soundCategory, sound, getPosition().getX(), getPosition().getY(), getPosition().getZ(), 1.0f, 1.0f);
                sendPacketToViewersAndSelf(damageSoundPacket);
            }

            // Set the last damage type since the event is not cancelled
            this.lastDamageSource = entityDamageEvent.getDamageType();
        });

        return !entityDamageEvent.isCancelled();
    }

    /**
     * Is this entity immune to the given type of damage?
     *
     * @param type the type of damage
     * @return true if this entity is immune to the given type of damage
     */
    public boolean isImmune(DamageType type) {
        return false;
    }

    /**
     * Get the entity health
     *
     * @return the entity health
     */
    public float getHealth() {
        return health;
    }

    /**
     * Change the entity health, kill it if {@code health} is &gt;= 0 and is not dead yet
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
     * Get the last damage source which damaged of this entity
     *
     * @return the last damage source, null if not any
     */
    public DamageType getLastDamageSource() {
        return lastDamageSource;
    }

    /**
     * Get the entity max health from {@link #getAttributeValue(Attribute)} {@link Attribute#MAX_HEALTH}
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getAttributeValue(Attribute.MAX_HEALTH);
    }

    /**
     * Set the heal of the entity as its max health
     * retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attribute#MAX_HEALTH}
     */
    public void heal() {
        setHealth(getAttributeValue(Attribute.MAX_HEALTH));
    }

    /**
     * Change the specified attribute value to {@code value}
     *
     * @param attribute The attribute to change
     * @param value     the new value of the attribute
     */
    public void setAttribute(Attribute attribute, float value) {
        this.attributeValues[attribute.ordinal()] = value;
    }

    /**
     * Retrieve the attribute value set by {@link #setAttribute(Attribute, float)}
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public float getAttributeValue(Attribute attribute) {
        return this.attributeValues[attribute.ordinal()];
    }

    /**
     * Get if the entity is dead or not
     *
     * @return true if the entity is dead
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Get if the entity is able to pickup items
     *
     * @return true if the entity is able to pickup items
     */
    public boolean canPickupItem() {
        return canPickupItem;
    }

    /**
     * When set to false, the entity will not be able to pick {@link ItemEntity} on the ground
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
     * Send a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation)
     */
    public void swingMainHand() {
        EntityAnimationPacket animationPacket = new EntityAnimationPacket();
        animationPacket.entityId = getEntityId();
        animationPacket.animation = EntityAnimationPacket.Animation.SWING_MAIN_ARM;
        sendPacketToViewers(animationPacket);
    }

    /**
     * Send a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation)
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

    protected void refreshIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    protected EntityPropertiesPacket getPropertiesPacket() {
        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket();
        propertiesPacket.entityId = getEntityId();

        final int length = Attribute.values().length;
        EntityPropertiesPacket.Property[] properties = new EntityPropertiesPacket.Property[length];
        for (int i = 0; i < length; i++) {
            EntityPropertiesPacket.Property property = new EntityPropertiesPacket.Property();

            final Attribute attribute = Attribute.values()[i];
            final float value = getAttributeValue(attribute);

            property.attribute = attribute;
            property.value = value;

            properties[i] = property;
        }

        propertiesPacket.properties = properties;
        return propertiesPacket;
    }

    /**
     * Set all the attributes to {@link Attribute#getDefaultValue()}
     */
    private void setupAttributes() {
        for (Attribute attribute : Attribute.values()) {
            setAttribute(attribute, attribute.getDefaultValue());
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
     * Get the time in ms between two fire damage applications
     *
     * @return the time in ms
     */
    public long getFireDamagePeriod() {
        return fireDamagePeriod;
    }

    /**
     * Change the delay between two fire damage applications
     *
     * @param fireDamagePeriod the delay
     * @param timeUnit         the time unit
     */
    public void setFireDamagePeriod(long fireDamagePeriod, TimeUnit timeUnit) {
        fireDamagePeriod = timeUnit.toMilliseconds(fireDamagePeriod);
        this.fireDamagePeriod = fireDamagePeriod;
    }

    /**
     * Change the {@link Team} for the entity
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
     * Gets the {@link Team} of the entity
     *
     * @return the {@link Team}
     */
    public Team getTeam() {
        return team;
    }
}
