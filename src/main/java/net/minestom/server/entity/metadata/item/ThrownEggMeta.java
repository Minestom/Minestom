package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ThrownEggMeta extends ItemContainingMeta {

    public ThrownEggMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata, Material.EGG);
    }

}
