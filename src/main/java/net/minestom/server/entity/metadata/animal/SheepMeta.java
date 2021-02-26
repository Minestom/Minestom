package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SheepMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 16;

    private final static byte COLOR_BITS = 0x0F;
    private final static byte SHEARED_BIT = 0x10;

    public SheepMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return getMask(MASK_INDEX) & COLOR_BITS;
    }

    public void setColor(byte color) {
        byte before = getMask(MASK_INDEX);
        byte mask = before;
        mask &= ~COLOR_BITS;
        mask |= (color & COLOR_BITS);
        if (mask != before) {
            setMask(MASK_INDEX, mask);
        }
    }

    public boolean isSheared() {
        return getMaskBit(MASK_INDEX, SHEARED_BIT);
    }

    public void setSheared(boolean value) {
        setMaskBit(MASK_INDEX, SHEARED_BIT, value);
    }

}
