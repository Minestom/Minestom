package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class GiantMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public GiantMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
