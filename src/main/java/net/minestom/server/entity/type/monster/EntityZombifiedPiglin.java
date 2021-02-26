package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.zombie.ZombifiedPiglinMeta} instead.
 */
@Deprecated
public class EntityZombifiedPiglin extends EntityZombie {

    public EntityZombifiedPiglin(@NotNull Position spawnPosition) {
        super(EntityType.ZOMBIFIED_PIGLIN, spawnPosition);
    }

}
