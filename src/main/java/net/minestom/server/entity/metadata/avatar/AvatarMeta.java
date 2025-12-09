package net.minestom.server.entity.metadata.avatar;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MainHand;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.LivingEntityMeta;

public class AvatarMeta extends LivingEntityMeta {

    protected AvatarMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }
    
    public MainHand getMainHand() {
        return metadata.get(MetadataDef.Avatar.MAIN_HAND);
    }

    public void setMainHand(MainHand value) {
        metadata.set(MetadataDef.Avatar.MAIN_HAND, value);
    }

    public boolean isCapeEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_CAPE_ENABLED);
    }

    public void setCapeEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_CAPE_ENABLED, value);
    }

    public boolean isJacketEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_JACKET_ENABLED);
    }

    public void setJacketEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_JACKET_ENABLED, value);
    }

    public boolean isLeftSleeveEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_LEFT_SLEEVE_ENABLED);
    }

    public void setLeftSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_LEFT_SLEEVE_ENABLED, value);
    }

    public boolean isRightSleeveEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_RIGHT_SLEEVE_ENABLED);
    }

    public void setRightSleeveEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_RIGHT_SLEEVE_ENABLED, value);
    }

    public boolean isLeftLegEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_LEFT_PANTS_LEG_ENABLED);
    }

    public void setLeftLegEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_LEFT_PANTS_LEG_ENABLED, value);
    }

    public boolean isRightLegEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_RIGHT_PANTS_LEG_ENABLED);
    }

    public void setRightLegEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_RIGHT_PANTS_LEG_ENABLED, value);
    }

    public boolean isHatEnabled() {
        return metadata.get(MetadataDef.Avatar.IS_HAT_ENABLED);
    }

    public void setHatEnabled(boolean value) {
        metadata.set(MetadataDef.Avatar.IS_HAT_ENABLED, value);
    }

    public byte getDisplayedSkinParts() {
        return metadata.get(MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS);
    }

    public void setDisplayedSkinParts(byte skinDisplayByte) {
        metadata.set(MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS, skinDisplayByte);
    }

}
