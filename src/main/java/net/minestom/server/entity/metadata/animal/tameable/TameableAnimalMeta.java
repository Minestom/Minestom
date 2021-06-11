package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TameableAnimalMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    private final static byte SITTING_BIT = 0x01;
    private final static byte TAMED_BIT = 0x04;

    protected TameableAnimalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isSitting() {
        return getMaskBit(OFFSET, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(OFFSET, SITTING_BIT, value);
    }

    public boolean isTamed() {
        return getMaskBit(OFFSET, TAMED_BIT);
    }

    public void setTamed(boolean value) {
        setMaskBit(OFFSET, TAMED_BIT, value);
    }

    @NotNull
    public UUID getOwner() {
        return super.metadata.getIndex(OFFSET + 1, null);
    }

    public void setOwner(@NotNull UUID value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.OptUUID(value));
    }

}
