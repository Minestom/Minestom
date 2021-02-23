package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityChestedHorse extends EntityAbstractHorse {

    EntityChestedHorse(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    EntityChestedHorse(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean hasChest() {
        return this.metadata.getIndex((byte) 18, false);
    }

    public void setChest(boolean value) {
        this.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

}
