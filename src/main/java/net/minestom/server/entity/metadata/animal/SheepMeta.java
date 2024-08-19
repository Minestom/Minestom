package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SheepMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    //Microtus start - update java keyword usage
    private static final byte COLOR_BITS = 0x0F;
    private static final byte SHEARED_BIT = 0x10;
    //Microtus end - update java keyword usage

    public SheepMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return getMask(OFFSET) & COLOR_BITS;
    }

    public void setColor(byte color) {
        byte before = getMask(OFFSET);
        byte mask = before;
        mask &= ~COLOR_BITS;
        mask |= (color & COLOR_BITS);
        if (mask != before) {
            setMask(OFFSET, mask);
        }
    }

    public boolean isSheared() {
        return getMaskBit(OFFSET, SHEARED_BIT);
    }

    public void setSheared(boolean value) {
        setMaskBit(OFFSET, SHEARED_BIT, value);
    }

}
