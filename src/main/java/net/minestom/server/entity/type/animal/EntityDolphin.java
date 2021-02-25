package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.water.DolphinMeta} instead.
 */
@Deprecated
public class EntityDolphin extends AgeableCreature implements Animal {
    public EntityDolphin(Position spawnPosition) {
        super(EntityType.DOLPHIN, spawnPosition);
        setBoundingBox(0.9f, 0.6f, 0.9f);
    }
}
