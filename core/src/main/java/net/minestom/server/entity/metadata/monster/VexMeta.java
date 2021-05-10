package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class VexMeta extends MonsterMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte ATTACKING_BIT = 0x01;

    public VexMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return getMaskBit(MASK_INDEX, ATTACKING_BIT);
    }

    public void setAttacking(boolean value) {
        setMaskBit(MASK_INDEX, ATTACKING_BIT, value);
    }

}
