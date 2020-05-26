package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityPig extends EntityCreature {

    private boolean saddle;

    public EntityPig(Position spawnPosition) {
        super(EntityType.PIG, spawnPosition);
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
            packet.writeBoolean(saddle);
        }
    }

    public boolean hasSaddle() {
        return saddle;
    }

    public void setSaddle(boolean saddle) {
        this.saddle = saddle;
        sendMetadataIndex(16);
    }
}
