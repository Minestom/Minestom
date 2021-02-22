package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.type.Projectile;
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

public class EntityAbstractArrow extends ObjectEntity implements Projectile {

    private final static byte CRITICAL_BIT = 0x01;
    private final static byte NO_CLIP_BIT  = 0x02;

    private final Entity shooter;

    EntityAbstractArrow(@Nullable Entity shooter, @NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        this.shooter = shooter;
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

    @SuppressWarnings("ConstantConditions")
    private boolean isStuck(Position pos, Position posNow) {
        if (pos.isSimilar(posNow)) {
            return true;
        }

        Instance           instance = getInstance();
        Chunk              chunk    = null;
        Collection<Entity> entities = null;

        double   part      = .25D; // half of the bounding box
        Vector   dir       = posNow.toVector().subtract(pos.toVector());
        int      parts     = (int) Math.ceil(dir.length() / part);
        Position direction = dir.normalize().multiply(part).toPosition();
        for (int i = 0; i < parts; ++i) {
            if (i == parts - 1) {
                pos.setX(posNow.getX());
                pos.setY(posNow.getY());
                pos.setZ(posNow.getZ());
            } else {
                pos.add(direction);
            }
            BlockPosition bpos  = pos.toBlockPosition();
            Block         block = getInstance().getBlock(bpos.getX(), bpos.getY() - 1, bpos.getZ());
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
            Optional<Entity> victimOptional = entities.stream()
                    .filter(entity -> entity.getBoundingBox().intersect(pos.getX(), pos.getY(), pos.getZ()))
                    .findAny();
            if (victimOptional.isPresent()) {
                LivingEntity victim = (LivingEntity) victimOptional.get();
                victim.setArrowCount(victim.getArrowCount() + 1);
                victim.damage(DamageType.fromProjectile(this.shooter, this), 2F);
                remove();
                return super.onGround;
            }
        }
        return false;
    }

    public void setCritical(boolean value) {
        modifyMask(CRITICAL_BIT, value);
    }

    public boolean isCritical() {
        return (getMask() & CRITICAL_BIT) != 0;
    }

    public void setNoClip(boolean value) {
        modifyMask(NO_CLIP_BIT, value);
    }

    public boolean isNoClip() {
        return (getMask() & NO_CLIP_BIT) != 0;
    }

    public void setPiercingLevel(byte value) {
        this.metadata.setIndex((byte) 8, Metadata.Byte(value));
    }

    public byte getPiercingLevel() {
        return this.metadata.getIndex((byte) 8, (byte) 0);
    }

    private byte getMask() {
        return this.metadata.getIndex((byte) 7, (byte) 0);
    }

    private void setMask(byte mask) {
        this.metadata.setIndex((byte) 7, Metadata.Byte(mask));
    }

    private void modifyMask(byte bit, boolean value) {
        byte    mask      = getMask();
        boolean isPresent = (mask & bit) == bit;
        if (isPresent == value) {
            return;
        }
        if (value) {
            mask |= bit;
        } else {
            mask &= ~bit;
        }
        setMask(mask);
    }

    @Override
    public int getObjectData() {
        return this.shooter == null ? 0 : this.shooter.getEntityId() + 1;
    }

}
