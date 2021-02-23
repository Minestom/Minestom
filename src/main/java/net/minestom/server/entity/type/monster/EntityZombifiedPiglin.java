package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class EntityZombifiedPiglin extends EntityZombie {

    public EntityZombifiedPiglin(@NotNull Position spawnPosition) {
        super(EntityType.ZOMBIFIED_PIGLIN, spawnPosition);
    }

}
