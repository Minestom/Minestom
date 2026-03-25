package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;

public sealed abstract class AmbientCreatureMeta extends MobMeta permits BatMeta {
    protected AmbientCreatureMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
