package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityEvoker extends EntitySpellcasterIllager {

    public EntityEvoker(@NotNull Position spawnPosition) {
        super(EntityType.EVOKER, spawnPosition);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public EntityEvoker(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.EVOKER, spawnPosition, instance);
        setBoundingBox(.6D, 1.95D, .6D);
    }

}
