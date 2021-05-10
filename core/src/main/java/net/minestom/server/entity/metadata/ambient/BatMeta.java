package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BatMeta extends AmbientCreatureMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte IS_HANGING_BIT = 0x01;

    public BatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHanging() {
        return getMaskBit(MASK_INDEX, IS_HANGING_BIT);
    }

    public void setHanging(boolean value) {
        setMaskBit(MASK_INDEX, IS_HANGING_BIT, value);
    }

}
