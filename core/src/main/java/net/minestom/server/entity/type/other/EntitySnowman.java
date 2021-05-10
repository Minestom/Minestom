package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Constructable;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.golem.SnowGolemMeta} instead.
 */
@Deprecated
public class EntitySnowman extends EntityCreature implements Constructable {

    public EntitySnowman(Position spawnPosition) {
        super(EntityType.SNOW_GOLEM, spawnPosition);
        setBoundingBox(0.7f, 1.9f, 0.7f);
    }

    public boolean hasPumpkinHat() {
        return metadata.getIndex((byte) 15, (byte) 0x00) == 0x10;
    }

    public void setPumpkinHat(boolean pumpkinHat) {
        this.metadata.setIndex((byte) 15, Metadata.Byte((byte) (pumpkinHat ? 0x10 : 0x00)));
    }
}
