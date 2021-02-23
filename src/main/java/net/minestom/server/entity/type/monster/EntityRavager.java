package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityRavager extends EntityRaider {

    public EntityRavager(@NotNull Position spawnPosition) {
        super(EntityType.RAVAGER, spawnPosition);
        setBoundingBox(1.95D, 2.2D, 1.95D);
    }

    public EntityRavager(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.RAVAGER, spawnPosition, instance);
        setBoundingBox(1.95D, 2.2D, 1.95D);
    }

}
