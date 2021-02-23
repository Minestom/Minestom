package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySkeletonHorse extends EntityAbstractHorse {

    public EntitySkeletonHorse(@NotNull Position spawnPosition) {
        super(EntityType.SKELETON_HORSE, spawnPosition);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

    public EntitySkeletonHorse(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.SKELETON_HORSE, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

}
