package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.utils.Vector;
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
    public Vector getHeadRotation() {
        return super.metadata.getIndex(OFFSET + 1, new Vector(0D, 0D, 0D));
    }

    public void setHeadRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Rotation(value));
    }

    @NotNull
    public Vector getBodyRotation() {
        return super.metadata.getIndex(OFFSET + 2, new Vector(0D, 0D, 0D));
    }

    public void setBodyRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Rotation(value));
    }

    @NotNull
    public Vector getLeftArmRotation() {
        return super.metadata.getIndex(OFFSET + 3, new Vector(-10D, 0D, -10D));
    }

    public void setLeftArmRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Rotation(value));
    }

    @NotNull
    public Vector getRightArmRotation() {
        return super.metadata.getIndex(OFFSET + 4, new Vector(-15D, 0D, 10D));
    }

    public void setRightArmRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Rotation(value));
    }

    @NotNull
    public Vector getLeftLegRotation() {
        return super.metadata.getIndex(OFFSET + 5, new Vector(-1D, 0D, -1D));
    }

    public void setLeftLegRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.Rotation(value));
    }

    @NotNull
    public Vector getRightLegRotation() {
        return super.metadata.getIndex(OFFSET + 6, new Vector(1D, 0D, 1D));
    }

    public void setRightLegRotation(@NotNull Vector value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.Rotation(value));
    }

}
