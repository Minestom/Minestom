package net.minestom.server.entity.metadata.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractDisplayMeta extends EntityMeta {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 15;

    protected AbstractDisplayMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getTransformationInterpolationStartDelta() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setTransformationInterpolationStartDelta(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getTransformationInterpolationDuration() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    public void setTransformationInterpolationDuration(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public int getPosRotInterpolationDuration() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setPosRotInterpolationDuration(int value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value));
    }

    public @NotNull Point getTranslation() {
        return super.metadata.getIndex(OFFSET + 3, Vec.ZERO);
    }

    public void setTranslation(@NotNull Point value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Vector3(value));
    }

    public @NotNull Vec getScale() {
        return super.metadata.getIndex(OFFSET + 4, Vec.ONE);
    }

    public void setScale(@NotNull Vec value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Vector3(value));
    }

    public float @NotNull[] getLeftRotation() {
        //todo replace with actual quaternion type
        return super.metadata.getIndex(OFFSET + 5, new float[] {0, 0, 0, 1});
    }

    public void setLeftRotation(float @NotNull[] value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.Quaternion(value));
    }

    public float @NotNull[] getRightRotation() {
        //todo replace with actual quaternion type
        return super.metadata.getIndex(OFFSET + 6, new float[] {0, 0, 0, 1});
    }

    public void setRightRotation(float @NotNull[] value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.Quaternion(value));
    }

    public @NotNull BillboardConstraints getBillboardRenderConstraints() {
        return BillboardConstraints.VALUES[super.metadata.getIndex(OFFSET + 7, (byte) 0)];
    }

    public void setBillboardRenderConstraints(@NotNull BillboardConstraints value) {
        super.metadata.setIndex(OFFSET + 7, Metadata.Byte((byte) value.ordinal()));
    }

    public int getBrightnessOverride() {
        return super.metadata.getIndex(OFFSET + 8, -1);
    }

    public void setBrightnessOverride(int value) {
        super.metadata.setIndex(OFFSET + 8, Metadata.VarInt(value));
    }

    public float getViewRange() {
        return super.metadata.getIndex(OFFSET + 9, 1.0F);
    }

    public void setViewRange(float value) {
        super.metadata.setIndex(OFFSET + 9, Metadata.Float(value));
    }

    public float getShadowRadius() {
        return super.metadata.getIndex(OFFSET + 10, 0.0F);
    }

    public void setShadowRadius(float value) {
        super.metadata.setIndex(OFFSET + 10, Metadata.Float(value));
    }

    public float getShadowStrength() {
        return super.metadata.getIndex(OFFSET + 11, 1.0F);
    }

    public void setShadowStrength(float value) {
        super.metadata.setIndex(OFFSET + 11, Metadata.Float(value));
    }

    public float getWidth() {
        return super.metadata.getIndex(OFFSET + 12, 0.0F);
    }

    public void setWidth(float value) {
        super.metadata.setIndex(OFFSET + 12, Metadata.Float(value));
    }

    public float getHeight() {
        return super.metadata.getIndex(OFFSET + 13, 0.0F);
    }

    public void setHeight(float value) {
        super.metadata.setIndex(OFFSET + 13, Metadata.Float(value));
    }

    public int getGlowColorOverride() {
        return super.metadata.getIndex(OFFSET + 14, 0);
    }

    public void setGlowColorOverride(int value) {
        super.metadata.setIndex(OFFSET + 14, Metadata.VarInt(value));
    }

    public enum BillboardConstraints {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER;

        private static final BillboardConstraints[] VALUES = values(); //Microtus - update java keyword usage
    }
}
