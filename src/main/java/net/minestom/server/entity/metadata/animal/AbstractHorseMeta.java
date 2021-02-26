package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AbstractHorseMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 16;

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
        return getMaskBit(MASK_INDEX, TAMED_BIT);
    }

    public void setTamed(boolean value) {
        setMaskBit(MASK_INDEX, TAMED_BIT, value);
    }

    public boolean isSaddled() {
        return getMaskBit(MASK_INDEX, SADDLED_BIT);
    }

    public void setSaddled(boolean value) {
        setMaskBit(MASK_INDEX, SADDLED_BIT, value);
    }

    public boolean isHasBred() {
        return getMaskBit(MASK_INDEX, HAS_BRED_BIT);
    }

    public void setHasBred(boolean value) {
        setMaskBit(MASK_INDEX, HAS_BRED_BIT, value);
    }

    public boolean isEating() {
        return getMaskBit(MASK_INDEX, EATING_BIT);
    }

    public void setEating(boolean value) {
        setMaskBit(MASK_INDEX, EATING_BIT, value);
    }

    public boolean isRearing() {
        return getMaskBit(MASK_INDEX, REARING_BIT);
    }

    public void setRearing(boolean value) {
        setMaskBit(MASK_INDEX, REARING_BIT, value);
    }

    public boolean isMouthOpen() {
        return getMaskBit(MASK_INDEX, MOUTH_OPEN_BIT);
    }

    public void setMouthOpen(boolean value) {
        setMaskBit(MASK_INDEX, MOUTH_OPEN_BIT, value);
    }

    public UUID getOwner() {
        return super.metadata.getIndex((byte) 17, null);
    }

    public void setOwner(UUID value) {
        super.metadata.setIndex((byte) 17, Metadata.OptUUID(value));
    }

}
