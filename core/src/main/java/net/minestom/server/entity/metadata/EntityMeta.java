package net.minestom.server.entity.metadata;

import net.kyori.adventure.text.Component;
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
    protected final Metadata metadata;

    protected EntityMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        this.entity = entity;
        this.metadata = metadata;
    }

    /**
     * Sets whether any changes to this meta must result in a metadata packet being sent to entity viewers.
     * By default it's set to true.
     * <p>
     * It's usable if you want to change multiple values of this meta at the same time and want just a
     * single packet being sent: if so, disable notification before your first change and enable it
     * right after the last one: once notification is set to false, we collect all the updates
     * that are being performed, and when it's returned to true we send them all together.
     * An example usage could be found at
     * {@link net.minestom.server.entity.LivingEntity#refreshActiveHand(boolean, boolean, boolean)}.
     *
     * @param notifyAboutChanges if to notify entity viewers about this meta changes.
     */
    public void setNotifyAboutChanges(boolean notifyAboutChanges) {
        this.metadata.setNotifyAboutChanges(notifyAboutChanges);
    }

    public boolean isOnFire() {
        return getMaskBit(MASK_INDEX, ON_FIRE_BIT);
    }

    public void setOnFire(boolean value) {
        setMaskBit(MASK_INDEX, ON_FIRE_BIT, value);
    }

    public boolean isSneaking() {
        return getMaskBit(MASK_INDEX, CROUNCHING_BIT);
    }

    public void setSneaking(boolean value) {
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
        return this.metadata.getIndex((byte) 1, 300);
    }

    public void setAirTicks(int value) {
        this.metadata.setIndex((byte) 1, Metadata.VarInt(value));
    }

    /**
     * @deprecated Use {@link #getCustomName()}
     */
    @Deprecated
    public JsonMessage getCustomNameJson() {
        return JsonMessage.fromComponent(this.getCustomName());
    }

    /**
     * @deprecated Use {@link #setCustomName(Component)}
     */
    @Deprecated
    public void setCustomName(JsonMessage value) {
        if (value != null) {
            this.setCustomName(value.asComponent());
        }
    }

    public Component getCustomName() {
        return this.metadata.getIndex((byte) 2, null);
    }

    public void setCustomName(Component value) {
        this.metadata.setIndex((byte) 2, Metadata.OptChat(value));
    }

    public boolean isCustomNameVisible() {
        return this.metadata.getIndex((byte) 3, false);
    }

    public void setCustomNameVisible(boolean value) {
        this.metadata.setIndex((byte) 3, Metadata.Boolean(value));
    }

    public boolean isSilent() {
        return this.metadata.getIndex((byte) 4, false);
    }

    public void setSilent(boolean value) {
        this.metadata.setIndex((byte) 4, Metadata.Boolean(value));
    }

    public boolean isHasNoGravity() {
        return this.metadata.getIndex((byte) 5, false);
    }

    public void setHasNoGravity(boolean value) {
        this.metadata.setIndex((byte) 5, Metadata.Boolean(value));
    }

    public Entity.Pose getPose() {
        return this.metadata.getIndex((byte) 6, Entity.Pose.STANDING);
    }

    public void setPose(Entity.Pose value) {
        this.metadata.setIndex((byte) 6, Metadata.Pose(value));
    }

    protected byte getMask(byte index) {
        return this.metadata.getIndex(index, (byte) 0);
    }

    protected void setMask(byte index, byte mask) {
        this.metadata.setIndex(index, Metadata.Byte(mask));
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
