package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractGolemMeta extends PathfinderMobMeta {
    protected AbstractGolemMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
