package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CowMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public CowMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
