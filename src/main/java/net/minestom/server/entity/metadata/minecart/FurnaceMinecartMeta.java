package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class FurnaceMinecartMeta extends AbstractMinecartMeta {
    public FurnaceMinecartMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasFuel() {
        return metadata.get(MetadataDef.MinecartFurnace.HAS_FUEL);
    }

    public void setHasFuel(boolean value) {
        metadata.set(MetadataDef.MinecartFurnace.HAS_FUEL, value);
    }

}
