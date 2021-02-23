package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityPiglinBrute extends EntityBasePiglin {

    public EntityPiglinBrute(@NotNull Position spawnPosition) {
        super(EntityType.PIGLIN_BRUTE, spawnPosition);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public EntityPiglinBrute(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.PIGLIN_BRUTE, spawnPosition, instance);
        setBoundingBox(.6D, 1.95D, .6D);
    }

}
