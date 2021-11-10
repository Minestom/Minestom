package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CodMeta extends AbstractFishMeta {
    public static final byte OFFSET = AbstractFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public CodMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
