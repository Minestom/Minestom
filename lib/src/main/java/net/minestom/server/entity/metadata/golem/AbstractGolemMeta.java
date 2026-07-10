package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import org.jetbrains.annotations.Nullable;

public class AbstractGolemMeta extends PathfinderMobMeta {
    protected AbstractGolemMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
