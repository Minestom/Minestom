package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class VexMeta extends MonsterMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte ATTACKING_BIT = 0x01;

    public VexMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isAttacking() {
        return getMaskBit(MASK_INDEX, ATTACKING_BIT);
    }

    public void setAttacking(boolean value) {
        setMaskBit(MASK_INDEX, ATTACKING_BIT, value);
    }

}
