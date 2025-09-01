package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;

public class HopperMinecartMeta extends AbstractMinecartContainerMeta {
    public HopperMinecartMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public int getObjectData() {
        return 5;
    }

}
