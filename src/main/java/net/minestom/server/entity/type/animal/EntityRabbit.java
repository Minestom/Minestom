package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityRabbit extends EntityCreature implements Animal {

    private int type;

    public EntityRabbit(Position spawnPosition) {
        super(EntityType.RABBIT, spawnPosition);
        setBoundingBox(0.4f, 0.5f, 0.4f);
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
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(type);
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        sendMetadataIndex(16);
    }
}
