package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityElderGuardian extends EntityGuardian {

    public EntityElderGuardian(@NotNull Position spawnPosition) {
        super(EntityType.ELDER_GUARDIAN, spawnPosition);
        setBoundingBox(1.9975D, 1.9975D, 1.9975D);
    }

    public EntityElderGuardian(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.ELDER_GUARDIAN, spawnPosition, instance);
        setBoundingBox(1.9975D, 1.9975D, 1.9975D);
    }

}
