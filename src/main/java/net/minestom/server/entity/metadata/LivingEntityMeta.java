package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEntityMeta extends EntityMeta {

    private final static byte MASK_INDEX = 7;

    private final static byte IS_HAND_ACTIVE_BIT = 0x01;
    private final static byte ACTIVE_HAND_BIT = 0x02;
    private final static byte IS_IN_SPIN_ATTACK_BIT = 0x04;

    protected LivingEntityMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHandActive() {
        return getMaskBit(MASK_INDEX, IS_HAND_ACTIVE_BIT);
    }

    public void setHandActive(boolean value) {
        setMaskBit(MASK_INDEX, IS_HAND_ACTIVE_BIT, value);
    }

    @NotNull
    public Player.Hand getActiveHand() {
        return getMaskBit(MASK_INDEX, ACTIVE_HAND_BIT) ? Player.Hand.OFF : Player.Hand.MAIN;
    }

    public void setActiveHand(@NotNull Player.Hand hand) {
        setMaskBit(MASK_INDEX, ACTIVE_HAND_BIT, hand == Player.Hand.OFF);
    }

    public boolean isInRiptideSpinAttack() {
        return getMaskBit(MASK_INDEX, IS_IN_SPIN_ATTACK_BIT);
    }

    public void setInRiptideSpinAttack(boolean value) {
        setMaskBit(MASK_INDEX, IS_IN_SPIN_ATTACK_BIT, value);
    }

    public float getHealth() {
        return super.metadata.getIndex((byte) 8, 1F);
    }

    public void setHealth(float value) {
        super.metadata.setIndex((byte) 8, Metadata.Float(value));
    }

    public int getPotionEffectColor() {
        return super.metadata.getIndex((byte) 9, 0);
    }

    public void setPotionEffectColor(int value) {
        super.metadata.setIndex((byte) 9, Metadata.VarInt(value));
    }

    public boolean isPotionEffectAmbient() {
        return super.metadata.getIndex((byte) 10, false);
    }

    public void setPotionEffectAmbient(boolean value) {
        super.metadata.setIndex((byte) 10, Metadata.Boolean(value));
    }

    public int getArrowCount() {
        return super.metadata.getIndex((byte) 11, 0);
    }

    public void setArrowCount(int value) {
        super.metadata.getIndex((byte) 11, Metadata.VarInt(value));
    }

    public int getHealthAddedByAbsorption() {
        return super.metadata.getIndex((byte) 12, 0);
    }

    public void setHealthAddedByAbsorption(int value) {
        super.metadata.getIndex((byte) 12, Metadata.VarInt(value));
    }

    @Nullable
    public BlockPosition getBedInWhichSleepingPosition() {
        return super.metadata.getIndex((byte) 13, null);
    }

    public void setBedInWhichSleepingPosition(@Nullable BlockPosition value) {
        super.metadata.setIndex((byte) 13, Metadata.OptPosition(value));
    }

}
