package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LivingEntityMeta extends BaseEntityMeta {

    private final static byte MASK_INDEX = 7;

    private final static byte IS_HAND_ACTIVE_BIT = 0x01;
    private final static byte ACTIVE_HAND_BIT = 0x02;
    private final static byte IS_IN_SPIN_ATTACK_BIT = 0x04;

    protected LivingEntityMeta(@NotNull Entity entity) {
        super(entity);
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
        return getMetadata().getIndex((byte) 8, 1F);
    }

    public void setHealth(float value) {
        getMetadata().setIndex((byte) 8, Metadata.Float(value));
    }

    public int getPotionEffectColor() {
        return getMetadata().getIndex((byte) 9, 0);
    }

    public void setPotionEffectColor(int value) {
        getMetadata().setIndex((byte) 9, Metadata.VarInt(value));
    }

    public boolean isPotionEffectAmbient() {
        return getMetadata().getIndex((byte) 10, false);
    }

    public void setPotionEffectAmbient(boolean value) {
        getMetadata().setIndex((byte) 10, Metadata.Boolean(value));
    }

    public int getNumberOfArrows() {
        return getMetadata().getIndex((byte) 11, 0);
    }

    public void setNumberOfArrows(int value) {
        getMetadata().getIndex((byte) 11, Metadata.VarInt(value));
    }

    public int getHealthAddedByAbsorption() {
        return getMetadata().getIndex((byte) 12, 0);
    }

    public void setHealthAddedByAbsorption(int value) {
        getMetadata().getIndex((byte) 12, Metadata.VarInt(value));
    }

    @Nullable
    public BlockPosition getBedInWhichSleepingPosition() {
        return getMetadata().getIndex((byte) 13, null);
    }

    public void setBedInWhichSleepingPosition(@Nullable BlockPosition value) {
        getMetadata().setIndex((byte) 13, Metadata.OptPosition(value));
    }

}
