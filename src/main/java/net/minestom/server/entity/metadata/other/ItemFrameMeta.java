package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.Nullable;

public class ItemFrameMeta extends HangingMeta {
    public ItemFrameMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.ItemFrame.ITEM);
    }

    public void setItem(ItemStack value) {
        metadata.set(MetadataDef.ItemFrame.ITEM, value);
    }

    public Rotation getRotation() {
        return Rotation.values()[metadata.get(MetadataDef.ItemFrame.ROTATION)];
    }

    public void setRotation(Rotation value) {
        metadata.set(MetadataDef.ItemFrame.ROTATION, value.ordinal());
    }

}
