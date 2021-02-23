package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityDonkey extends EntityChestedHorse {

    public EntityDonkey(@NotNull Position spawnPosition) {
        super(EntityType.DONKEY, spawnPosition);
        setBoundingBox(1.3965D, 1.5D, 1.3965D);
    }

    public EntityDonkey(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.DONKEY, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.5D, 1.3965D);
    }

}
