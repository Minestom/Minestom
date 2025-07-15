package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.LivingEntityMeta;

public class ArmorStandMeta extends LivingEntityMeta {
    public ArmorStandMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isSmall() {
        return metadata.get(MetadataDef.ArmorStand.IS_SMALL);
    }

    public void setSmall(boolean value) {
        metadata.set(MetadataDef.ArmorStand.IS_SMALL, value);
    }

    public boolean isHasArms() {
        return metadata.get(MetadataDef.ArmorStand.HAS_ARMS);
    }

    public void setHasArms(boolean value) {
        metadata.set(MetadataDef.ArmorStand.HAS_ARMS, value);
    }

    public boolean isHasNoBasePlate() {
        return metadata.get(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE);
    }

    public void setHasNoBasePlate(boolean value) {
        metadata.set(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE, value);
    }

    public boolean isMarker() {
        return metadata.get(MetadataDef.ArmorStand.IS_MARKER);
    }

    public void setMarker(boolean value) {
        metadata.set(MetadataDef.ArmorStand.IS_MARKER, value);
    }

    public Vec getHeadRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.HEAD_ROTATION));
    }

    public void setHeadRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.HEAD_ROTATION, value);
    }

    public Vec getBodyRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.BODY_ROTATION));
    }

    public void setBodyRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.BODY_ROTATION, value);
    }

    public Vec getLeftArmRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.LEFT_ARM_ROTATION));
    }

    public void setLeftArmRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.LEFT_ARM_ROTATION, value);
    }

    public Vec getRightArmRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION));
    }

    public void setRightArmRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION, value);
    }

    public Vec getLeftLegRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.LEFT_LEG_ROTATION));
    }

    public void setLeftLegRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.LEFT_LEG_ROTATION, value);
    }

    public Vec getRightLegRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION));
    }

    public void setRightLegRotation(Vec value) {
        metadata.set(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION, value);
    }

}
