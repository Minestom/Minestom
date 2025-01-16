package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link EntityProjectile#shoot(Point, double, double)}
 */
public record EntityShootEvent(@NotNull Entity entity, @NotNull Entity projectile, @NotNull Point to, double power, double spread, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntityShootEvent> {

    public EntityShootEvent(@NotNull Entity entity, @NotNull Entity projectile, @NotNull Point to, double power, double spread) {
        this(entity, projectile, to, power, spread, false);
    }

    /**
     * Gets the projectile.
     *
     * @return the projectile.
     */
    public @NotNull Entity projectile() {
        return projectile;
    }

    /**
     * Gets the position projectile was shot to.
     *
     * @return the position projectile was shot to.
     */
    public @NotNull Point to() {
        return to;
    }

    /**
     * Gets shot spread.
     *
     * @return shot spread.
     */
    @Override
    public double spread() {
        return spread;
    }

    /**
     * Gets shot power.
     *
     * @return shot power.
     */
    @Override
    public double power() {
        return power;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<EntityShootEvent> {
        private final Entity entity;
        private final Entity projectile;
        private final Point to;
        private double power;
        private double spread;

        private boolean cancelled;

        public Mutator(EntityShootEvent event) {
            this.entity = event.entity;
            this.projectile = event.projectile;
            this.to = event.to;
            this.power = event.power;
            this.spread = event.spread;
            this.cancelled = event.cancelled;
        }

        /**
         * Gets shot spread.
         *
         * @return shot spread.
         */
        public double getSpread() {
            return this.spread;
        }

        /**
         * Sets shot spread.
         *
         * @param spread shot spread.
         */
        public void setSpread(double spread) {
            this.spread = spread;
        }

        /**
         * Gets shot power.
         *
         * @return shot power.
         */
        public double getPower() {
            return this.power;
        }

        /**
         * Sets shot power.
         *
         * @param power shot power.
         */
        public void setPower(double power) {
            this.power = power;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public @NotNull EntityShootEvent mutated() {
            return new EntityShootEvent(this.entity, this.projectile, this.to, this.power, this.spread, this.cancelled);
        }
    }

}

