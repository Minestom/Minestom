package net.minestom.server.entity.metadata.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractDisplayMeta extends EntityMeta {
    protected AbstractDisplayMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getTransformationInterpolationStartDelta() {
        return metadata.get(MetadataDef.Display.INTERPOLATION_DELAY);
    }

    public void setTransformationInterpolationStartDelta(int value) {
        metadata.set(MetadataDef.Display.INTERPOLATION_DELAY, value);
    }

    public int getTransformationInterpolationDuration() {
        return metadata.get(MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION);
    }

    public void setTransformationInterpolationDuration(int value) {
        metadata.set(MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION, value);
    }

    public int getPosRotInterpolationDuration() {
        return metadata.get(MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION);
    }

    public void setPosRotInterpolationDuration(int value) {
        metadata.set(MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION, value);
    }

    public @NotNull Point getTranslation() {
        return metadata.get(MetadataDef.Display.TRANSLATION);
    }

    public void setTranslation(@NotNull Point value) {
        metadata.set(MetadataDef.Display.TRANSLATION, value);
    }

    public @NotNull Vec getScale() {
        return Vec.fromPoint(metadata.get(MetadataDef.Display.SCALE));
    }

    public void setScale(@NotNull Vec value) {
        metadata.set(MetadataDef.Display.SCALE, value);
    }

    public float @NotNull[] getLeftRotation() {
        //todo replace with actual quaternion type
        return metadata.get(MetadataDef.Display.ROTATION_LEFT);
    }

    public void setLeftRotation(float @NotNull[] value) {
        metadata.set(MetadataDef.Display.ROTATION_LEFT, value);
    }

    public float @NotNull[] getRightRotation() {
        //todo replace with actual quaternion type
        return metadata.get(MetadataDef.Display.ROTATION_RIGHT);
    }

    public void setRightRotation(float @NotNull[] value) {
        metadata.set(MetadataDef.Display.ROTATION_RIGHT, value);
    }

    public @NotNull BillboardConstraints getBillboardRenderConstraints() {
        return BillboardConstraints.VALUES[metadata.get(MetadataDef.Display.BILLBOARD_CONSTRAINTS)];
    }

    public void setBillboardRenderConstraints(@NotNull BillboardConstraints value) {
        metadata.set(MetadataDef.Display.BILLBOARD_CONSTRAINTS, (byte) value.ordinal());
    }

    public int getBrightnessOverride() {
        return metadata.get(MetadataDef.Display.BRIGHTNESS_OVERRIDE);
    }

    public void setBrightnessOverride(int value) {
        metadata.set(MetadataDef.Display.BRIGHTNESS_OVERRIDE, value);
    }

    public void setBrightness(int blockLight, int skyLight) {
        setBrightnessOverride((blockLight & 0xF) << 4 | (skyLight & 0xF) << 20);
    }

    public int getBlockLight() {
        return getLight(4);
    }

    public int getSkyLight() {
        return getLight(20);
    }

    private int getLight(int shift) {
        int brightnessOverride = getBrightnessOverride();
        if (brightnessOverride <= 0)
            return 0;
        else
            return (brightnessOverride >> shift) & 0xF;
    }

    public float getViewRange() {
        return metadata.get(MetadataDef.Display.VIEW_RANGE);
    }

    public void setViewRange(float value) {
        metadata.set(MetadataDef.Display.VIEW_RANGE, value);
    }

    public float getShadowRadius() {
        return metadata.get(MetadataDef.Display.SHADOW_RADIUS);
    }

    public void setShadowRadius(float value) {
        metadata.set(MetadataDef.Display.SHADOW_RADIUS, value);
    }

    public float getShadowStrength() {
        return metadata.get(MetadataDef.Display.SHADOW_STRENGTH);
    }

    public void setShadowStrength(float value) {
        metadata.set(MetadataDef.Display.SHADOW_STRENGTH, value);
    }

    public float getWidth() {
        return metadata.get(MetadataDef.Display.WIDTH);
    }

    public void setWidth(float value) {
        metadata.set(MetadataDef.Display.WIDTH, value);
    }

    public float getHeight() {
        return metadata.get(MetadataDef.Display.HEIGHT);
    }

    public void setHeight(float value) {
        metadata.set(MetadataDef.Display.HEIGHT, value);
    }

    public int getGlowColorOverride() {
        return metadata.get(MetadataDef.Display.GLOW_COLOR_OVERRIDE);
    }

    public void setGlowColorOverride(int value) {
        metadata.set(MetadataDef.Display.GLOW_COLOR_OVERRIDE, value);
    }

    public enum BillboardConstraints {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER;

        private final static BillboardConstraints[] VALUES = values();
    }

}
