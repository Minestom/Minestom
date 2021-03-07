package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
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

    @Deprecated
    public EntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        this.shooter = shooter;
        setup();
    }

    private void setup() {
        super.hasPhysics = false;
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(this.shooter);
        }
        setGravity(0.02f, 0.04f, 1.96f);
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

    public void shoot(Position to, double power, double spread) {
        EntityShootEvent event = new EntityShootEvent(this.shooter, this, to, power, spread);
        this.shooter.callEvent(EntityShootEvent.class, event);
        if (event.isCancelled()) {
            remove();
            return;
        }
        Position from = this.shooter.getPosition().clone().add(0D, this.shooter.getEyeHeight(), 0D);
        shoot(from, to, event.getPower(), event.getSpread());
    }

    private void shoot(@NotNull Position from, @NotNull Position to, double power, double spread) {
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
        super.velocity.setX(dx);
        super.velocity.setY(dy);
        super.velocity.setZ(dz);
        super.velocity.multiply(20 * power);
        setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    @Override
    public void tick(long time) {
        Position posBefore = getPosition().clone();
        super.tick(time);
        Position posNow = getPosition().clone();
        if (isStuck(posBefore, posNow)) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            getVelocity().zero();
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
    private boolean isStuck(Position pos, Position posNow) {
        if (pos.isSimilar(posNow)) {
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
        Vector dir = posNow.toVector().subtract(pos.toVector());
        int parts = (int) Math.ceil(dir.length() / part);
        Position direction = dir.normalize().multiply(part).toPosition();
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed end point.
            if (i == parts - 1) {
                pos.setX(posNow.getX());
                pos.setY(posNow.getY());
                pos.setZ(posNow.getZ());
            } else {
                pos.add(direction);
            }
            BlockPosition bpos = pos.toBlockPosition();
            Block block = instance.getBlock(bpos.getX(), bpos.getY() - 1, bpos.getZ());
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
            Optional<Entity> victimOptional = entities.stream()
                    .filter(entity -> entity.getBoundingBox().intersect(pos.getX(), pos.getY(), pos.getZ()))
                    .findAny();
            if (victimOptional.isPresent()) {
                LivingEntity victim = (LivingEntity) victimOptional.get();
                victim.setArrowCount(victim.getArrowCount() + 1);
                callEvent(EntityAttackEvent.class, new EntityAttackEvent(this, victim));
                remove();
                return super.onGround;
            }
        }
        return false;
    }

}
