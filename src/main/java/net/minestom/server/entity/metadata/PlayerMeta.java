package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;

public class PlayerMeta extends LivingEntityMeta {
    public static final byte OFFSET = LivingEntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final static byte CAPE_BIT = 0x01;
    private final static byte JACKET_BIT = 0x02;
    private final static byte LEFT_SLEEVE_BIT = 0x04;
    private final static byte RIGHT_SLEEVE_BIT = 0x08;
    private final static byte LEFT_LEG_BIT = 0x10;
    private final static byte RIGHT_LEG_BIT = 0x20;
    private final static byte HAT_BIT = 0x40;

    public PlayerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public float getAdditionalHearts() {
        return super.metadata.getIndex(OFFSET, 0F);
    }

    public void setAdditionalHearts(float value) {
        super.metadata.setIndex(OFFSET, Metadata.Float(value));
    }

    public int getScore() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    public void setScore(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public boolean isCapeEnabled() {
        return getMaskBit(OFFSET + 2, CAPE_BIT);
    }

    public void setCapeEnabled(boolean value) {
        setMaskBit(OFFSET + 2, CAPE_BIT, value);
    }

    public boolean isJacketEnabled() {
        return getMaskBit(OFFSET + 2, JACKET_BIT);
    }

    public void setJacketEnabled(boolean value) {
        setMaskBit(OFFSET + 2, JACKET_BIT, value);
    }

    public boolean isLeftSleeveEnabled() {
        return getMaskBit(OFFSET + 2, LEFT_SLEEVE_BIT);
    }

    public void setLeftSleeveEnabled(boolean value) {
        setMaskBit(OFFSET + 2, LEFT_SLEEVE_BIT, value);
    }

    public boolean isRightSleeveEnabled() {
        return getMaskBit(OFFSET + 2, RIGHT_SLEEVE_BIT);
    }

    public void setRightSleeveEnabled(boolean value) {
        setMaskBit(OFFSET + 2, RIGHT_SLEEVE_BIT, value);
    }

    public boolean isLeftLegEnabled() {
        return getMaskBit(OFFSET + 2, LEFT_LEG_BIT);
    }

    public void setLeftLegEnabled(boolean value) {
        setMaskBit(OFFSET + 2, LEFT_LEG_BIT, value);
    }

    public boolean isRightLegEnabled() {
        return getMaskBit(OFFSET + 2, RIGHT_LEG_BIT);
    }

    public void setRightLegEnabled(boolean value) {
        setMaskBit(OFFSET + 2, RIGHT_LEG_BIT, value);
    }

    public boolean isHatEnabled() {
        return getMaskBit(OFFSET + 2, HAT_BIT);
    }

    public void setHatEnabled(boolean value) {
        setMaskBit(OFFSET + 2, HAT_BIT, value);
    }

    public boolean isRightMainHand() {
        return super.metadata.getIndex(OFFSET + 3, (byte) 1) == (byte) 1;
    }

    public void setRightMainHand(boolean value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Byte(value ? (byte) 1 : (byte) 0));
    }

    @Nullable
    public NBT getLeftShoulderEntityData() {
        return super.metadata.getIndex(OFFSET + 4, null);
    }

    public void setLeftShoulderEntityData(@Nullable NBT value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.NBT(value));
    }

    @Nullable
    public NBT getRightShoulderEntityData() {
        return super.metadata.getIndex(OFFSET + 5, null);
    }

    public void setRightShoulderEntityData(@Nullable NBT value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.NBT(value));
    }

}
