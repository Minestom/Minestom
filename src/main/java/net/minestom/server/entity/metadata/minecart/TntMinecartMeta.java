package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class TntMinecartMeta extends AbstractMinecartMeta {
    public TntMinecartMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public int getObjectData() {
        return 3;
    }

}
