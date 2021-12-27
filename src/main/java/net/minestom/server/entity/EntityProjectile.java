package net.minestom.server.entity;

import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    /**
     * Called when this projectile is stuck in blocks.
     * Probably you want to do nothing with arrows in such case and to remove other types of projectiles.
     */
    public void onStuck() {

    }

    /**
     * Called when this projectile unstucks.
     * Probably you want to add some random velocity to arrows in such case.
     */
    public void onUnstuck() {

    }

    public void shoot(Point to, double power, double spread) {
        EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, to, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }
        final var from = this.shooter.getPosition().add(0D, this.shooter.getEyeHeight(), 0D);
        shoot(from, to, shootEvent.getPower(), shootEvent.getSpread());
    }

    private void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
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

        final double mul = 20 * power;
        this.velocity = new Vec(dx * mul, dy * mul, dz * mul);
        setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    @Override
    public void tick(long time) {
        final var posBefore = getPosition();
        super.tick(time);
        final var posNow = getPosition();
        if (isStuck(posBefore, posNow)) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            this.velocity = Vec.ZERO;
            sendPacketToViewersAndSelf(getVelocityPacket());
            setNoGravity(true);
            onStuck();
        } else {
            if (!super.onGround) {
                return;
            }
            super.onGround = false;
            setNoGravity(false);
            onUnstuck();
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
        if (pos.samePoint(posNow)) {
            return true;
        }

        Instance instance = getInstance();
        Chunk chunk = null;
        Collection<Entity> entities = null;

        /*
          What we're about to do is to discretely jump from the previous position to the new one.
          For each point we will be checking blocks and entities we're in.
         */
        double part = .25D; // half of the bounding box
        final var dir = posNow.sub(pos).asVec();
        int parts = (int) Math.ceil(dir.length() / part);
        final var direction = dir.normalize().mul(part).asPosition();
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed end point.
            if (i == parts - 1) {
                pos = posNow;
            } else {
                pos = pos.add(direction);
            }
            Block block = instance.getBlock(pos);
            if (!block.isAir() && !block.isLiquid()) {
                teleport(pos);
                return true;
            }
            Chunk currentChunk = instance.getChunkAt(pos);
            if (currentChunk != chunk) {
                chunk = currentChunk;
                entities = instance.getChunkEntities(chunk)
                        .stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .collect(Collectors.toSet());
            }
            /*
              We won't check collisions with entities for first ticks of arrow's life, because it spawns in the
              shooter and will immediately damage him.
             */
            if (getAliveTicks() < 3) {
                continue;
            }
            final Pos finalPos = pos;
            Optional<Entity> victimOptional = entities.stream()
                    .filter(entity -> entity.getBoundingBox().intersect(finalPos))
                    .findAny();
            if (victimOptional.isPresent()) {
                LivingEntity victim = (LivingEntity) victimOptional.get();
                if(entityType == EntityTypes.ARROW || entityType == EntityTypes.SPECTRAL_ARROW) {
                    victim.setArrowCount(victim.getArrowCount() + 1);
                }
                EventDispatcher.call(new EntityAttackEvent(this, victim));
                remove();
                return super.onGround;
            }
        }
        return false;
    }
}
