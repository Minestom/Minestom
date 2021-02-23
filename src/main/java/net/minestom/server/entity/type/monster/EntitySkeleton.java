package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySkeleton extends EntityCreature implements Monster {

    public EntitySkeleton(@NotNull Position spawnPosition) {
        this(EntityType.SKELETON, spawnPosition);
        setBoundingBox(.6D, 1.99D, .6D);
    }

    public EntitySkeleton(@NotNull Position spawnPosition, @Nullable Instance instance) {
        this(EntityType.SKELETON, spawnPosition, instance);
        setBoundingBox(.6D, 1.99D, .6D);
    }

    EntitySkeleton(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    EntitySkeleton(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

}
