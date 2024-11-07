package net.minestom.server.entity.metadata;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class EntityMeta {
    private final WeakReference<Entity> entityRef;
    protected final MetadataHolder metadata;

    public EntityMeta(@Nullable Entity entity, @NotNull MetadataHolder metadata) {
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
        return metadata.get(MetadataDef.IS_ON_FIRE);
    }

    public void setOnFire(boolean value) {
        metadata.set(MetadataDef.IS_ON_FIRE, value);
    }

    public boolean isSneaking() {
        return metadata.get(MetadataDef.IS_CROUCHING);
    }

    public void setSneaking(boolean value) {
        metadata.set(MetadataDef.IS_CROUCHING, value);
    }

    public boolean isSprinting() {
        return metadata.get(MetadataDef.IS_SPRINTING);
    }

    public void setSprinting(boolean value) {
        metadata.set(MetadataDef.IS_SPRINTING, value);
    }

    public boolean isSwimming() {
        return metadata.get(MetadataDef.IS_SWIMMING);
    }

    public void setSwimming(boolean value) {
        metadata.set(MetadataDef.IS_SWIMMING, value);
    }

    public boolean isInvisible() {
        return metadata.get(MetadataDef.IS_INVISIBLE);
    }

    public void setInvisible(boolean value) {
        metadata.set(MetadataDef.IS_INVISIBLE, value);
    }

    public boolean isHasGlowingEffect() {
        return metadata.get(MetadataDef.HAS_GLOWING_EFFECT);
    }

    public void setHasGlowingEffect(boolean value) {
        metadata.set(MetadataDef.HAS_GLOWING_EFFECT, value);
    }

    public boolean isFlyingWithElytra() {
        return metadata.get(MetadataDef.IS_FLYING_WITH_ELYTRA);
    }

    public void setFlyingWithElytra(boolean value) {
        metadata.set(MetadataDef.IS_FLYING_WITH_ELYTRA, value);
    }

    public int getAirTicks() {
        return metadata.get(MetadataDef.AIR_TICKS);
    }

    public void setAirTicks(int value) {
        metadata.set(MetadataDef.AIR_TICKS, value);
    }

    @Nullable
    public Component getCustomName() {
        return metadata.get(MetadataDef.CUSTOM_NAME);
    }

    public void setCustomName(@Nullable Component value) {
        metadata.set(MetadataDef.CUSTOM_NAME, value);
    }

    public boolean isCustomNameVisible() {
        return metadata.get(MetadataDef.CUSTOM_NAME_VISIBLE);
    }

    public void setCustomNameVisible(boolean value) {
        metadata.set(MetadataDef.CUSTOM_NAME_VISIBLE, value);
    }

    public boolean isSilent() {
        return metadata.get(MetadataDef.IS_SILENT);
    }

    public void setSilent(boolean value) {
        metadata.set(MetadataDef.IS_SILENT, value);
    }

    public boolean isHasNoGravity() {
        return metadata.get(MetadataDef.HAS_NO_GRAVITY);
    }

    public void setHasNoGravity(boolean value) {
        metadata.set(MetadataDef.HAS_NO_GRAVITY, value);
    }

    @NotNull
    public EntityPose getPose() {
        return metadata.get(MetadataDef.POSE);
    }

    public void setPose(@NotNull EntityPose value) {
        metadata.set(MetadataDef.POSE, value);
    }

    public int getTickFrozen() {
        return metadata.get(MetadataDef.TICKS_FROZEN);
    }

    public void setTickFrozen(int tickFrozen) {
        metadata.set(MetadataDef.TICKS_FROZEN, tickFrozen);
    }

    protected void consumeEntity(Consumer<Entity> consumer) {
        Entity entity = this.entityRef.get();
        if (entity != null) {
            consumer.accept(entity);
        }
    }

}
