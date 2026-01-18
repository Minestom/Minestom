package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Rotation;

public sealed class ItemFrameMeta extends HangingMeta permits GlowItemFrameMeta {
    public ItemFrameMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return get(MetadataDef.ItemFrame.ITEM);
    }

    public void setItem(ItemStack value) {
        set(MetadataDef.ItemFrame.ITEM, value);
    }

    public Rotation getRotation() {
        return Rotation.values()[get(MetadataDef.ItemFrame.ROTATION)];
    }

    public void setRotation(Rotation value) {
        set(MetadataDef.ItemFrame.ROTATION, value.ordinal());
    }

}
