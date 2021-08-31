package net.minestom.server.entity.features.living;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.features.EntityFeatureBase;
import net.minestom.server.entity.features.EntityFeatures;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public class EntityFeatureLiving extends EntityFeatureBase {

    protected boolean isDead;

    protected DamageType lastDamageSource;

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

    private int arrowCount;
    private float health = 1F;

    public EntityFeatureLiving(Entity entity) {
        super(entity);
    }

    @Override
    public void tick(long time) {
        if (entity.isOnFire()) {
            if (time > fireExtinguishTime) {
                entity.setOnFire(false);
            } else {
                if (time - lastFireDamageTime > fireDamagePeriod) {
                    damage(DamageType.ON_FIRE, 1.0f);
                    lastFireDamageTime = time;
                }
            }
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
        entity.triggerStatus((byte) 3); // Start death animation status
        setHealth(0);

        // Reset velocity
        entity.setVelocity(Vec.ZERO);

        // Remove passengers if any
        if (entity.hasPassenger()) {
            entity.getPassengers().forEach(entity::removePassenger);
        }

        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(entity);
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
     * @see Entity#setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(int duration, TemporalUnit temporalUnit) {
        setFireForDuration(Duration.of(duration, temporalUnit));
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration of the effect
     * @see Entity#setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(Duration duration) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(entity, duration);

        // Do not start fire event if the fire needs to be removed (< 0 duration)
        if (duration.toMillis() > 0) {
            EventDispatcher.callCancellable(entityFireEvent, () -> {
                final long fireTime = entityFireEvent.getFireTime(TimeUnit.MILLISECOND);
                entity.setOnFire(true);
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

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(entity, type, value);
        EventDispatcher.callCancellable(entityDamageEvent, () -> {
            // Set the last damage type since the event is not cancelled
            this.lastDamageSource = entityDamageEvent.getDamageType();

            float remainingDamage = entityDamageEvent.getDamage();

            entity.sendPacketToViewersAndSelf(new EntityAnimationPacket(entity.getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));

            // Additional hearts support
            if (entity instanceof Player) {
                final Player player = (Player) entity;
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
            final SoundEvent sound = type.getSound(entity);
            if (sound != null) {
                Sound.Source soundCategory;
                if (entity instanceof Player) {
                    soundCategory = Sound.Source.PLAYER;
                } else {
                    // TODO: separate living entity categories
                    soundCategory = Sound.Source.HOSTILE;
                }

                SoundEffectPacket damageSoundPacket =
                        SoundEffectPacket.create(soundCategory, sound,
                                entity.getPosition(),
                                1.0f, 1.0f);
                entity.sendPacketToViewersAndSelf(damageSoundPacket);
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
     * Gets the entity max health from {@link #getAttributeValue(Attribute)} {@link Attributes#MAX_HEALTH}.
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getAttributeValue(Attribute.MAX_HEALTH);
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attributes#MAX_HEALTH}.
     */
    public void heal() {
        setHealth(getAttributeValue(Attribute.MAX_HEALTH));
    }

    public float getAttributeValue(Attribute attribute) {
        return entity.getFeature(EntityFeatures.ATTRIBUTES).getAttributeValue(attribute);
    }

    /**
     * Gets if the entity is dead or not.
     *
     * @return true if the entity is dead
     */
    public boolean isDead() {
        return isDead;
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

    /**
     * Used to change the {@code isDead} internal field.
     *
     * @param isDead the new field value
     */
    public void refreshIsDead(boolean isDead) {
        this.isDead = isDead;
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
     * Gets {@link net.minestom.server.entity.metadata.EntityMeta} of this entity casted to {@link LivingEntityMeta}.
     *
     * @return null if meta of this entity does not inherit {@link LivingEntityMeta}, casted value otherwise.
     */
    public LivingEntityMeta getLivingEntityMeta() {
        EntityMeta meta = entity.getEntityMeta();
        if (meta instanceof LivingEntityMeta) {
            return (LivingEntityMeta) meta;
        }
        return null;
    }
}
