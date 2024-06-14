package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called with {@link LivingEntity#damage(DamageType, float)}.
 */
public class EntityDamageEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private final Damage damage;
    private SoundEvent sound;
    private boolean animation = true;

    private boolean cancelled;

    public EntityDamageEvent(@NotNull LivingEntity entity, @NotNull Damage damage, @Nullable SoundEvent sound) {
        this.entity = entity;
        this.damage = damage;
        this.sound = sound;
    }

    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * Gets the damage type.
     *
     * @return the damage type
     */
    @NotNull
    public Damage getDamage() {
        return damage;
    }

    /**
     * Gets the damage sound.
     *
     * @return the damage sound
     */
    @Nullable
    public SoundEvent getSound() {
        return sound;
    }

    /**
     * Changes the damage sound.
     *
     * @param sound the new damage sound
     */
    public void setSound(@Nullable SoundEvent sound) {
        this.sound = sound;
    }

    /**
     * Gets whether the damage animation should be played.
     *
     * @return true if the animation should be played
     */
    public boolean shouldAnimate() {
        return animation;
    }

    /**
     * Sets whether the damage animation should be played.
     *
     * @param animation whether the animation should be played or not
     */
    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
