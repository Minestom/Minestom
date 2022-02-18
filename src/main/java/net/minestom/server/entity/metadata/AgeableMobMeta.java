package net.minestom.server.entity.metadata;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class AgeableMobMeta extends PathfinderMobMeta {
    public static final byte OFFSET = PathfinderMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected AgeableMobMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
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

}
