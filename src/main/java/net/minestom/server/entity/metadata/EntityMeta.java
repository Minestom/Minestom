package net.minestom.server.entity.metadata;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class EntityMeta {

    private final static byte MASK_INDEX = 0;

    private final static byte ON_FIRE_BIT = 0x01;
    private final static byte CROUNCHING_BIT = 0x02;
    private final static byte SPRINTING_BIT = 0x08;
    private final static byte SWIMMING_BIT = 0x10;
    private final static byte INVISIBLE_BIT = 0x20;
    private final static byte HAS_GLOWING_EFFECT_BIT = 0x40;
    private final static byte FLYING_WITH_ELYTRA_BIT = (byte) 0x80;

    protected final Entity entity;

    protected EntityMeta(@NotNull Entity entity) {
        this.entity = entity;
    }

    public boolean isOnFire() {
        return getMaskBit(MASK_INDEX, ON_FIRE_BIT);
    }

    public void setOnFire(boolean value) {
        setMaskBit(MASK_INDEX, ON_FIRE_BIT, value);
    }

    public boolean isCrounching() {
        return getMaskBit(MASK_INDEX, CROUNCHING_BIT);
    }

    public void setCrounching(boolean value) {
        setMaskBit(MASK_INDEX, CROUNCHING_BIT, value);
    }

    public boolean isSprinting() {
        return getMaskBit(MASK_INDEX, SPRINTING_BIT);
    }

    public void setSprinting(boolean value) {
        setMaskBit(MASK_INDEX, SPRINTING_BIT, value);
    }

    public boolean isSwimming() {
        return getMaskBit(MASK_INDEX, SWIMMING_BIT);
    }

    public void setSwimming(boolean value) {
        setMaskBit(MASK_INDEX, SWIMMING_BIT, value);
    }

    public boolean isInvisible() {
        return getMaskBit(MASK_INDEX, INVISIBLE_BIT);
    }

    public void setInvisible(boolean value) {
        setMaskBit(MASK_INDEX, INVISIBLE_BIT, value);
    }

    public boolean isHasGlowingEffect() {
        return getMaskBit(MASK_INDEX, HAS_GLOWING_EFFECT_BIT);
    }

    public void setHasGlowingEffect(boolean value) {
        setMaskBit(MASK_INDEX, HAS_GLOWING_EFFECT_BIT, value);
    }

    public boolean isFlyingWithElytra() {
        return getMaskBit(MASK_INDEX, FLYING_WITH_ELYTRA_BIT);
    }

    public void setFlyingWithElytra(boolean value) {
        setMaskBit(MASK_INDEX, FLYING_WITH_ELYTRA_BIT, value);
    }

    public int getAirTicks() {
        return getMetadata().getIndex((byte) 1, 300);
    }

    public void setAirTicks(int value) {
        getMetadata().setIndex((byte) 1, Metadata.VarInt(value));
    }

    public JsonMessage getCustomName() {
        return getMetadata().getIndex((byte) 2, null);
    }

    public void setCustomName(JsonMessage value) {
        getMetadata().setIndex((byte) 2, Metadata.OptChat(value));
    }

    public boolean isCustomNameVisible() {
        return getMetadata().getIndex((byte) 3, false);
    }

    public void setCustomNameVisible(boolean value) {
        getMetadata().setIndex((byte) 3, Metadata.Boolean(value));
    }

    public boolean isSilent() {
        return getMetadata().getIndex((byte) 4, false);
    }

    public void setSilent(boolean value) {
        getMetadata().setIndex((byte) 4, Metadata.Boolean(value));
    }

    public boolean isHasNoGravity() {
        return getMetadata().getIndex((byte) 5, false);
    }

    public void setHasNoGravity(boolean value) {
        getMetadata().setIndex((byte) 5, Metadata.Boolean(value));
    }

    public Entity.Pose getPose() {
        return getMetadata().getIndex((byte) 6, Entity.Pose.STANDING);
    }

    public void setPose(Entity.Pose value) {
        getMetadata().setIndex((byte) 6, Metadata.Pose(value));
    }

    protected Metadata getMetadata() {
        return this.entity.getMetadata();
    }

    protected byte getMask(byte index) {
        return getMetadata().getIndex(index, (byte) 0);
    }

    protected void setMask(byte index, byte mask) {
        getMetadata().setIndex(index, Metadata.Byte(mask));
    }

    protected boolean getMaskBit(byte index, byte bit) {
        return (getMask(index) & bit) == bit;
    }

    protected void setMaskBit(byte index, byte bit, boolean value) {
        byte mask = getMask(index);
        boolean currentValue = (mask & bit) == bit;
        if (currentValue == value) {
            return;
        }
        if (value) {
            mask |= bit;
        } else {
            mask &= ~bit;
        }
        setMask(index, mask);
    }

    protected void setBoundingBox(double x, double y, double z) {
        this.entity.setBoundingBox(x, y, z);
    }

    protected void setBoundingBox(double width, double height) {
        setBoundingBox(width, height, width);
    }

}
