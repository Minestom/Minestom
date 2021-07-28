package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.water.WaterAnimalMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractFishMeta extends WaterAnimalMeta {
    public static final byte OFFSET = WaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected AbstractFishMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isFromBucket() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setFromBucket(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }
}
