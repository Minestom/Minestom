package net.minestom.server.entity.metadata.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;

public sealed abstract class AbstractDisplayMeta extends EntityMeta permits BlockDisplayMeta, ItemDisplayMeta, TextDisplayMeta {
    protected AbstractDisplayMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getTransformationInterpolationStartDelta() {
        return get(MetadataDef.Display.INTERPOLATION_DELAY);
    }

    public void setTransformationInterpolationStartDelta(int value) {
        set(MetadataDef.Display.INTERPOLATION_DELAY, value);
    }

    public int getTransformationInterpolationDuration() {
        return get(MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION);
    }

    public void setTransformationInterpolationDuration(int value) {
        set(MetadataDef.Display.TRANSFORMATION_INTERPOLATION_DURATION, value);
    }

    public int getPosRotInterpolationDuration() {
        return get(MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION);
    }

    public void setPosRotInterpolationDuration(int value) {
        set(MetadataDef.Display.POSITION_ROTATION_INTERPOLATION_DURATION, value);
    }

    public Point getTranslation() {
        return get(MetadataDef.Display.TRANSLATION);
    }

    public void setTranslation(Point value) {
        set(MetadataDef.Display.TRANSLATION, value);
    }

    public Vec getScale() {
        return get(MetadataDef.Display.SCALE).asVec();
    }

    public void setScale(Vec value) {
        set(MetadataDef.Display.SCALE, value);
    }

    public float [] getLeftRotation() {
        //todo replace with actual quaternion type
        return get(MetadataDef.Display.ROTATION_LEFT);
    }

    public void setLeftRotation(float [] value) {
        set(MetadataDef.Display.ROTATION_LEFT, value);
    }

    public float [] getRightRotation() {
        //todo replace with actual quaternion type
        return get(MetadataDef.Display.ROTATION_RIGHT);
    }

    public void setRightRotation(float [] value) {
        set(MetadataDef.Display.ROTATION_RIGHT, value);
    }

    public BillboardConstraints getBillboardRenderConstraints() {
        return BillboardConstraints.VALUES[get(MetadataDef.Display.BILLBOARD_CONSTRAINTS)];
    }

    public void setBillboardRenderConstraints(BillboardConstraints value) {
        set(MetadataDef.Display.BILLBOARD_CONSTRAINTS, (byte) value.ordinal());
    }

    public int getBrightnessOverride() {
        return get(MetadataDef.Display.BRIGHTNESS_OVERRIDE);
    }

    public void setBrightnessOverride(int value) {
        set(MetadataDef.Display.BRIGHTNESS_OVERRIDE, value);
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
        return get(MetadataDef.Display.VIEW_RANGE);
    }

    public void setViewRange(float value) {
        set(MetadataDef.Display.VIEW_RANGE, value);
    }

    public float getShadowRadius() {
        return get(MetadataDef.Display.SHADOW_RADIUS);
    }

    public void setShadowRadius(float value) {
        set(MetadataDef.Display.SHADOW_RADIUS, value);
    }

    public float getShadowStrength() {
        return get(MetadataDef.Display.SHADOW_STRENGTH);
    }

    public void setShadowStrength(float value) {
        set(MetadataDef.Display.SHADOW_STRENGTH, value);
    }

    public float getWidth() {
        return get(MetadataDef.Display.WIDTH);
    }

    public void setWidth(float value) {
        set(MetadataDef.Display.WIDTH, value);
    }

    public float getHeight() {
        return get(MetadataDef.Display.HEIGHT);
    }

    public void setHeight(float value) {
        set(MetadataDef.Display.HEIGHT, value);
    }

    public int getGlowColorOverride() {
        return get(MetadataDef.Display.GLOW_COLOR_OVERRIDE);
    }

    public void setGlowColorOverride(int value) {
        set(MetadataDef.Display.GLOW_COLOR_OVERRIDE, value);
    }

    public enum BillboardConstraints {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER;

        private final static BillboardConstraints[] VALUES = values();
    }

}
