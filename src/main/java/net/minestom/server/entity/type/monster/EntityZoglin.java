package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityZoglin extends AgeableCreature implements Monster {

    public EntityZoglin(@NotNull Position spawnPosition) {
        super(EntityType.ZOGLIN, spawnPosition);
        setBoundingBox(1.3965D, 1.4D, 1.3965D);
    }

    public EntityZoglin(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.ZOGLIN, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.4D, 1.3965D);
    }

}
