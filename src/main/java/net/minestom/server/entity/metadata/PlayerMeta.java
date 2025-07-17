package net.minestom.server.entity.metadata;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerMeta extends LivingEntityMeta {
    public PlayerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getAdditionalHearts() {
        return metadata.get(MetadataDef.Player.ADDITIONAL_HEARTS);
    }

    public void setAdditionalHearts(float value) {
        metadata.set(MetadataDef.Player.ADDITIONAL_HEARTS, value);
    }

    public int getScore() {
        return metadata.get(MetadataDef.Player.SCORE);
    }

    public void setScore(int value) {
        metadata.set(MetadataDef.Player.SCORE, value);
    }

    public boolean isCapeEnabled() {
        return metadata.get(MetadataDef.Player.IS_CAPE_ENABLED);
    }

    public void setCapeEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_CAPE_ENABLED, value);
    }

    public boolean isJacketEnabled() {
        return metadata.get(MetadataDef.Player.IS_JACKET_ENABLED);
    }

    public void setJacketEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_JACKET_ENABLED, value);
    }

    public boolean isLeftSleeveEnabled() {
        return metadata.get(MetadataDef.Player.IS_LEFT_SLEEVE_ENABLED);
    }

    public void setLeftSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_LEFT_SLEEVE_ENABLED, value);
    }

    public boolean isRightSleeveEnabled() {
        return metadata.get(MetadataDef.Player.IS_RIGHT_SLEEVE_ENABLED);
    }

    public void setRightSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_RIGHT_SLEEVE_ENABLED, value);
    }

    public boolean isLeftLegEnabled() {
        return metadata.get(MetadataDef.Player.IS_LEFT_PANTS_LEG_ENABLED);
    }

    public void setLeftLegEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_LEFT_PANTS_LEG_ENABLED, value);
    }

    public boolean isRightLegEnabled() {
        return metadata.get(MetadataDef.Player.IS_RIGHT_PANTS_LEG_ENABLED);
    }

    public void setRightLegEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_RIGHT_PANTS_LEG_ENABLED, value);
    }

    public boolean isHatEnabled() {
        return metadata.get(MetadataDef.Player.IS_HAT_ENABLED);
    }

    public void setHatEnabled(boolean value) {
        metadata.set(MetadataDef.Player.IS_HAT_ENABLED, value);
    }

    public byte getDisplayedSkinParts() {
        return metadata.get(MetadataDef.Player.DISPLAYED_SKIN_PARTS_FLAGS);
    }

    public void setDisplayedSkinParts(byte skinDisplayByte) {
        metadata.set(MetadataDef.Player.DISPLAYED_SKIN_PARTS_FLAGS, skinDisplayByte);
    }

    public boolean isRightMainHand() {
        return metadata.get(MetadataDef.Player.MAIN_HAND) == (byte) 1;
    }

    public void setRightMainHand(boolean value) {
        metadata.set(MetadataDef.Player.MAIN_HAND, value ? (byte) 1 : (byte) 0);
    }

    @NotNull
    public BinaryTag getLeftShoulderEntityData() {
        return metadata.get(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA);
    }

    public void setLeftShoulderEntityData(@Nullable BinaryTag value) {
        if (value == null) value = CompoundBinaryTag.empty();

        metadata.set(MetadataDef.Player.LEFT_SHOULDER_ENTITY_DATA, value);
    }

    @NotNull
    public BinaryTag getRightShoulderEntityData() {
        return metadata.get(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA);
    }

    public void setRightShoulderEntityData(@Nullable BinaryTag value) {
        if (value == null) value = CompoundBinaryTag.empty();

        metadata.set(MetadataDef.Player.RIGHT_SHOULDER_ENTITY_DATA, value);
    }

}
