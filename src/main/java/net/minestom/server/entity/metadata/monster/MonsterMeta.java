package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.NotNull;

public class MonsterMeta extends PathfinderMobMeta {

    protected MonsterMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
