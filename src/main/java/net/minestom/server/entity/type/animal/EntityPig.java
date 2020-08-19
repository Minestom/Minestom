package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityPig extends EntityCreature implements Animal {

    private boolean saddle;

    public EntityPig(Position spawnPosition) {
        super(EntityType.PIG, spawnPosition);
        setBoundingBox(0.9f, 0.9f, 0.9f);
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
            packet.writeBoolean(saddle);
        }
    }

    /**
     * Get if the pig has a saddle
     *
     * @return true if the pig has a saddle, false otherwise
     */
    public boolean hasSaddle() {
        return saddle;
    }

    /**
     * Set a saddle to the pig
     *
     * @param saddle true to add a saddle, false to remove it
     */
    public void setSaddle(boolean saddle) {
        this.saddle = saddle;
        sendMetadataIndex(16);
    }
}
