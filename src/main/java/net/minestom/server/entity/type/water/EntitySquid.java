package net.minestom.server.entity.type.water;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySquid extends EntityCreature {

    public EntitySquid(@NotNull Position spawnPosition) {
        super(EntityType.SQUID, spawnPosition);
        setBoundingBox(.8D, .8D, .8D);
    }

    public EntitySquid(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.SQUID, spawnPosition, instance);
        setBoundingBox(.8D, .8D, .8D);
    }

}
