package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityHoglin extends AgeableCreature implements Animal {

    public EntityHoglin(@NotNull Position spawnPosition) {
        super(EntityType.HOGLIN, spawnPosition);
        setBoundingBox(1.3965D, 1.4D, 1.3965D);
    }

    public EntityHoglin(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.HOGLIN, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.4D, 1.3965D);
    }

    public boolean isImmuneToZombification() {
        return this.metadata.getIndex((byte) 16, false);
    }

    public void setImmuneToZombification(boolean value) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

}
