package net.minestom.server.entity.type;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface Projectile {

    static void shoot(@NotNull Projectile projectile, @NotNull Entity shooter, Position to, double power, double spread) {
        Check.argCondition(!(projectile instanceof Entity), "Projectile must be an instance of Entity!");
        EntityShootEvent event = new EntityShootEvent(shooter, projectile, to, power, spread);
        shooter.callEvent(EntityShootEvent.class, event);
        if (event.isCancelled()) {
            Entity proj = (Entity) projectile;
            proj.remove();
            return;
        }
        Position from = shooter.getPosition().clone().add(0D, shooter.getEyeHeight(), 0D);
        shoot(projectile, from, to, event.getPower(), event.getSpread());
    }

    @SuppressWarnings("ConstantConditions")
    static void shoot(@NotNull Projectile projectile, @NotNull Position from, @NotNull Position to, double power, double spread) {
        Check.argCondition(!(projectile instanceof Entity), "Projectile must be an instance of Entity!");
        Entity proj = (Entity) projectile;
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double xzLength = Math.sqrt(dx * dx + dz * dz);
        dy += xzLength * 0.20000000298023224D;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;
        Vector velocity = proj.getVelocity();
        velocity.setX(dx);
        velocity.setY(dy);
        velocity.setZ(dz);
        velocity.multiply(20 * power);
        proj.setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    /**
     * Gets the shooter of this projectile.
     *
     * @return the shooter of this projectile.
     */
    @Nullable
    Entity getShooter();

}
