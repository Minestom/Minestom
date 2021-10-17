package net.minestom.server.entity;

import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that allows to instantiate entities with projectile-like physics handling.
 */
public class EntityProjectile extends Entity {

    private final Entity shooter;
    private final @Nullable Predicate<Entity> victimsPredicate;

    /**
     * Constructs new projectile.
     *
     * @param shooter shooter of the projectile: may be null.
     * @param entityType type of the projectile.
     * @param victimsPredicate if this projectile must not be able to hit entities, leave this null;
     *                         otherwise it's a predicate for those entities that may be hit by that projectile.
     */
    public EntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType, @Nullable Predicate<Entity> victimsPredicate) {
        super(entityType);
        this.shooter = shooter;
        this.victimsPredicate = victimsPredicate;
        setup();
    }

    /**
     * Constructs new projectile that can hit living entities.
     *
     * @param shooter shooter of the projectile: may be null.
     * @param entityType type of the projectile.
     */
    public EntityProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
        this(shooter, entityType, entity -> entity instanceof LivingEntity);
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

    /**
     * Called when this projectile hits an entity.
     * Probably you want to call {@link EntityDamageEvent} in such case.
     */
    public void onHit(Entity entity) {

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
        final Pos posBefore = getPosition();
        super.tick(time);
        final Pos posNow = getPosition();
        final State state = getState(posBefore, posNow);
        // TODO: swap to sealed switch
        if (state == State.Flying) {
            if (!super.onGround) {
                return;
            }
            super.onGround = false;
            setNoGravity(false);
            onUnstuck();
        } else if (state == State.StuckInBlock) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            this.velocity = Vec.ZERO;
            sendPacketToViewersAndSelf(getVelocityPacket());
            setNoGravity(true);
            onStuck();
        } else {
            onHit(((State.HitEntity) state).entity);
            remove();
        }
    }

    /**
     * Checks whether a projectile is stuck in block / hit an entity.
     *
     * @param pos    position right before current tick.
     * @param posNow position after current tick.
     * @return current state of the projectile.
     */
    @SuppressWarnings("ConstantConditions")
    private State getState(Pos pos, Pos posNow) {
        if (pos.samePoint(posNow)) {
            return State.StuckInBlock;
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
            if (instance.getBlock(pos).isSolid()) {
                teleport(pos);
                return State.StuckInBlock;
            }
            if (victimsPredicate == null) {
                continue;
            }
            Chunk currentChunk = instance.getChunkAt(pos);
            if (currentChunk != chunk) {
                chunk = currentChunk;
                entities = instance.getChunkEntities(chunk)
                        .stream()
                        .filter(victimsPredicate)
                        .collect(Collectors.toSet());
            }

            final Pos finalPos = pos;
            Stream<Entity> victims = entities.stream().filter(entity -> entity.getBoundingBox().intersect(finalPos));

            /*
              We won't check collisions with self for first ticks of projectile's life, because it spawns in the
              shooter and will immediately be triggered by him.
             */
            if (getAliveTicks() < 3) {
                victims = victims.filter(entity -> entity != getShooter());
            }
            Optional<Entity> victim = victims.findAny();
            if (victim.isPresent()) {
                return new State.HitEntity(victim.get());
            }
        }
        return State.Flying;
    }

    // TODO: swap to sealed
    private interface State {
        State Flying = new State() {};
        State StuckInBlock = new State() {};

        final class HitEntity implements State {

            private final Entity entity;

            public HitEntity(Entity entity) {
                this.entity = entity;
            }
        }
    }
}
