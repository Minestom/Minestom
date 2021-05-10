package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.animal.PolarBearMeta} instead.
 */
@Deprecated
public class EntityPolarBear extends AgeableCreature implements Animal {

    public EntityPolarBear(Position spawnPosition) {
        super(EntityType.POLAR_BEAR, spawnPosition);
        setBoundingBox(1.3f, 1.4f, 1.3f);
    }

    public boolean isStandingUp() {
        return metadata.getIndex((byte) 16, false);
    }

    public void setStandingUp(boolean standingUp) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(standingUp));
    }
}
