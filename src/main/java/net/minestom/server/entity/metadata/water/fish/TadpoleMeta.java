package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class TadpoleMeta extends AbstractFishMeta {
    public static final byte OFFSET = AbstractFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public TadpoleMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
