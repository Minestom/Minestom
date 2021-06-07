package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GlowFrameMeta extends ItemFrameMeta {
    public static final byte OFFSET = ItemFrameMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public GlowFrameMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }
}
