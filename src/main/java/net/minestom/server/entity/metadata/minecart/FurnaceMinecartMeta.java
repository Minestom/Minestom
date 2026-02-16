package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class FurnaceMinecartMeta extends AbstractMinecartMeta {
    public FurnaceMinecartMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasFuel() {
        return get(MetadataDef.MinecartFurnace.HAS_FUEL);
    }

    public void setHasFuel(boolean value) {
        set(MetadataDef.MinecartFurnace.HAS_FUEL, value);
    }

}
