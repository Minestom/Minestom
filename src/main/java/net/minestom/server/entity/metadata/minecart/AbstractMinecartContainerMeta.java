package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;

public sealed abstract class AbstractMinecartContainerMeta extends AbstractMinecartMeta permits ChestMinecartMeta, HopperMinecartMeta {
    protected AbstractMinecartContainerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
