package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SquidMeta extends WaterAnimalMeta {
    public static final byte OFFSET = WaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public SquidMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
