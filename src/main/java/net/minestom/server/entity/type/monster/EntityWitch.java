package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityWitch extends EntityCreature implements Monster {

    private boolean drinkingPotion;

    public EntityWitch(Position spawnPosition) {
        super(EntityType.WITCH, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
        setEyeHeight(1.62f);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 16);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 16) {
            packet.writeByte((byte) 16);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(drinkingPotion);
        }
    }

    public boolean isDrinkingPotion() {
        return drinkingPotion;
    }

    public void setDrinkingPotion(boolean drinkingPotion) {
        this.drinkingPotion = drinkingPotion;
        sendMetadataIndex(16);
    }
}
