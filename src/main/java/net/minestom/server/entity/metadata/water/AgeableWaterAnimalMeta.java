package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AgeableWaterAnimalMeta extends AgeableMobMeta {
    public static final byte OFFSET = AgeableMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public AgeableWaterAnimalMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }
}
