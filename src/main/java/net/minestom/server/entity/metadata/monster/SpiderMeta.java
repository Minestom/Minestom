package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SpiderMeta extends MonsterMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte CLIMBING_BIT = 0x01;

    public SpiderMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isClimbing() {
        return getMaskBit(MASK_INDEX, CLIMBING_BIT);
    }

    public void setClimbing(boolean value) {
        setMaskBit(MASK_INDEX, CLIMBING_BIT, value);
    }

}
