package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

class ItemContainingMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final ItemStack defaultItem;

    protected ItemContainingMeta(@NotNull Entity entity, @NotNull Metadata metadata, @NotNull Material defaultItemMaterial) {
        super(entity, metadata);
        this.defaultItem = ItemStack.of(defaultItemMaterial);
    }

    @NotNull
    public ItemStack getItem() {
        return super.metadata.getIndex(OFFSET, this.defaultItem);
    }

    public void setItem(@NotNull ItemStack item) {
        super.metadata.setIndex(OFFSET, Metadata.Slot(item));
    }

}
