package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.animal.PigMeta} instead.
 */
@Deprecated
public class EntityPig extends AgeableCreature implements Animal {

    public EntityPig(Position spawnPosition) {
        super(EntityType.PIG, spawnPosition);
        setBoundingBox(0.9f, 0.9f, 0.9f);
    }

    /**
     * Gets if the pig has a saddle.
     *
     * @return true if the pig has a saddle, false otherwise
     */
    public boolean hasSaddle() {
        return metadata.getIndex((byte) 16, false);
    }

    /**
     * Sets a saddle to the pig.
     *
     * @param saddle true to add a saddle, false to remove it
     */
    public void setSaddle(boolean saddle) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(saddle));
    }
}
