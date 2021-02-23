package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.other.SlimeMeta} instead.
 */
@Deprecated
public class EntitySlime extends EntityCreature implements Monster {

    public EntitySlime(@NotNull Position spawnPosition) {
        this(EntityType.SLIME, spawnPosition);
    }

    EntitySlime(@NotNull EntityType type, @NotNull Position spawnPosition) {
        super(type, spawnPosition);
        setSize(1);
    }

    public int getSize() {
        return metadata.getIndex((byte) 15, 1);
    }

    public void setSize(int size) {
        final float boxSize = 0.51000005f * size;
        setBoundingBox(boxSize, boxSize, boxSize);
        this.metadata.setIndex((byte) 15, Metadata.VarInt(size));
    }
}
