package net.minestom.server.entity.type.water;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityCod extends EntityAbstractFish {

    public EntityCod(@NotNull Position spawnPosition) {
        super(EntityType.COD, spawnPosition);
        setBoundingBox(.5D, .3D, .5D);
    }

    public EntityCod(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.COD, spawnPosition, instance);
        setBoundingBox(.5D, .3D, .5D);
    }

}
