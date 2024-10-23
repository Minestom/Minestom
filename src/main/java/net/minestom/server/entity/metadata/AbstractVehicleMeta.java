package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class AbstractVehicleMeta extends EntityMeta {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public AbstractVehicleMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getShakingTicks() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setShakingTicks(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getShakingDirection() {
        return super.metadata.getIndex(OFFSET + 1, 1);
    }

    public void setShakingDirection(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public float getShakingMultiplier() {
        return super.metadata.getIndex(OFFSET + 2, 0);
    }

    public void setShakingMultiplier(float value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Float(value));
    }
}
