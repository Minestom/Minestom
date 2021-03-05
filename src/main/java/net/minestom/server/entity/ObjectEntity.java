package net.minestom.server.entity;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.EntityMeta} that inherits
 * {@link net.minestom.server.entity.metadata.ObjectDataProvider} instead.
 */
@Deprecated
public abstract class ObjectEntity extends Entity {

    public ObjectEntity(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setGravity(0.02f, 0.04f, 1.96f);
    }

    /**
     * Gets the data of this object entity.
     *
     * @return an object data
     * @see <a href="https://wiki.vg/Object_Data">here</a>
     */
    public abstract int getObjectData();

    @Override
    public void update(long time) {

    }

    @Override
    public void spawn() {

    }

}
