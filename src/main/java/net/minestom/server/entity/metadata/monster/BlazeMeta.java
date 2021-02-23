package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BlazeMeta extends MonsterMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte ON_FIRE_BIT = 0x01;

    public BlazeMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isOnFire() {
        return getMaskBit(MASK_INDEX, ON_FIRE_BIT);
    }

    public void setOnFire(boolean value) {
        setMaskBit(MASK_INDEX, ON_FIRE_BIT, value);
    }

}
