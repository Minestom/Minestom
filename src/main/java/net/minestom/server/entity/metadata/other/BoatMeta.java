package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class BoatMeta extends AbstractVehicleMeta {
    public static final byte OFFSET = AbstractVehicleMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

    public BoatMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Type getType() {
        return Type.VALUES[super.metadata.getIndex(OFFSET, 0)];
    }

    public void setType(@NotNull Type value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLeftPaddleTurning() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setLeftPaddleTurning(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isRightPaddleTurning() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setRightPaddleTurning(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

    public int getSplashTimer() {
        return super.metadata.getIndex(OFFSET + 3, 0);
    }

    public void setSplashTimer(int value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value));
    }

    public enum Type {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        CHERRY,
        DARK_OAK,
        MANGROVE,
        BAMBOO;

        private final static Type[] VALUES = values();
    }

}
