package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class MobMeta extends LivingEntityMeta {

    private final static byte MASK_INDEX = 14;

    private final static byte NO_AI_BIT = 0x01;
    private final static byte IS_LEFT_HANDED_BIT = 0x02;
    private final static byte IS_AGGRESSIVE_BIT = 0x04;

    protected MobMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isNoAi() {
        return getMaskBit(MASK_INDEX, NO_AI_BIT);
    }

    public void setNoAi(boolean value) {
        setMaskBit(MASK_INDEX, NO_AI_BIT, value);
    }

    public boolean isLeftHanded() {
        return getMaskBit(MASK_INDEX, IS_LEFT_HANDED_BIT);
    }

    public void setLeftHanded(boolean value) {
        setMaskBit(MASK_INDEX, IS_LEFT_HANDED_BIT, value);
    }

    public boolean isAggressive() {
        return getMaskBit(MASK_INDEX, IS_AGGRESSIVE_BIT);
    }

    public void setAggressive(boolean value) {
        setMaskBit(MASK_INDEX, IS_AGGRESSIVE_BIT, value);
    }

}
