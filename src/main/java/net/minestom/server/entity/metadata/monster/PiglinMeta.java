package net.minestom.server.entity.metadata.monster;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PiglinMeta extends BasePiglinMeta {
    public static final byte OFFSET = BasePiglinMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public PiglinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getBoundingBox();
            if (value) {
                double width = bb.getWidth() / 2;
                entity.setBoundingBox(width, bb.getHeight() / 2, width);
            } else {
                double width = bb.getWidth() * 2;
                entity.setBoundingBox(width, bb.getHeight() * 2, width);
            }
        });
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    public boolean isChargingCrossbow() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setChargingCrossbow(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isDancing() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setDancing(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

}
