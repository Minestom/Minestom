package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;
import org.jetbrains.annotations.NotNull;

public class AmbientCreatureMeta extends MobMeta {
    protected AmbientCreatureMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
