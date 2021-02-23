package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityMule extends EntityChestedHorse {

    public EntityMule(@NotNull Position spawnPosition) {
        super(EntityType.MULE, spawnPosition);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

    public EntityMule(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.MULE, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

}
