package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class VexMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final static byte ATTACKING_BIT = 0x01;

    public VexMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return getMaskBit(OFFSET, ATTACKING_BIT);
    }

    public void setAttacking(boolean value) {
        setMaskBit(OFFSET, ATTACKING_BIT, value);
    }

}
