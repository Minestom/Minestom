package net.minestom.server.entity.metadata;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class EntityMeta {
    public static final byte OFFSET = 0;
    public static final byte MAX_OFFSET = OFFSET + 8;

    private final static byte ON_FIRE_BIT = 0x01;
    private final static byte CROUNCHING_BIT = 0x02;
    private final static byte SPRINTING_BIT = 0x08;
    private final static byte SWIMMING_BIT = 0x10;
    private final static byte INVISIBLE_BIT = 0x20;
    private final static byte HAS_GLOWING_EFFECT_BIT = 0x40;
    private final static byte FLYING_WITH_ELYTRA_BIT = (byte) 0x80;

    private final WeakReference<Entity> entityRef;
    protected final Metadata metadata;

    public EntityMeta(@Nullable Entity entity, @NotNull Metadata metadata) {
        this.entityRef = new WeakReference<>(entity);
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
        return getMaskBit(OFFSET, ON_FIRE_BIT);
    }

    public void setOnFire(boolean value) {
        setMaskBit(OFFSET, ON_FIRE_BIT, value);
    }

    public boolean isSneaking() {
        return getMaskBit(OFFSET, CROUNCHING_BIT);
    }

    public void setSneaking(boolean value) {
        setMaskBit(OFFSET, CROUNCHING_BIT, value);
    }

    public boolean isSprinting() {
        return getMaskBit(OFFSET, SPRINTING_BIT);
    }

    public void setSprinting(boolean value) {
        setMaskBit(OFFSET, SPRINTING_BIT, value);
    }

    public boolean isSwimming() {
        return getMaskBit(OFFSET, SWIMMING_BIT);
    }

    public void setSwimming(boolean value) {
        setMaskBit(OFFSET, SWIMMING_BIT, value);
    }

    public boolean isInvisible() {
        return getMaskBit(OFFSET, INVISIBLE_BIT);
    }

    public void setInvisible(boolean value) {
        setMaskBit(OFFSET, INVISIBLE_BIT, value);
    }

    public boolean isHasGlowingEffect() {
        return getMaskBit(OFFSET, HAS_GLOWING_EFFECT_BIT);
    }

    public void setHasGlowingEffect(boolean value) {
        setMaskBit(OFFSET, HAS_GLOWING_EFFECT_BIT, value);
    }

    public boolean isFlyingWithElytra() {
        return getMaskBit(OFFSET, FLYING_WITH_ELYTRA_BIT);
    }

    public void setFlyingWithElytra(boolean value) {
        setMaskBit(OFFSET, FLYING_WITH_ELYTRA_BIT, value);
    }

    public int getAirTicks() {
        return this.metadata.getIndex(OFFSET + 1, 300);
    }

    public void setAirTicks(int value) {
        this.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public Component getCustomName() {
        return this.metadata.getIndex(OFFSET + 2, null);
    }

    public void setCustomName(Component value) {
        this.metadata.setIndex(OFFSET + 2, Metadata.OptChat(value));
    }

    public boolean isCustomNameVisible() {
        return this.metadata.getIndex(OFFSET + 3, false);
    }

    public void setCustomNameVisible(boolean value) {
        this.metadata.setIndex(OFFSET + 3, Metadata.Boolean(value));
    }

    public boolean isSilent() {
        return this.metadata.getIndex(OFFSET + 4, false);
    }

    public void setSilent(boolean value) {
        this.metadata.setIndex(OFFSET + 4, Metadata.Boolean(value));
    }

    public boolean isHasNoGravity() {
        return this.metadata.getIndex(OFFSET + 5, false);
    }

    public void setHasNoGravity(boolean value) {
        this.metadata.setIndex(OFFSET + 5, Metadata.Boolean(value));
    }

    public Entity.Pose getPose() {
        return this.metadata.getIndex(OFFSET + 6, Entity.Pose.STANDING);
    }

    public void setPose(Entity.Pose value) {
        this.metadata.setIndex(OFFSET + 6, Metadata.Pose(value));
    }

    public int getTickFrozen() {
        return this.metadata.getIndex(OFFSET + 7, 0);
    }

    public void setTickFrozen(int tickFrozen) {
        this.metadata.setIndex(OFFSET + 7, Metadata.VarInt(tickFrozen));
    }

    protected byte getMask(int index) {
        return this.metadata.getIndex(index, (byte) 0);
    }

    protected void setMask(int index, byte mask) {
        this.metadata.setIndex(index, Metadata.Byte(mask));
    }

    protected boolean getMaskBit(int index, byte bit) {
        return (getMask(index) & bit) == bit;
    }

    protected void setMaskBit(int index, byte bit, boolean value) {
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

    protected void consumeEntity(Consumer<Entity> consumer) {
        Entity entity = this.entityRef.get();
        if (entity != null) {
            consumer.accept(entity);
        }
    }

}
