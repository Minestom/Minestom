package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class EyeOfEnderMeta extends ItemContainingMeta {
    public static final byte OFFSET = ItemContainingMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public EyeOfEnderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata, Material.ENDER_EYE);
    }

}
