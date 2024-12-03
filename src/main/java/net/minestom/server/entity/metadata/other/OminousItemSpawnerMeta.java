package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OminousItemSpawnerMeta extends EntityMeta {
    public OminousItemSpawnerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull ItemStack getItem() {
        return metadata.get(MetadataDef.OminousItemSpawner.ITEM);
    }

    public void setItem(@NotNull ItemStack value) {
        metadata.set(MetadataDef.OminousItemSpawner.ITEM, value);
    }

}
