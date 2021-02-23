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
public class EntityRaider extends EntityCreature implements Monster {

    protected EntityRaider(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    protected EntityRaider(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean isCelebrating() {
        return this.metadata.getIndex((byte) 15, false);
    }

    public void setCelebrating(boolean value) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

}
