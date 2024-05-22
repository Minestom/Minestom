package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class MobMeta extends LivingEntityMeta {
    public static final byte OFFSET = LivingEntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    //Microtus start - update java keyword usage
    private static final byte NO_AI_BIT = 0x01;
    private static final byte IS_LEFT_HANDED_BIT = 0x02;
    private static final byte IS_AGGRESSIVE_BIT = 0x04;
    //Microtus end - update java keyword usage

    protected MobMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isNoAi() {
        return getMaskBit(OFFSET, NO_AI_BIT);
    }

    public void setNoAi(boolean value) {
        setMaskBit(OFFSET, NO_AI_BIT, value);
    }

    public boolean isLeftHanded() {
        return getMaskBit(OFFSET, IS_LEFT_HANDED_BIT);
    }

    public void setLeftHanded(boolean value) {
        setMaskBit(OFFSET, IS_LEFT_HANDED_BIT, value);
    }

    public boolean isAggressive() {
        return getMaskBit(OFFSET, IS_AGGRESSIVE_BIT);
    }

    public void setAggressive(boolean value) {
        setMaskBit(OFFSET, IS_AGGRESSIVE_BIT, value);
    }

}
