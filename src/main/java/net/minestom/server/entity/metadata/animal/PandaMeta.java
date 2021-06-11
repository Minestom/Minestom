package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PandaMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 6;

    private final static byte SNEEZING_BIT = 0x02;
    private final static byte ROLLING_BIT = 0x04;
    private final static byte SITTING_BIT = 0x08;
    private final static byte ON_BACK_BIT = 0x10;

    public PandaMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getBreedTimer() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setBreedTimer(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getSneezeTimer() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    public void setSneezeTimer(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public int getEatTimer() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setEatTimer(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
    }

    @NotNull
    public Gene getMainGene() {
        return Gene.VALUES[super.metadata.getIndex(OFFSET + 3, (byte) 0)];
    }

    public void setMainGene(@NotNull Gene value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Byte((byte) value.ordinal()));
    }

    @NotNull
    public Gene getHiddenGene() {
        return Gene.VALUES[super.metadata.getIndex(OFFSET + 4, (byte) 0)];
    }

    public void setHiddenGene(@NotNull Gene value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Byte((byte) value.ordinal()));
    }

    public boolean isSneezing() {
        return getMaskBit(OFFSET + 5, SNEEZING_BIT);
    }

    public void setSneezing(boolean value) {
        setMaskBit(OFFSET + 5, SNEEZING_BIT, value);
    }

    public boolean isRolling() {
        return getMaskBit(OFFSET + 5, ROLLING_BIT);
    }

    public void setRolling(boolean value) {
        setMaskBit(OFFSET + 5, ROLLING_BIT, value);
    }

    public boolean isSitting() {
        return getMaskBit(OFFSET + 5, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(OFFSET + 5, SITTING_BIT, value);
    }

    public boolean isOnBack() {
        return getMaskBit(OFFSET + 5, ON_BACK_BIT);
    }

    public void setOnBack(boolean value) {
        setMaskBit(OFFSET + 5, ON_BACK_BIT, value);
    }

    public enum Gene {
        NORMAL,
        AGGRESSIVE,
        LAZY,
        WORRIED,
        PLAYFUL,
        WEAK,
        BROWN;

        private final static Gene[] VALUES = values();
    }

}
