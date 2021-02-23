package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class MinecartFurnaceMeta extends AbstractMinecartMeta {

    public MinecartFurnaceMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isHasFuel() {
        return getMetadata().getIndex((byte) 13, false);
    }

    public void setHasFuel(boolean value) {
        getMetadata().setIndex((byte) 13, Metadata.Boolean(value));
    }

}
