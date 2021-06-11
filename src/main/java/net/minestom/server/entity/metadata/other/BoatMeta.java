package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class BoatMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 7;

    public BoatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getTimeSinceLastHit() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setTimeSinceLastHit(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getForwardDirection() {
        return super.metadata.getIndex(OFFSET + 1, 1);
    }

    public void setForwardDirection(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public float getDamageTaken() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setDamageTaken(float value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Float(value));
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[super.metadata.getIndex(OFFSET + 3, 0)];
    }

    public void setType(@NotNull Type value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLeftPaddleTurning() {
        return super.metadata.getIndex(OFFSET + 4, false);
    }

    public void setLeftPaddleTurning(boolean value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Boolean(value));
    }

    public boolean isRightPaddleTurning() {
        return super.metadata.getIndex(OFFSET + 5, false);
    }

    public void setRightPaddleTurning(boolean value) {
        super.metadata.setIndex(OFFSET + 5, Metadata.Boolean(value));
    }

    public int getSplashTimer() {
        return super.metadata.getIndex(OFFSET + 6, 0);
    }

    public void setSplashTimer(int value) {
        super.metadata.setIndex(OFFSET + 6, Metadata.VarInt(value));
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
