package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

class ItemContainingMeta extends EntityMeta {

    private final ItemStack defaultItem;

    protected ItemContainingMeta(@NotNull Entity entity, @NotNull Metadata metadata, @NotNull Material defaultItemMaterial) {
        super(entity, metadata);
        this.defaultItem = new ItemStack(defaultItemMaterial, (byte) 1);
    }

    @NotNull
    public ItemStack getItem() {
        return super.metadata.getIndex((byte) 7, this.defaultItem);
    }

    public void setItem(@NotNull ItemStack item) {
        super.metadata.setIndex((byte) 7, Metadata.Slot(item));
    }

}
