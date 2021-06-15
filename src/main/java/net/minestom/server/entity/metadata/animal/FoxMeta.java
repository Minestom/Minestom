package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FoxMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

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
        return Type.VALUES[super.metadata.getIndex(OFFSET, 0)];
    }

    public void setType(@NotNull Type type) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(type.ordinal()));
    }

    public boolean isSitting() {
        return getMaskBit(OFFSET + 1, SITTING_BIT);
    }

    public void setSitting(boolean value) {
        setMaskBit(OFFSET + 1, SITTING_BIT, value);
    }

    public boolean isFoxSneaking() {
        return getMaskBit(OFFSET + 1, CROUCHING_BIT);
    }

    public void setFoxSneaking(boolean value) {
        setMaskBit(OFFSET + 1, CROUCHING_BIT, value);
    }

    public boolean isInterested() {
        return getMaskBit(OFFSET + 1, INTERESTED_BIT);
    }

    public void setInterested(boolean value) {
        setMaskBit(OFFSET + 1, INTERESTED_BIT, value);
    }

    public boolean isPouncing() {
        return getMaskBit(OFFSET + 1, POUNCING_BIT);
    }

    public void setPouncing(boolean value) {
        setMaskBit(OFFSET + 1, POUNCING_BIT, value);
    }

    public boolean isSleeping() {
        return getMaskBit(OFFSET + 1, SLEEPING_BIT);
    }

    public void setSleeping(boolean value) {
        setMaskBit(OFFSET + 1, SLEEPING_BIT, value);
    }

    public boolean isFaceplanted() {
        return getMaskBit(OFFSET + 1, FACEPLANTED_BIT);
    }

    public void setFaceplanted(boolean value) {
        setMaskBit(OFFSET + 1, FACEPLANTED_BIT, value);
    }

    public boolean isDefending() {
        return getMaskBit(OFFSET + 1, DEFENDING_BIT);
    }

    public void setDefending(boolean value) {
        setMaskBit(OFFSET + 1, DEFENDING_BIT, value);
    }

    @Nullable
    public UUID getFirstUUID() {
        return super.metadata.getIndex(OFFSET + 2, null);
    }

    public void setFirstUUID(@Nullable UUID value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.OptUUID(value));
    }

    @Nullable
    public UUID getSecondUUID() {
        return super.metadata.getIndex(OFFSET + 3, null);
    }

    public void setSecondUUID(@Nullable UUID value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.OptUUID(value));
    }

    public enum Type {
        RED,
        SNOW;

        private final static Type[] VALUES = values();
    }

}
