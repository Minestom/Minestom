package net.minestom.server.entity.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LivingEntityMeta extends EntityMeta {
    protected LivingEntityMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHandActive() {
        return metadata.get(MetadataDef.LivingEntity.IS_HAND_ACTIVE);
    }

    public void setHandActive(boolean value) {
        metadata.set(MetadataDef.LivingEntity.IS_HAND_ACTIVE, value);
    }

    @NotNull
    public PlayerHand getActiveHand() {
        return metadata.get(MetadataDef.LivingEntity.ACTIVE_HAND) ? PlayerHand.OFF : PlayerHand.MAIN;
    }

    public void setActiveHand(@NotNull PlayerHand hand) {
        metadata.set(MetadataDef.LivingEntity.ACTIVE_HAND, hand == PlayerHand.OFF);
    }

    public boolean isInRiptideSpinAttack() {
        return metadata.get(MetadataDef.LivingEntity.IS_RIPTIDE_SPIN_ATTACK);
    }

    public void setInRiptideSpinAttack(boolean value) {
        metadata.set(MetadataDef.LivingEntity.IS_RIPTIDE_SPIN_ATTACK, value);
    }

    public float getHealth() {
        return metadata.get(MetadataDef.LivingEntity.HEALTH);
    }

    public void setHealth(float value) {
        metadata.set(MetadataDef.LivingEntity.HEALTH, value);
    }

    public @NotNull List<Particle> getEffectParticles() {
        return metadata.get(MetadataDef.LivingEntity.POTION_EFFECT_PARTICLES);
    }

    public void setEffectParticles(@NotNull List<Particle> value) {
        metadata.set(MetadataDef.LivingEntity.POTION_EFFECT_PARTICLES, value);
    }

    public boolean isPotionEffectAmbient() {
        return metadata.get(MetadataDef.LivingEntity.IS_POTION_EFFECT_AMBIANT);
    }

    public void setPotionEffectAmbient(boolean value) {
        metadata.set(MetadataDef.LivingEntity.IS_POTION_EFFECT_AMBIANT, value);
    }

    public int getArrowCount() {
        return metadata.get(MetadataDef.LivingEntity.NUMBER_OF_ARROWS);
    }

    public void setArrowCount(int value) {
        metadata.set(MetadataDef.LivingEntity.NUMBER_OF_ARROWS, value);
    }

    /**
     * Gets the amount of bee stingers in this entity
     *
     * @return The amount of bee stingers
     */
    public int getBeeStingerCount() {
        return metadata.get(MetadataDef.LivingEntity.NUMBER_OF_BEE_STINGERS);
    }

    /**
     * Sets the amount of bee stingers in this entity
     *
     * @param value The amount of bee stingers to set, use 0 to clear all stingers
     */
    public void setBeeStingerCount(int value) {
        metadata.set(MetadataDef.LivingEntity.NUMBER_OF_BEE_STINGERS, value);
    }

    @Nullable
    public Point getBedInWhichSleepingPosition() {
        return metadata.get(MetadataDef.LivingEntity.LOCATION_OF_BED);
    }

    public void setBedInWhichSleepingPosition(@Nullable Point value) {
        metadata.set(MetadataDef.LivingEntity.LOCATION_OF_BED, value);
    }

}
