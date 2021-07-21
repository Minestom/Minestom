package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import org.jetbrains.annotations.NotNull;

public class ArmorStandMeta extends LivingEntityMeta {
    public static final byte OFFSET = LivingEntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 7;

    private final static byte IS_SMALL_BIT = 0x01;
    private final static byte HAS_ARMS_BIT = 0x04;
    private final static byte HAS_NO_BASE_PLATE_BIT = 0x08;
    private final static byte IS_MARKER_BIT = 0x10;

    public ArmorStandMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isSmall() {
        return getMaskBit(OFFSET, IS_SMALL_BIT);
    }

    public void setSmall(boolean value) {
        setMaskBit(OFFSET, IS_SMALL_BIT, value);
    }

    public boolean isHasArms() {
        return getMaskBit(OFFSET, HAS_ARMS_BIT);
    }

    public void setHasArms(boolean value) {
        setMaskBit(OFFSET, HAS_ARMS_BIT, value);
    }

    public boolean isHasNoBasePlate() {
        return getMaskBit(OFFSET, HAS_NO_BASE_PLATE_BIT);
    }

    public void setHasNoBasePlate(boolean value) {
        setMaskBit(OFFSET, HAS_NO_BASE_PLATE_BIT, value);
    }

    public boolean isMarker() {
        return getMaskBit(OFFSET, IS_MARKER_BIT);
    }

    public void setMarker(boolean value) {
        setMaskBit(OFFSET, IS_MARKER_BIT, value);
    }

    @NotNull
    public Vec getHeadRotation() {
        return super.metadata.getIndex(OFFSET + 1, Vec.ZERO);
    }

    public void setHeadRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Rotation(value));
    }

    @NotNull
    public Vec getBodyRotation() {
        return super.metadata.getIndex(OFFSET + 2, Vec.ZERO);
    }

    public void setBodyRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Rotation(value));
    }

    @NotNull
    public Vec getLeftArmRotation() {
        return super.metadata.getIndex(OFFSET + 3, new Vec(-10D, 0D, -10D));
    }

    public void setLeftArmRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Rotation(value));
    }

    @NotNull
    public Vec getRightArmRotation() {
        return super.metadata.getIndex(OFFSET + 4, new Vec(-15D, 0D, 10D));
    }

    public void setRightArmRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Rotation(value));
    }

    @NotNull
    public Vec getLeftLegRotation() {
        return super.metadata.getIndex(OFFSET + 5, new Vec(-1D, 0D, -1D));
    }

    public void setLeftLegRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.Rotation(value));
    }

    @NotNull
    public Vec getRightLegRotation() {
        return super.metadata.getIndex(OFFSET + 6, new Vec(1D, 0D, 1D));
    }

    public void setRightLegRotation(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.Rotation(value));
    }

}
