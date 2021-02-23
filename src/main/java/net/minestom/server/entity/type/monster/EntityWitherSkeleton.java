package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityWitherSkeleton extends EntitySkeleton {

    public EntityWitherSkeleton(@NotNull Position spawnPosition) {
        super(EntityType.WITHER_SKELETON, spawnPosition);
        setBoundingBox(.7D, 2.4D, .7D);
    }

    public EntityWitherSkeleton(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.WITHER_SKELETON, spawnPosition, instance);
        setBoundingBox(.7D, 2.4D, .7D);
    }

}
