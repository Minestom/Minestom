package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityAbstractVillager extends AgeableCreature {

    EntityAbstractVillager(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    EntityAbstractVillager(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public int getHeadShakeTimer() {
        return this.metadata.getIndex((byte) 16, 0);
    }

    public void setHeadShakeTimer(int value) {
        this.metadata.setIndex((byte) 16, Metadata.VarInt(value));
    }

}
