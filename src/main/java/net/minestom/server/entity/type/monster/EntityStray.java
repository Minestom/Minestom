package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityStray extends EntitySkeleton {

    public EntityStray(@NotNull Position spawnPosition) {
        super(EntityType.STRAY, spawnPosition);
        setBoundingBox(.6D, 1.99D, .6D);
    }

    public EntityStray(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.STRAY, spawnPosition, instance);
        setBoundingBox(.6D, 1.99D, .6D);
    }

}
