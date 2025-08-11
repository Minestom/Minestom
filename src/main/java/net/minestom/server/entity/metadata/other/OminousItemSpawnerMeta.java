package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;

public class OminousItemSpawnerMeta extends EntityMeta {
    public OminousItemSpawnerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.OminousItemSpawner.ITEM);
    }

    public void setItem(ItemStack value) {
        metadata.set(MetadataDef.OminousItemSpawner.ITEM, value);
    }

}
