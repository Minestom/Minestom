package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;

public class EyeOfEnderMeta extends EntityMeta {
    public EyeOfEnderMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.EyeOfEnder.ITEM);
    }

    public void setItem(ItemStack value) {
        metadata.set(MetadataDef.EyeOfEnder.ITEM, value);
    }

}
