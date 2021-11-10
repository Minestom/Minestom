package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AbstractHorseMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    private final static byte TAMED_BIT = 0x02;
    private final static byte SADDLED_BIT = 0x04;
    private final static byte HAS_BRED_BIT = 0x08;
    private final static byte EATING_BIT = 0x10;
    private final static byte REARING_BIT = 0x20;
    private final static byte MOUTH_OPEN_BIT = 0x40;

    protected AbstractHorseMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isTamed() {
        return getMaskBit(OFFSET, TAMED_BIT);
    }

    public void setTamed(boolean value) {
        setMaskBit(OFFSET, TAMED_BIT, value);
    }

    public boolean isSaddled() {
        return getMaskBit(OFFSET, SADDLED_BIT);
    }

    public void setSaddled(boolean value) {
        setMaskBit(OFFSET, SADDLED_BIT, value);
    }

    public boolean isHasBred() {
        return getMaskBit(OFFSET, HAS_BRED_BIT);
    }

    public void setHasBred(boolean value) {
        setMaskBit(OFFSET, HAS_BRED_BIT, value);
    }

    public boolean isEating() {
        return getMaskBit(OFFSET, EATING_BIT);
    }

    public void setEating(boolean value) {
        setMaskBit(OFFSET, EATING_BIT, value);
    }

    public boolean isRearing() {
        return getMaskBit(OFFSET, REARING_BIT);
    }

    public void setRearing(boolean value) {
        setMaskBit(OFFSET, REARING_BIT, value);
    }

    public boolean isMouthOpen() {
        return getMaskBit(OFFSET, MOUTH_OPEN_BIT);
    }

    public void setMouthOpen(boolean value) {
        setMaskBit(OFFSET, MOUTH_OPEN_BIT, value);
    }

    public UUID getOwner() {
        return super.metadata.getIndex(OFFSET + 1, null);
    }

    public void setOwner(UUID value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.OptUUID(value));
    }

}
