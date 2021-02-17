package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.Position;

public class EntityIronGolem extends EntityCreature {

    public EntityIronGolem(Position spawnPosition) {
        super(EntityType.IRON_GOLEM, spawnPosition);
        setBoundingBox(1.4f, 2.7f, 1.4f);
    }

    public boolean isPlayerCreated() {
        return metadata.getIndex((byte) 15, 0) == 0x01;
    }

    public void setPlayerCreated(boolean playerCreated) {
        this.metadata.setIndex((byte) 15, Metadata.Byte((byte) (playerCreated ? 0x01 : 0x00)));
    }
}
