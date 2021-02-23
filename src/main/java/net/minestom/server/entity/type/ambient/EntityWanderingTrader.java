package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityWanderingTrader extends EntityAbstractVillager {

    public EntityWanderingTrader(@NotNull Position spawnPosition) {
        super(EntityType.WANDERING_TRADER, spawnPosition);
    }

    public EntityWanderingTrader(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.WANDERING_TRADER, spawnPosition, instance);
    }

}
