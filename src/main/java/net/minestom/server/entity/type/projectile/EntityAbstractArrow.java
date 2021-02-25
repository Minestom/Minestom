package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.event.entity.EntityAttackEvent;
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
import java.util.stream.Collectors;

public class EntityAbstractArrow extends AbstractProjectile {

    EntityAbstractArrow(@Nullable Entity shooter, @NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(shooter, entityType, spawnPosition);
        super.hasPhysics = false;

        setBoundingBox(.5F, .5F, .5F);
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
        } else {
            if (!super.onGround) {
                return;
            }
            super.onGround = false;
            setNoGravity(false);
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
