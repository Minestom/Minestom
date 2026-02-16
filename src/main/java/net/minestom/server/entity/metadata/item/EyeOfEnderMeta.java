package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;

public final class EyeOfEnderMeta extends EntityMeta {
    public EyeOfEnderMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return get(MetadataDef.EyeOfEnder.ITEM);
    }

    public void setItem(ItemStack value) {
        set(MetadataDef.EyeOfEnder.ITEM, value);
    }

}
