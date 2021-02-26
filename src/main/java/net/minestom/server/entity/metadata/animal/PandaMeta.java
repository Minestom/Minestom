package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PandaMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 21;

    private final static byte SNEEZING_BIT = 0x02;
    private final static byte ROLLING_BIT = 0x04;
    private final static byte SITTING_BIT = 0x08;
    private final static byte ON_BACK_BIT = 0x10;

    public PandaMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getBreedTimer() {
        return super.metadata.getIndex((byte) 16, 0);
    }

    public void setBreedTimer(int value) {
        super.metadata.setIndex((byte) 16, Metadata.VarInt(value));
    }

    public int getSneezeTimer() {
        return super.metadata.getIndex((byte) 17, 0);
    }

    public void setSneezeTimer(int value) {
        super.metadata.setIndex((byte) 17, Metadata.VarInt(value));
    }

    public int getEatTimer() {
        return super.metadata.getIndex((byte) 18, 0);
    }

    public void setEatTimer(int value) {
        super.metadata.setIndex((byte) 18, Metadata.VarInt(value));
    }

    @NotNull
    public Gene getMainGene() {
        return Gene.VALUES[super.metadata.getIndex((byte) 19, (byte) 0)];
    }

    public void setMainGene(@NotNull Gene value) {
        super.metadata.setIndex((byte) 19, Metadata.Byte((byte) value.ordinal()));
    }

    @NotNull
    public Gene getHiddenGene() {
        return Gene.VALUES[super.metadata.getIndex((byte) 20, (byte) 0)];
    }

    public void setHiddenGene(@NotNull Gene value) {
        super.metadata.setIndex((byte) 20, Metadata.Byte((byte) value.ordinal()));
    }

    public boolean isSneezing() {
        return getMaskBit(MASK_INDEX, SNEEZING_BIT);
    }

    public void setSneezing(boolean value) {
        setMaskBit(MASK_INDEX, SNEEZING_BIT, value);
    }

    public boolean isRolling() {
        return getMaskBit(MASK_INDEX, ROLLING_BIT);
    }

    public void setRolling(boolean value) {
        setMaskBit(MASK_INDEX, ROLLING_BIT, value);
    }

    public boolean isSitting() {
        return getMaskBit(MASK_INDEX, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(MASK_INDEX, SITTING_BIT, value);
    }

    public boolean isOnBack() {
        return getMaskBit(MASK_INDEX, ON_BACK_BIT);
    }

    public void setOnBack(boolean value) {
        setMaskBit(MASK_INDEX, ON_BACK_BIT, value);
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
