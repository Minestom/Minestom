package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class AbstractHorseMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    //Microtus start - update java keyword usage
    private static final byte TAMED_BIT = 0x02;
    private static final byte SADDLED_BIT = 0x04;
    private static final byte HAS_BRED_BIT = 0x08;
    private static final byte EATING_BIT = 0x10;
    private static final byte REARING_BIT = 0x20;
    private static final byte MOUTH_OPEN_BIT = 0x40;
    //Microtus end - update java keyword usage

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

}
