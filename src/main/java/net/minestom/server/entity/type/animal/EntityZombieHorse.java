package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityZombieHorse extends EntityAbstractHorse {

    public EntityZombieHorse(@NotNull Position spawnPosition) {
        super(EntityType.ZOMBIE_HORSE, spawnPosition);
    }

    public EntityZombieHorse(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.ZOMBIE_HORSE, spawnPosition, instance);
    }

}
