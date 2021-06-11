package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PiglinBruteMeta extends BasePiglinMeta {
    public static final byte OFFSET = BasePiglinMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public PiglinBruteMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
