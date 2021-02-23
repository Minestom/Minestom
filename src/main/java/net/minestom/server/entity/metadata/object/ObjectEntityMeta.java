package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ObjectEntityMeta extends BaseEntityMeta {

    private final ItemStack defaultItem;

    protected ObjectEntityMeta(@NotNull Entity entity, @NotNull Metadata metadata, @NotNull Material defaultItemMaterial) {
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
