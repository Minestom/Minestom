package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityBasePiglin extends EntityCreature implements Monster {

    EntityBasePiglin(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    EntityBasePiglin(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean isImmuneToZombification() {
        return this.metadata.getIndex((byte) 16, false);
    }

    public void setImmuneToZombification(boolean value) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

}
