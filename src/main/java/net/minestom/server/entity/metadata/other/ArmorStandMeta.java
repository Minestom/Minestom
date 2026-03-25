package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.LivingEntityMeta;

public final class ArmorStandMeta extends LivingEntityMeta {
    public ArmorStandMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSmall() {
        return get(MetadataDef.ArmorStand.IS_SMALL);
    }

    public void setSmall(boolean value) {
        set(MetadataDef.ArmorStand.IS_SMALL, value);
    }

    public boolean isHasArms() {
        return get(MetadataDef.ArmorStand.HAS_ARMS);
    }

    public void setHasArms(boolean value) {
        set(MetadataDef.ArmorStand.HAS_ARMS, value);
    }

    public boolean isHasNoBasePlate() {
        return get(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE);
    }

    public void setHasNoBasePlate(boolean value) {
        set(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE, value);
    }

    public boolean isMarker() {
        return get(MetadataDef.ArmorStand.IS_MARKER);
    }

    public void setMarker(boolean value) {
        set(MetadataDef.ArmorStand.IS_MARKER, value);
    }

    public Vec getHeadRotation() {
        return get(MetadataDef.ArmorStand.HEAD_ROTATION).asVec();
    }

    public void setHeadRotation(Vec value) {
        set(MetadataDef.ArmorStand.HEAD_ROTATION, value);
    }

    public Vec getBodyRotation() {
        return get(MetadataDef.ArmorStand.BODY_ROTATION).asVec();
    }

    public void setBodyRotation(Vec value) {
        set(MetadataDef.ArmorStand.BODY_ROTATION, value);
    }

    public Vec getLeftArmRotation() {
        return get(MetadataDef.ArmorStand.LEFT_ARM_ROTATION).asVec();
    }

    public void setLeftArmRotation(Vec value) {
        set(MetadataDef.ArmorStand.LEFT_ARM_ROTATION, value);
    }

    public Vec getRightArmRotation() {
        return get(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION).asVec();
    }

    public void setRightArmRotation(Vec value) {
        set(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION, value);
    }

    public Vec getLeftLegRotation() {
        return get(MetadataDef.ArmorStand.LEFT_LEG_ROTATION).asVec();
    }

    public void setLeftLegRotation(Vec value) {
        set(MetadataDef.ArmorStand.LEFT_LEG_ROTATION, value);
    }

    public Vec getRightLegRotation() {
        return get(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION).asVec();
    }

    public void setRightLegRotation(Vec value) {
        set(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION, value);
    }

}
