package net.minestom.server.entity.metadata.arrow;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractArrowMeta extends EntityMeta {

    private final static byte MASK_INDEX = 7;

    private final static byte CRITICAL_BIT = 0x01;
    private final static byte NO_CLIP_BIT = 0x01;

    protected AbstractArrowMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isCritical() {
        return getMaskBit(MASK_INDEX, CRITICAL_BIT);
    }

    public void setCritical(boolean value) {
        setMaskBit(MASK_INDEX, CRITICAL_BIT, value);
    }

    public boolean isNoClip() {
        return getMaskBit(MASK_INDEX, NO_CLIP_BIT);
    }

    public void setNoClip(boolean value) {
        setMaskBit(MASK_INDEX, NO_CLIP_BIT, value);
    }

    public byte getPiercingLevel() {
        return super.metadata.getIndex((byte) 8, (byte) 0);
    }

    public void setPiercingLevel(byte value) {
        super.metadata.setIndex((byte) 8, Metadata.Byte(value));
    }

}
