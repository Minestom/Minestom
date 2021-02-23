package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemFrameMeta extends BaseEntityMeta {

    public ItemFrameMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public ItemStack getItem() {
        return getMetadata().getIndex((byte) 7, ItemStack.getAirItem());
    }

    public void setItem(@NotNull ItemStack value) {
        getMetadata().setIndex((byte) 7, Metadata.Slot(value));
    }

    public int getRotation() {
        return getMetadata().getIndex((byte) 8, 0);
    }

    public void setRotation(int value) {
        getMetadata().setIndex((byte) 8, Metadata.VarInt(value));
    }

}
