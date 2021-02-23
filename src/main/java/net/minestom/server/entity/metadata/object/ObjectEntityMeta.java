package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ObjectEntityMeta extends BaseEntityMeta {

    private final ItemStack defaultItem;

    protected ObjectEntityMeta(@NotNull Entity entity, @NotNull Material defaultItemMaterial) {
        super(entity);
        this.defaultItem = new ItemStack(defaultItemMaterial, (byte) 1);
    }

    @NotNull
    public ItemStack getItem() {
        return getMetadata().getIndex((byte) 7, this.defaultItem);
    }

    public void setItem(@NotNull ItemStack item) {
        getMetadata().setIndex((byte) 7, Metadata.Slot(item));
    }

}
