package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BeeMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 16;

    private final static byte ANGRY_BIT = 0x02;
    private final static byte HAS_STUNG_BIT = 0x04;
    private final static byte HAS_NECTAR_BIT = 0x08;

    public BeeMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isAngry() {
        return getMaskBit(MASK_INDEX, ANGRY_BIT);
    }

    public void setAngry(boolean value) {
        setMaskBit(MASK_INDEX, ANGRY_BIT, value);
    }

    public boolean isHasStung() {
        return getMaskBit(MASK_INDEX, HAS_STUNG_BIT);
    }

    public void setHasStung(boolean value) {
        setMaskBit(MASK_INDEX, HAS_STUNG_BIT, value);
    }

    public boolean isHasNectar() {
        return getMaskBit(MASK_INDEX, HAS_NECTAR_BIT);
    }

    public void setHasNectar(boolean value) {
        setMaskBit(MASK_INDEX, HAS_NECTAR_BIT, value);
    }

    public int getAngerTicks() {
        return super.metadata.getIndex((byte) 17, 0);
    }

    public void setAngerTicks(int value) {
        super.metadata.setIndex((byte) 17, Metadata.VarInt(value));
    }

}
