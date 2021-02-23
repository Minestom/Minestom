package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class SmallFireballMeta extends ObjectEntityMeta {

    public SmallFireballMeta(@NotNull Entity entity) {
        super(entity, Material.FIRE_CHARGE);
    }

}
