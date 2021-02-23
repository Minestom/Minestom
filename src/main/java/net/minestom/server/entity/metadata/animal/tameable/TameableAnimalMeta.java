package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TameableAnimalMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 16;

    private final static byte SITTING_BIT = 0x01;
    private final static byte TAMED_BIT = 0x04;

    protected TameableAnimalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isSitting() {
        return getMaskBit(MASK_INDEX, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(MASK_INDEX, SITTING_BIT, value);
    }

    public boolean isTamed() {
        return getMaskBit(MASK_INDEX, TAMED_BIT);
    }

    public void setTamed(boolean value) {
        setMaskBit(MASK_INDEX, TAMED_BIT, value);
    }

    @NotNull
    public UUID getOwner() {
        return super.metadata.getIndex((byte) 17, null);
    }

    public void setOwner(@NotNull UUID value) {
        super.metadata.setIndex((byte) 17, Metadata.OptUUID(value));
    }

}
