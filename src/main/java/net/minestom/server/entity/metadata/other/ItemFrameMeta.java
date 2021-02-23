package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

public class ItemFrameMeta extends EntityMeta {

    public ItemFrameMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getItem() {
        return super.metadata.getIndex((byte) 7, ItemStack.getAirItem());
    }

    public void setItem(@NotNull ItemStack value) {
        super.metadata.setIndex((byte) 7, Metadata.Slot(value));
    }

    public Rotation getRotation() {
        return Rotation.values()[super.metadata.getIndex((byte) 8, 0)];
    }

    public void setRotation(Rotation value) {
        super.metadata.setIndex((byte) 8, Metadata.VarInt(value.ordinal()));
    }

}
