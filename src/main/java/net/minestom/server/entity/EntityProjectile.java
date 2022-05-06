package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that allows to instantiate entities with projectile-like physics handling.
 */
public class EntityProjectile extends Entity {

    private final Entity shooter;

    public EntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
        super(entityType);
        this.shooter = shooter;
        setup();
    }

    private void setup() {
        super.hasPhysics = false;
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
        final Pos posBefore = getPosition();
        super.tick(time);
        final Pos posNow = getPosition();
        if (isStuck(posBefore, posNow)) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            this.velocity = Vec.ZERO;
            sendPacketToViewersAndSelf(getVelocityPacket());
            setNoGravity(true);
        } else {
            if (!super.onGround) {
                return;
            }
            super.onGround = false;
            setNoGravity(false);
            EventDispatcher.call(new ProjectileUncollideEvent(this));
        }
    }

    /**
     * Checks whether an arrow is stuck in block / hit an entity.
     *
     * @param pos    position right before current tick.
     * @param posNow position after current tick.
     * @return if an arrow is stuck in block / hit an entity.
     */
    @SuppressWarnings("ConstantConditions")
    private boolean isStuck(Pos pos, Pos posNow) {
        final Instance instance = getInstance();
        if (pos.samePoint(posNow)) {
            return instance.getBlock(pos).isSolid();
        }

        Chunk chunk = null;
        Collection<LivingEntity> entities = null;
        final BoundingBox bb = getBoundingBox();

        /*
          What we're about to do is to discretely jump from a previous position to the new one.
          For each point we will be checking blocks and entities we're in.
         */
        final double part = bb.width() / 2;
        final Vec dir = posNow.sub(pos).asVec();
        final int parts = (int) Math.ceil(dir.length() / part);
        final Pos direction = dir.normalize().mul(part).asPosition();
        final long aliveTicks = getAliveTicks();
        Block block = null;
        Point blockPos = null;
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed the end point.
            pos = (i == parts - 1) ? posNow : pos.add(direction);
            if (block == null || !pos.sameBlock(blockPos)) {
                block = instance.getBlock(pos);
                blockPos = pos;
            }
            if (block.isSolid()) {
                final ProjectileCollideWithBlockEvent event = new ProjectileCollideWithBlockEvent(this, pos, block);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    teleport(pos);
                    return true;
                }
            }
            if (currentChunk != chunk) {
                chunk = currentChunk;
                entities = instance.getChunkEntities(chunk)
                        .stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity) entity)
                        .collect(Collectors.toSet());
            }
            final Point currentPos = pos;
            Stream<LivingEntity> victimsStream = entities.stream()
                    .filter(entity -> bb.intersectEntity(currentPos, entity));
            /*
              We won't check collisions with a shooter for first ticks of arrow's life, because it spawns in him
              and will immediately deal damage.
             */
            if (aliveTicks < 3 && shooter != null) {
                victimsStream = victimsStream.filter(entity -> entity != shooter);
            }
            final Optional<LivingEntity> victimOptional = victimsStream.findAny();
            if (victimOptional.isPresent()) {
                final LivingEntity target = victimOptional.get();
                final ProjectileCollideWithEntityEvent event = new ProjectileCollideWithEntityEvent(this, pos, target);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    return super.onGround;
                }
            }
        }
        return false;
    }
}
