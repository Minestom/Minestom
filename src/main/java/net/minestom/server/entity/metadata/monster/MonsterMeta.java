package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.NotNull;

public class MonsterMeta extends PathfinderMobMeta {
    public static final byte OFFSET = PathfinderMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    protected MonsterMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
