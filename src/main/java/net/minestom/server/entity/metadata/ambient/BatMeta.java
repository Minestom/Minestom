package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class BatMeta extends AmbientCreatureMeta {
    public static final byte OFFSET = AmbientCreatureMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    private static final byte IS_HANGING_BIT = 0x01; //Microtus - update java keyword usage

    public BatMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHanging() {
        return getMaskBit(OFFSET, IS_HANGING_BIT);
    }

    public void setHanging(boolean value) {
        setMaskBit(OFFSET, IS_HANGING_BIT, value);
    }

}
