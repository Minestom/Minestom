package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SpiderMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final static byte CLIMBING_BIT = 0x01;

    public SpiderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isClimbing() {
        return getMaskBit(OFFSET, CLIMBING_BIT);
    }

    public void setClimbing(boolean value) {
        setMaskBit(OFFSET, CLIMBING_BIT, value);
    }

}
