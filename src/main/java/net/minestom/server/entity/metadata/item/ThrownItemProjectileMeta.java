package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

class ThrownItemProjectileMeta extends EntityMeta {
    protected ThrownItemProjectileMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getItem() {
        return metadata.get(MetadataDef.ThrownItemProjectile.ITEM);
    }

    public void setItem(@NotNull ItemStack item) {
        metadata.set(MetadataDef.ThrownItemProjectile.ITEM, item);
    }

}
