package net.minestom.server.entity.type.water;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySalmon extends EntityAbstractFish {

    public EntitySalmon(@NotNull Position spawnPosition) {
        super(EntityType.SALMON, spawnPosition);
        setBoundingBox(.7D, .4D, .7D);
    }

    public EntitySalmon(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.SALMON, spawnPosition, instance);
        setBoundingBox(.7D, .4D, .7D);
    }

}
