package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.water.WaterAnimalMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractFishMeta extends WaterAnimalMeta {

    protected AbstractFishMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isFromBucket() {
        return getMetadata().getIndex((byte) 15, false);
    }

    public void setFromBucket(boolean value) {
        getMetadata().setIndex((byte) 15, Metadata.Boolean(value));
    }
}
