package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

class ThrownItemProjectileMeta extends EntityMeta {
    protected ThrownItemProjectileMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.ThrownItemProjectile.ITEM);
    }

    public void setItem(ItemStack item) {
        metadata.set(MetadataDef.ThrownItemProjectile.ITEM, item);
    }

}
