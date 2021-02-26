package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class FoxMeta extends AnimalMeta {

    private final static byte MASK_INDEX = 17;

    private final static byte SITTING_BIT = 0x01;
    private final static byte CROUCHING_BIT = 0x04;
    private final static byte INTERESTED_BIT = 0x08;
    private final static byte POUNCING_BIT = 0x10;
    private final static byte SLEEPING_BIT = 0x20;
    private final static byte FACEPLANTED_BIT = 0x40;
    private final static byte DEFENDING_BIT = (byte) 0x80;

    public FoxMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[super.metadata.getIndex((byte) 16, 0)];
    }

    public void setType(@NotNull Type type) {
        super.metadata.setIndex((byte) 16, Metadata.VarInt(type.ordinal()));
    }

    public boolean isSitting() {
        return getMaskBit(MASK_INDEX, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(MASK_INDEX, SITTING_BIT, value);
    }

    public boolean isFoxSneaking() {
        return getMaskBit(MASK_INDEX, CROUCHING_BIT);
    }

    public void setFoxSneaking(boolean value) {
        setMaskBit(MASK_INDEX, CROUCHING_BIT, value);
    }

    public boolean isInterested() {
        return getMaskBit(MASK_INDEX, INTERESTED_BIT);
    }

    public void setInterested(boolean value) {
        setMaskBit(MASK_INDEX, INTERESTED_BIT, value);
    }

    public boolean isPouncing() {
        return getMaskBit(MASK_INDEX, POUNCING_BIT);
    }

    public void setPouncing(boolean value) {
        setMaskBit(MASK_INDEX, POUNCING_BIT, value);
    }

    public boolean isSleeping() {
        return getMaskBit(MASK_INDEX, SLEEPING_BIT);
    }

    public void setSleeping(boolean value) {
        setMaskBit(MASK_INDEX, SLEEPING_BIT, value);
    }

    public boolean isFaceplanted() {
        return getMaskBit(MASK_INDEX, FACEPLANTED_BIT);
    }

    public void setFaceplanted(boolean value) {
        setMaskBit(MASK_INDEX, FACEPLANTED_BIT, value);
    }

    public boolean isDefending() {
        return getMaskBit(MASK_INDEX, DEFENDING_BIT);
    }

    public void setDefending(boolean value) {
        setMaskBit(MASK_INDEX, DEFENDING_BIT, value);
    }

    @Nullable
    public UUID getFirstUUID() {
        return super.metadata.getIndex((byte) 18, null);
    }

    public void setFirstUUID(@Nullable UUID value) {
        super.metadata.setIndex((byte) 18, Metadata.OptUUID(value));
    }

    @Nullable
    public UUID getSecondUUID() {
        return super.metadata.getIndex((byte) 19, null);
    }

    public void setSecondUUID(@Nullable UUID value) {
        super.metadata.setIndex((byte) 19, Metadata.OptUUID(value));
    }

    public enum Type {
        RED,
        SNOW;

        private final static Type[] VALUES = values();
    }

}
