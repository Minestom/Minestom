package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityBat extends EntityCreature implements Animal {

    public EntityBat(Position spawnPosition) {
        super(EntityType.BAT, spawnPosition);
        setBoundingBox(0.5f, 0.9f, 0.5f);
    }

    /**
     * Gets if the bat is hanging.
     *
     * @return true if the bat is hanging, false otherwise
     */
    public boolean isHanging() {
        return metadata.getIndex((byte) 15, 0) == 1;
    }

    /**
     * Makes the bat hanging or cancel.
     *
     * @param hanging true to make the bat hanging, false otherwise
     */
    public void setHanging(boolean hanging) {
        this.metadata.setIndex((byte) 15, Metadata.Byte((byte) (hanging ? 1 : 0)));
    }
}
