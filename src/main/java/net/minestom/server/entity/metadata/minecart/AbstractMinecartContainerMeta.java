package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecartContainerMeta extends AbstractMinecartMeta {
    protected AbstractMinecartContainerMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
