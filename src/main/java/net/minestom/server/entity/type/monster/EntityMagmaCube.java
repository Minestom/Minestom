package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class EntityMagmaCube extends EntitySlime {

    public EntityMagmaCube(@NotNull Position spawnPosition) {
        super(EntityType.MAGMA_CUBE, spawnPosition);
    }

}
