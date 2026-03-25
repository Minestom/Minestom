package net.minestom.server.entity.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.avatar.AvatarMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed abstract class LivingEntityMeta extends EntityMeta permits MobMeta, AvatarMeta, ArmorStandMeta {
    protected LivingEntityMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHandActive() {
        return get(MetadataDef.LivingEntity.IS_HAND_ACTIVE);
    }

    public void setHandActive(boolean value) {
        set(MetadataDef.LivingEntity.IS_HAND_ACTIVE, value);
    }

    public PlayerHand getActiveHand() {
        return get(MetadataDef.LivingEntity.ACTIVE_HAND) ? PlayerHand.OFF : PlayerHand.MAIN;
    }

    public void setActiveHand(PlayerHand hand) {
        set(MetadataDef.LivingEntity.ACTIVE_HAND, hand == PlayerHand.OFF);
    }

    public boolean isInRiptideSpinAttack() {
        return get(MetadataDef.LivingEntity.IS_RIPTIDE_SPIN_ATTACK);
    }

    public void setInRiptideSpinAttack(boolean value) {
        set(MetadataDef.LivingEntity.IS_RIPTIDE_SPIN_ATTACK, value);
    }

    public float getHealth() {
        return get(MetadataDef.LivingEntity.HEALTH);
    }

    public void setHealth(float value) {
        set(MetadataDef.LivingEntity.HEALTH, value);
    }

    public List<Particle> getEffectParticles() {
        return get(MetadataDef.LivingEntity.POTION_EFFECT_PARTICLES);
    }

    public void setEffectParticles(List<Particle> value) {
        set(MetadataDef.LivingEntity.POTION_EFFECT_PARTICLES, value);
    }

    public boolean isPotionEffectAmbient() {
        return get(MetadataDef.LivingEntity.IS_POTION_EFFECT_AMBIANT);
    }

    public void setPotionEffectAmbient(boolean value) {
        set(MetadataDef.LivingEntity.IS_POTION_EFFECT_AMBIANT, value);
    }

    public int getArrowCount() {
        return get(MetadataDef.LivingEntity.NUMBER_OF_ARROWS);
    }

    public void setArrowCount(int value) {
        set(MetadataDef.LivingEntity.NUMBER_OF_ARROWS, value);
    }

    /**
     * Gets the amount of bee stingers in this entity
     *
     * @return The amount of bee stingers
     */
    public int getBeeStingerCount() {
        return get(MetadataDef.LivingEntity.NUMBER_OF_BEE_STINGERS);
    }

    /**
     * Sets the amount of bee stingers in this entity
     *
     * @param value The amount of bee stingers to set, use 0 to clear all stingers
     */
    public void setBeeStingerCount(int value) {
        set(MetadataDef.LivingEntity.NUMBER_OF_BEE_STINGERS, value);
    }

    @Nullable
    public Point getBedInWhichSleepingPosition() {
        return get(MetadataDef.LivingEntity.LOCATION_OF_BED);
    }

    public void setBedInWhichSleepingPosition(@Nullable Point value) {
        set(MetadataDef.LivingEntity.LOCATION_OF_BED, value);
    }

}
