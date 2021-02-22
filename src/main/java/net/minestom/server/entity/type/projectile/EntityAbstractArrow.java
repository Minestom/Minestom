package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 22.02.2021
 */
public class EntityAbstractArrow extends ObjectEntity implements Projectile {

    private final static byte CRITICAL_BIT = 0x01;
    private final static byte NO_CLIP_BIT  = 0x02;

    private final int shooterID;

    EntityAbstractArrow(@Nullable Entity shooter, @NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        this.shooterID = shooter == null ? 0 : shooter.getEntityId();
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
        return this.shooterID + 1;
    }

}
