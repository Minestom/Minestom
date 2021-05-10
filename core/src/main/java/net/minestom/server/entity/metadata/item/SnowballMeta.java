package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class SnowballMeta extends ItemContainingMeta {

    public SnowballMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata, Material.SNOWBALL);
    }

}
