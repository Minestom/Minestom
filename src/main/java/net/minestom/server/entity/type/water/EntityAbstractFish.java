package net.minestom.server.entity.type.water;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityAbstractFish extends EntityCreature {

    protected EntityAbstractFish(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    protected EntityAbstractFish(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean isFromBucket() {
        return this.metadata.getIndex((byte) 15, false);
    }

    public void setFromBucket(boolean value) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

}
