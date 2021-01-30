package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

public class EntityWitch extends EntityCreature implements Monster {

    public EntityWitch(Position spawnPosition) {
        super(EntityType.WITCH, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }

    public boolean isDrinkingPotion() {
        return metadata.getIndex((byte) 16, false);
    }

    public void setDrinkingPotion(boolean drinkingPotion) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(drinkingPotion));
    }
}
