package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called with {@link LivingEntity#damage(net.minestom.server.registry.DynamicRegistry.Key, float)}.
 */
public record EntityDamageEvent(@NotNull LivingEntity entity, @NotNull Damage damage, @Nullable SoundEvent sound, boolean animate, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntityDamageEvent> {

    public EntityDamageEvent(@NotNull LivingEntity entity, @NotNull Damage damage, @Nullable SoundEvent sound) {
        this(entity, damage, sound, true, false);
    }

    @NotNull
    @Override
    public LivingEntity entity() {
        return entity;
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
     * Gets whether the damage animation should be played.
     *
     * @return true if the animation should be played
     */
    public boolean shouldAnimate() {
        return animate;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<EntityDamageEvent> {
        private final LivingEntity entity;
        private final Damage damage;
        private SoundEvent sound;
        private boolean animate;
        private boolean cancelled;

        public Mutator(EntityDamageEvent event) {
            this.entity = event.entity;
            this.damage = event.damage;
            this.sound = event.sound;
            this.animate = event.animate;
            this.cancelled = event.cancelled;
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
            return animate;
        }

        /**
         * Sets whether the damage animation should be played.
         *
         * @param animation whether the animation should be played or not
         */
        public void setAnimation(boolean animation) {
            this.animate = animation;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull EntityDamageEvent mutated() {
            return new EntityDamageEvent(this.entity, this.damage, this.sound, this.animate, this.cancelled);
        }
    }
}
