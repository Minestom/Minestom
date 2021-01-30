package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

public class EntitySlime extends EntityCreature implements Monster {

    public EntitySlime(Position spawnPosition) {
        super(EntityType.SLIME, spawnPosition);
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
