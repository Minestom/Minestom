package net.minestom.server.entity.metadata.object;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ThrownExperienceBottleMeta extends ObjectEntityMeta {

    public ThrownExperienceBottleMeta(@NotNull Entity entity) {
        super(entity, Material.EXPERIENCE_BOTTLE);
    }

}
