package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;

public class EntityRabbit extends EntityCreature implements Animal {

    public EntityRabbit(Position spawnPosition) {
        super(EntityType.RABBIT, spawnPosition);
        setBoundingBox(0.4f, 0.5f, 0.4f);
    }

    public int getType() {
        return metadata.getIndex((byte) 16, 0);
    }

    public void setType(int type) {
        this.metadata.setIndex((byte) 16, Metadata.VarInt(type));
    }
}
