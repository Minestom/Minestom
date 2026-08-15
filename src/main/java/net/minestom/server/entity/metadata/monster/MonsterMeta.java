package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.Nullable;

public class MonsterMeta extends PathfinderMobMeta {
    protected MonsterMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
