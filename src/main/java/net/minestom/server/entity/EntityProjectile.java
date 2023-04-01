package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that allows to instantiate entities with projectile-like physics handling.
 */
public class EntityProjectile extends Entity {

    private final Entity shooter;

    private Vec collisionSignum;

    public EntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
        super(entityType);
        this.shooter = shooter;
        setup();
    }

    private void setup() {
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(this.shooter);
        }
    }

    @Nullable
    public Entity getShooter() {
        return this.shooter;
    }

    public void shoot(Point to, double power, double spread) {
        final EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, to, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }
        final Pos from = this.shooter.getPosition().add(0D, this.shooter.getEyeHeight(), 0D);
        shoot(from, to, shootEvent.getPower(), shootEvent.getSpread());
    }

    private void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        if (!hasNoGravity()) {
            final double xzLength = Math.sqrt(dx * dx + dz * dz);
            dy += xzLength * 0.20000000298023224D;
        }

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final double mul = 20 * power;
        this.velocity = new Vec(dx * mul, dy * mul, dz * mul);
        setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (hasNoGravity() && shouldUnstuck()) {
            setNoGravity(false);
            EventDispatcher.call(new ProjectileUncollideEvent(this));
        }

        if (!hasNoGravity()) {
            Vec deltaPos = velocity.div(MinecraftServer.TICK_PER_SECOND);
            handleEntityCollision(position, deltaPos);
        }
    }

    private void handleEntityCollision(@NotNull Pos position, @NotNull Vec deltaPos) {
        final BoundingBox boundingBox = getBoundingBox();
        final Entity shooter = getShooter();

        // Go over nearby entities and check which one will be hit and is the nearest
        Collection<Entity> entities = instance.getNearbyEntities(position, deltaPos.length() + 1);
        Entity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (Entity entity : entities) {
            // We won't check collisions with the shooter for the first ticks of the projectile's life,
            // because it spawns in them and will immediately be triggered
            if (getAliveTicks() < 6 && entity == shooter) continue;
            if (!(entity instanceof LivingEntity)) continue;

            // Check if moving projectile will hit the entity
            if (!entity.getBoundingBox().boundingBoxFullIntersectionCheck(boundingBox,
                    position, deltaPos, entity.getPosition())) continue;

            final double distanceSquared = getDistanceSquared(entity);
            if (distanceSquared < nearestDistanceSquared) {
                nearest = entity;
                nearestDistanceSquared = distanceSquared;
            }
        }

        if (nearest != null) {
            final var event = new ProjectileCollideWithEntityEvent(this, position, nearest);
            EventDispatcher.call(event);
            //teleport(position.add(0, 3, 0));
            //setVelocity(Vec.ZERO);
            remove();
        }
    }

    private boolean isFree(Point collidedPoint) {
        Block block = instance.getBlock(collidedPoint);

        // Move position slightly towards collision point because we will check for collision
        Point intersectPos = position.add(collidedPoint.sub(position).mul(0.003));
        return !block.registry().collisionShape().intersectBox(intersectPos.sub(collidedPoint), getBoundingBox());
    }

    private boolean shouldUnstuck() {
        Vec blockPosition = this.position.asVec().apply(Vec.Operator.FLOOR);
        return isFree(blockPosition.add(collisionSignum.x(), 0, 0))
                && isFree(blockPosition.add(0, collisionSignum.y(), 0))
                && isFree(blockPosition.add(0, 0, collisionSignum.z()));
    }

    @Override
    protected PhysicsResult handlePhysics(@NotNull Vec deltaPos) {
        // Handle block physics as if the bounding box didn't exist
        // Also only check for a single collision, because the velocity has to immediately be set to zero,
        // and not move the entity slightly on the axes that didn't collide
        //TODO improve
        final BoundingBox physicsBoundingBox = new BoundingBox(0, 0, 0);
        final PhysicsResult result = CollisionUtils.handlePhysics(this,
                deltaPos, physicsBoundingBox, true, lastPhysicsResult);

        final boolean stuck = result.collisionX() || result.collisionY() || result.collisionZ();
        if (stuck && !hasNoGravity()) {
            final Pos newPosition = result.newPosition();
            final Block block = instance.getBlock(newPosition);
            final var event = new ProjectileCollideWithBlockEvent(this, newPosition, block);
            EventDispatcher.callCancellable(event, () -> {
                setNoGravity(true);

                double signumX = result.collisionX() ? Math.signum(deltaPos.x()) : 0;
                double signumY = result.collisionY() ? Math.signum(deltaPos.y()) : 0;
                double signumZ = result.collisionZ() ? Math.signum(deltaPos.z()) : 0;
                this.collisionSignum = new Vec(signumX, signumY, signumZ);
            });
        }

        if (!stuck) {
            return result;
        } else {
            return new PhysicsResult(
                    result.newPosition(), Vec.ZERO, result.isOnGround(),
                    result.collisionX(), result.collisionY(), result.collisionZ(),
                    result.originalDelta(), result.collidedBlockY(), result.blockTypeY()
            );
        }
    }
}
