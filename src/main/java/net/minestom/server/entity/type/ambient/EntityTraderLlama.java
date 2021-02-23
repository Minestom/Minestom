package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityTraderLlama extends EntityCreature {

    public EntityTraderLlama(@NotNull Position spawnPosition) {
        super(EntityType.TRADER_LLAMA, spawnPosition);
        setBoundingBox(.9D, 1.87D, .9D);
    }

    public EntityTraderLlama(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.TRADER_LLAMA, spawnPosition, instance);
        setBoundingBox(.9D, 1.87D, .9D);
    }

}
