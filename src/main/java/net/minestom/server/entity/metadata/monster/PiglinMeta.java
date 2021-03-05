package net.minestom.server.entity.metadata.monster;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PiglinMeta extends BasePiglinMeta {

    public PiglinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return super.metadata.getIndex((byte) 16, false);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        BoundingBox bb = this.entity.getBoundingBox();
        if (value) {
            setBoundingBox(bb.getWidth() / 2, bb.getHeight() / 2);
        } else {
            setBoundingBox(bb.getWidth() * 2, bb.getHeight() * 2);
        }
        super.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

    public boolean isChargingCrossbow() {
        return super.metadata.getIndex((byte) 17, false);
    }

    public void setChargingCrossbow(boolean value) {
        super.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isDancing() {
        return super.metadata.getIndex((byte) 18, false);
    }

    public void setDancing(boolean value) {
        super.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

}
