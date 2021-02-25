package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class BoatMeta extends EntityMeta {

    public BoatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getTimeSinceLastHit() {
        return super.metadata.getIndex((byte) 7, 0);
    }

    public void setTimeSinceLastHit(int value) {
        super.metadata.setIndex((byte) 7, Metadata.VarInt(value));
    }

    public int getForwardDirection() {
        return super.metadata.getIndex((byte) 8, 1);
    }

    public void setForwardDirection(int value) {
        super.metadata.setIndex((byte) 8, Metadata.VarInt(value));
    }

    public float getDamageTaken() {
        return super.metadata.getIndex((byte) 9, 0);
    }

    public void setDamageTaken(float value) {
        super.metadata.setIndex((byte) 9, Metadata.Float(value));
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[super.metadata.getIndex((byte) 10, 0)];
    }

    public void setType(@NotNull Type value) {
        super.metadata.setIndex((byte) 10, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLeftPaddleTurning() {
        return super.metadata.getIndex((byte) 11, false);
    }

    public void setLeftPaddleTurning(boolean value) {
        super.metadata.setIndex((byte) 11, Metadata.Boolean(value));
    }

    public boolean isRightPaddleTurning() {
        return super.metadata.getIndex((byte) 12, false);
    }

    public void setRightPaddleTurning(boolean value) {
        super.metadata.setIndex((byte) 12, Metadata.Boolean(value));
    }

    public int getSplashTimer() {
        return super.metadata.getIndex((byte) 13, 0);
    }

    public void setSplashTimer(int value) {
        super.metadata.setIndex((byte) 13, Metadata.VarInt(value));
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
