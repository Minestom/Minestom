package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AgeableCreature extends EntityCreature implements Ageable {

    protected AgeableCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    protected AgeableCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    @Override
    public boolean isBaby() {
        return this.metadata.getIndex((byte) 15, false);
    }

    @Override
    public void setBaby(boolean value) {
        this.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

    @Override
    public double getEyeHeight() {
        return isBaby() ? super.getEyeHeight() / 2 : super.getEyeHeight();
    }
}
