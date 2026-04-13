package net.minestom.server.entity.metadata.avatar;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

public class MannequinMeta extends AvatarMeta {
    public MannequinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ResolvableProfile getProfile() {
        return metadata.get(MetadataDef.Mannequin.PROFILE);
    }

    public void setProfile(ResolvableProfile value) {
        metadata.set(MetadataDef.Mannequin.PROFILE, value);
    }

    public boolean isImmovable() {
        return metadata.get(MetadataDef.Mannequin.IMMOVABLE);
    }

    public void setImmovable(boolean value) {
        metadata.set(MetadataDef.Mannequin.IMMOVABLE, value);
    }

    public @Nullable Component getDescription() {
        return metadata.get(MetadataDef.Mannequin.DESCRIPTION);
    }

    public void setDescription(@Nullable Component value) {
        metadata.set(MetadataDef.Mannequin.DESCRIPTION, value);
    }

    @Override
    public boolean isCapeEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_CAPE_ENABLED);
    }

    @Override
    public void setCapeEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_CAPE_ENABLED, value);
    }

    @Override
    public boolean isJacketEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_JACKET_ENABLED);
    }

    @Override
    public void setJacketEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_JACKET_ENABLED, value);
    }

    @Override
    public boolean isLeftSleeveEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_LEFT_SLEEVE_ENABLED);
    }

    @Override
    public void setLeftSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_LEFT_SLEEVE_ENABLED, value);
    }

    @Override
    public boolean isRightSleeveEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_RIGHT_SLEEVE_ENABLED);
    }

    @Override
    public void setRightSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_RIGHT_SLEEVE_ENABLED, value);
    }

    @Override
    public boolean isLeftLegEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_LEFT_PANTS_LEG_ENABLED);
    }

    @Override
    public void setLeftLegEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_LEFT_PANTS_LEG_ENABLED, value);
    }

    @Override
    public boolean isRightLegEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_RIGHT_PANTS_LEG_ENABLED);
    }

    @Override
    public void setRightLegEnabled(boolean value) {
        metadata.get(MetadataDef.Mannequin.IS_RIGHT_PANTS_LEG_ENABLED);
    }

    @Override
    public boolean isHatEnabled() {
        return metadata.get(MetadataDef.Mannequin.IS_HAT_ENABLED);
    }

    @Override
    public void setHatEnabled(boolean value) {
        metadata.set(MetadataDef.Mannequin.IS_HAT_ENABLED, value);
    }

    @Override
    public byte getDisplayedSkinParts() {
        return metadata.get(MetadataDef.Mannequin.DISPLAYED_MODEL_PARTS_FLAGS);
    }

    @Override
    public void setDisplayedSkinParts(byte skinDisplayByte) {
        metadata.set(MetadataDef.Mannequin.DISPLAYED_MODEL_PARTS_FLAGS, skinDisplayByte);
    }
}
