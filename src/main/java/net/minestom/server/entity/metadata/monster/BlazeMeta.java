package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BlazeMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;
    private static final byte ON_FIRE_BIT = 0x01; //Microtus - update java keyword usage

    public BlazeMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isOnFire() {
        return getMaskBit(OFFSET, ON_FIRE_BIT);
    }

    public void setOnFire(boolean value) {
        setMaskBit(OFFSET, ON_FIRE_BIT, value);
    }

}
