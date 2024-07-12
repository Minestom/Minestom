package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OminousItemSpawnerMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public OminousItemSpawnerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull ItemStack getItem() {
        return super.metadata.getIndex(OFFSET, ItemStack.AIR);
    }

    public void setItem(@NotNull ItemStack value) {
        super.metadata.setIndex(OFFSET, Metadata.ItemStack(value));
    }

}
