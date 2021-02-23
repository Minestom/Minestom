package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class FireballMeta extends ObjectEntityMeta {

    public FireballMeta(@NotNull Entity entity) {
        super(entity, Material.AIR);
    }

}
