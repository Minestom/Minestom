package net.minestom.server.entity.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LivingEntityMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 7;

    private final static byte IS_HAND_ACTIVE_BIT = 0x01;
    private final static byte ACTIVE_HAND_BIT = 0x02;
    private final static byte IS_IN_SPIN_ATTACK_BIT = 0x04;

    protected LivingEntityMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHandActive() {
        return getMaskBit(OFFSET, IS_HAND_ACTIVE_BIT);
    }

    public void setHandActive(boolean value) {
        setMaskBit(OFFSET, IS_HAND_ACTIVE_BIT, value);
    }

    @NotNull
    public PlayerHand getActiveHand() {
        return getMaskBit(OFFSET, ACTIVE_HAND_BIT) ? PlayerHand.OFF : PlayerHand.MAIN;
    }

    public void setActiveHand(@NotNull PlayerHand hand) {
        setMaskBit(OFFSET, ACTIVE_HAND_BIT, hand == PlayerHand.OFF);
    }

    public boolean isInRiptideSpinAttack() {
        return getMaskBit(OFFSET, IS_IN_SPIN_ATTACK_BIT);
    }

    public void setInRiptideSpinAttack(boolean value) {
        setMaskBit(OFFSET, IS_IN_SPIN_ATTACK_BIT, value);
    }

    public float getHealth() {
        return super.metadata.getIndex(OFFSET + 1, 1F);
    }

    public void setHealth(float value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Float(value));
    }

    public @NotNull List<Particle> getEffectParticles() {
        return super.metadata.getIndex(OFFSET + 2, List.of());
    }

    public void setEffectParticles(@NotNull List<Particle> value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.ParticleList(value));
    }

    public boolean isPotionEffectAmbient() {
        return super.metadata.getIndex(OFFSET + 3, false);
    }

    public void setPotionEffectAmbient(boolean value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Boolean(value));
    }

    public int getArrowCount() {
        return super.metadata.getIndex(OFFSET + 4, 0);
    }

    public void setArrowCount(int value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.VarInt(value));
    }

    /**
     * Gets the amount of bee stingers in this entity
     *
     * @return The amount of bee stingers
     */
    public int getBeeStingerCount() {
        return super.metadata.getIndex(OFFSET + 5, 0);
    }

    /**
     * Sets the amount of bee stingers in this entity
     *
     * @param value The amount of bee stingers to set, use 0 to clear all stingers
     */
    public void setBeeStingerCount(int value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.VarInt(value));
    }

    @Nullable
    public Point getBedInWhichSleepingPosition() {
        return super.metadata.getIndex(OFFSET + 6, null);
    }

    public void setBedInWhichSleepingPosition(@Nullable Point value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.OptBlockPosition(value));
    }

}
