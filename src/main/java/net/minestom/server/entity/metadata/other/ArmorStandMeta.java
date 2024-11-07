package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import org.jetbrains.annotations.NotNull;

public class ArmorStandMeta extends LivingEntityMeta {
    public ArmorStandMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
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

    @NotNull
    public Vec getHeadRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.HEAD_ROTATION));
    }

    public void setHeadRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.HEAD_ROTATION, value);
    }

    @NotNull
    public Vec getBodyRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.BODY_ROTATION));
    }

    public void setBodyRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.BODY_ROTATION, value);
    }

    @NotNull
    public Vec getLeftArmRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.LEFT_ARM_ROTATION));
    }

    public void setLeftArmRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.LEFT_ARM_ROTATION, value);
    }

    @NotNull
    public Vec getRightArmRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION));
    }

    public void setRightArmRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION, value);
    }

    @NotNull
    public Vec getLeftLegRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.LEFT_LEG_ROTATION));
    }

    public void setLeftLegRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.LEFT_LEG_ROTATION, value);
    }

    @NotNull
    public Vec getRightLegRotation() {
        return Vec.fromPoint(metadata.get(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION));
    }

    public void setRightLegRotation(@NotNull Vec value) {
        metadata.set(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION, value);
    }

}
