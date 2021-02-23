package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class EyeOfEnderMeta extends ObjectEntityMeta {

    public EyeOfEnderMeta(@NotNull Entity entity) {
        super(entity, Material.ENDER_EYE);
    }

}
