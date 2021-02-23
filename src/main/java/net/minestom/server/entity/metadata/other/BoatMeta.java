package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import org.jetbrains.annotations.NotNull;

public class BoatMeta extends BaseEntityMeta {

    public BoatMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getTimeSinceLastHit() {
        return getMetadata().getIndex((byte) 7, 0);
    }

    public void setTimeSinceLastHit(int value) {
        getMetadata().setIndex((byte) 7, Metadata.VarInt(value));
    }

    public int getForwardDirection() {
        return getMetadata().getIndex((byte) 8, 1);
    }

    public void setForwardDirection(int value) {
        getMetadata().setIndex((byte) 8, Metadata.VarInt(value));
    }

    public float getDamageTaken() {
        return getMetadata().getIndex((byte) 9, 0);
    }

    public void setDamageTaken(float value) {
        getMetadata().setIndex((byte) 9, Metadata.Float(value));
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[getMetadata().getIndex((byte) 10, 0)];
    }

    public void setType(@NotNull Type value) {
        getMetadata().setIndex((byte) 10, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLeftPaddleTurning() {
        return getMetadata().getIndex((byte) 11, false);
    }

    public void setLeftPaddleTurning(boolean value) {
        getMetadata().setIndex((byte) 11, Metadata.Boolean(value));
    }

    public boolean isRightPaddleTurning() {
        return getMetadata().getIndex((byte) 12, false);
    }

    public void setRightPaddleTurning(boolean value) {
        getMetadata().setIndex((byte) 12, Metadata.Boolean(value));
    }

    public int getSplashTimer() {
        return getMetadata().getIndex((byte) 13, 0);
    }

    public void setSplashTimer(int value) {
        getMetadata().setIndex((byte) 13, Metadata.VarInt(value));
    }

    public enum Type {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        DARK_OAK;

        private final static Type[] VALUES = values();
    }

}
