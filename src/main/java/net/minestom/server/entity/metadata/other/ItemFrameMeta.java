package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

public class ItemFrameMeta extends HangingMeta {
    public ItemFrameMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getItem() {
        return metadata.get(MetadataDef.ItemFrame.ITEM);
    }

    public void setItem(@NotNull ItemStack value) {
        metadata.set(MetadataDef.ItemFrame.ITEM, value);
    }

    @NotNull
    public Rotation getRotation() {
        return Rotation.values()[metadata.get(MetadataDef.ItemFrame.ROTATION)];
    }

    public void setRotation(@NotNull Rotation value) {
        metadata.set(MetadataDef.ItemFrame.ROTATION, value.ordinal());
    }

}
