package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;

public sealed abstract class FlyingMeta extends MobMeta permits GhastMeta, PhantomMeta {
    protected FlyingMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
