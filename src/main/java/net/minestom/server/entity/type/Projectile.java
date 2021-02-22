package net.minestom.server.entity.type;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface Projectile {

    static void shoot(@NotNull Projectile projectile, @NotNull Entity shooter, Position to, double spread) {
        EntityShootEvent event = new EntityShootEvent(shooter, projectile, to, spread);
        shooter.callCancellableEvent(EntityShootEvent.class, event, () -> {
            Position from = shooter.getPosition().clone().add(0D, shooter.getEyeHeight(), 0D);
            shoot(projectile, from, to, event.getSpread());
        });
    }

    @SuppressWarnings("ConstantConditions")
    static void shoot(@NotNull Projectile projectile, @NotNull Position from, @NotNull Position to, double spread) {
        Check.argCondition(projectile instanceof Entity, "Projectile must be an instance of Entity!");
        Entity proj     = (Entity) projectile;
        double dx       = to.getX() - from.getX();
        double dy       = to.getY() - from.getY();
        double dz       = to.getZ() - from.getZ();
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
        dx *= 2;
        dy *= 2;
        dz *= 2;
        Vector velocity = proj.getVelocity();
        velocity.setX(dx);
        velocity.setY(dy);
        velocity.setZ(dz);
        xzLength = Math.sqrt(dx * dx + dz * dz);
        double yaw   = Math.max(Math.abs(dx), Math.abs(dz));
        double pitch = Math.max(Math.abs(dy), Math.abs(xzLength));
        proj.setView((float) (yaw * Math.toDegrees(1D)), (float) (pitch * Math.toDegrees(1D)));
    }

}
