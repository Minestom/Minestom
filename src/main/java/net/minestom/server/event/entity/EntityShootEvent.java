package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link Projectile#shoot(Projectile, Entity, Position, double, double)}.
 */
public class EntityShootEvent extends EntityEvent implements CancellableEvent {

    private final Projectile projectile;
    private final Position to;
    private double power;
    private double spread;

    private boolean cancelled;

    public EntityShootEvent(@NotNull Entity entity, @NotNull Projectile projectile, @NotNull Position to, double power, double spread) {
        super(entity);
        this.projectile = projectile;
        this.to = to;
        this.power = power;
        this.spread = spread;
    }

    /**
     * Gets the projectile.
     *
     * @return the projectile.
     */
    public Projectile getProjectile() {
        return this.projectile;
    }

    /**
     * Gets the position projectile was shot to.
     *
     * @return the position projectile was shot to.
     */
    public Position getTo() {
        return this.to;
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
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
