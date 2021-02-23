package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityIllusioner extends EntitySpellcasterIllager {

    public EntityIllusioner(@NotNull Position spawnPosition) {
        super(EntityType.ILLUSIONER, spawnPosition);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public EntityIllusioner(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.ILLUSIONER, spawnPosition, instance);
        setBoundingBox(.6D, 1.95D, .6D);
    }

}
