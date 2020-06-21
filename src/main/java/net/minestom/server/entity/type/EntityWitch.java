package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityWitch extends EntityCreature {

    private boolean drinkingPotion;

    public EntityWitch(Position spawnPosition) {
        super(EntityType.WITCH, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 16);
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
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
