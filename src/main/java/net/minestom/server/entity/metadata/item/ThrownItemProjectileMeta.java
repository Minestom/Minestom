package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;

class ThrownItemProjectileMeta extends EntityMeta {
    protected ThrownItemProjectileMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.ThrownItemProjectile.ITEM);
    }

    public void setItem(ItemStack item) {
        metadata.set(MetadataDef.ThrownItemProjectile.ITEM, item);
    }

}
