package net.minestom.server.entity.metadata;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEntityMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 7;

    private final static byte IS_HAND_ACTIVE_BIT = 0x01;
    private final static byte ACTIVE_HAND_BIT = 0x02;
    private final static byte IS_IN_SPIN_ATTACK_BIT = 0x04;

    protected LivingEntityMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHandActive() {
        return getMaskBit(OFFSET, IS_HAND_ACTIVE_BIT);
    }

    public void setHandActive(boolean value) {
        setMaskBit(OFFSET, IS_HAND_ACTIVE_BIT, value);
    }

    @NotNull
    public Player.Hand getActiveHand() {
        return getMaskBit(OFFSET, ACTIVE_HAND_BIT) ? Player.Hand.OFF : Player.Hand.MAIN;
    }

    public void setActiveHand(@NotNull Player.Hand hand) {
        setMaskBit(OFFSET, ACTIVE_HAND_BIT, hand == Player.Hand.OFF);
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

    public int getPotionEffectColor() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setPotionEffectColor(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
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

    public int getHealthAddedByAbsorption() {
        return super.metadata.getIndex(OFFSET + 5, 0);
    }

    public void setHealthAddedByAbsorption(int value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.VarInt(value));
    }

    @Nullable
    public Point getBedInWhichSleepingPosition() {
        return super.metadata.getIndex(OFFSET + 6, null);
    }

    public void setBedInWhichSleepingPosition(@Nullable Point value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.OptPosition(value));
    }

}
