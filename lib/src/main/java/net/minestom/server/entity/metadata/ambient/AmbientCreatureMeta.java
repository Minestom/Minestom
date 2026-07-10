package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;
import org.jetbrains.annotations.Nullable;

public class AmbientCreatureMeta extends MobMeta {
    protected AmbientCreatureMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
