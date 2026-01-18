package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;

public sealed abstract class AbstractGolemMeta extends PathfinderMobMeta permits CopperGolemMeta, IronGolemMeta, ShulkerMeta, SnowGolemMeta {
    protected AbstractGolemMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
