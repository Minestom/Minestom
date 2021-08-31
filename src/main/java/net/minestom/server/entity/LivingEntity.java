package net.minestom.server.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.features.EntityFeatures;
import net.minestom.server.entity.features.living.EntityFeatureLiving;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

public class LivingEntity extends Entity {

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public LivingEntity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);

        if (!(this instanceof Player)) {
            enableFeature(EntityFeatures.LIVING).heal();
            enableFeature(EntityFeatures.EQUIPMENT);
        }
    }

    public LivingEntity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * Gets the amount of arrows in the entity.
     *
     * @return the arrow count
     */
    public int getArrowCount() {
        return getLivingFeature().getArrowCount();
    }

    /**
     * Changes the amount of arrow stuck in the entity.
     *
     * @param arrowCount the arrow count
     */
    public void setArrowCount(int arrowCount) {
        getLivingFeature().setArrowCount(arrowCount);
    }

    /**
     * Gets if the entity is invulnerable.
     *
     * @return true if the entity is invulnerable
     */
    public boolean isInvulnerable() {
        return getLivingFeature().isInvulnerable();
    }

    /**
     * Makes the entity vulnerable or invulnerable.
     *
     * @param invulnerable should the entity be invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        getLivingFeature().setInvulnerable(invulnerable);
    }

    /**
     * Kills the entity, trigger the {@link EntityDeathEvent} event.
     */
    public void kill() {
        getLivingFeature().kill();
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
        getLivingFeature().setFireForDuration(duration);
    }

    /**
     * Damages the entity by a value, the type of the damage also has to be specified.
     *
     * @param type  the damage type
     * @param value the amount of damage
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(@NotNull DamageType type, float value) {
        return getLivingFeature().damage(type, value);
    }

    /**
     * Is this entity immune to the given type of damage?
     *
     * @param type the type of damage
     * @return true if this entity is immune to the given type of damage
     */
    public boolean isImmune(@NotNull DamageType type) {
        return getLivingFeature().isImmune(type);
    }

    /**
     * Gets the entity health.
     *
     * @return the entity health
     */
    public float getHealth() {
        return getLivingFeature().getHealth();
    }

    /**
     * Changes the entity health, kill it if {@code health} is &gt;= 0 and is not dead yet.
     *
     * @param health the new entity health
     */
    public void setHealth(float health) {
        getLivingFeature().setHealth(health);
    }

    /**
     * Gets the last damage source which damaged of this entity.
     *
     * @return the last damage source, null if not any
     */
    @Nullable
    public DamageType getLastDamageSource() {
        return getLivingFeature().getLastDamageSource();
    }

    /**
     * Gets the entity max health from {@link Attribute#MAX_HEALTH}.
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getLivingFeature().getMaxHealth();
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link Attribute#MAX_HEALTH}.
     */
    public void heal() {
        getLivingFeature().heal();
    }

    /**
     * Gets if the entity is dead or not.
     *
     * @return true if the entity is dead
     */
    public boolean isDead() {
        return getLivingFeature().isDead();
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        getLivingFeature().refreshActiveHand(isHandActive, offHand, riptideSpinAttack);
    }

    /**
     * Used to change the {@code isDead} internal field.
     *
     * @param isDead the new field value
     */
    protected void refreshIsDead(boolean isDead) {
        getLivingFeature().refreshIsDead(isDead);
    }

    /**
     * Gets the time in ms between two fire damage applications.
     *
     * @return the time in ms
     * @see #setFireDamagePeriod(Duration)
     */
    public long getFireDamagePeriod() {
        return getLivingFeature().getFireDamagePeriod();
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     * @param temporalUnit     the time unit
     */
    public void setFireDamagePeriod(long fireDamagePeriod, @NotNull TemporalUnit temporalUnit) {
        getLivingFeature().setFireDamagePeriod(fireDamagePeriod, temporalUnit);
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     */
    public void setFireDamagePeriod(Duration fireDamagePeriod) {
        getLivingFeature().setFireDamagePeriod(fireDamagePeriod);
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    @NotNull
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return getFeature(EntityFeatures.ATTRIBUTES).getAttribute(attribute);
    }

    /**
     * Retrieves the attribute value.
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public float getAttributeValue(Attribute attribute) {
        return getFeature(EntityFeatures.ATTRIBUTES).getAttributeValue(attribute);
    }

    private EntityFeatureLiving getLivingFeature() {
        return getFeature(EntityFeatures.LIVING);
    }

}
