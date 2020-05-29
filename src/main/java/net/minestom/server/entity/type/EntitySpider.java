package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntitySpider extends EntityCreature {

    private boolean climbing;

    public EntitySpider(Position spawnPosition) {
        super(EntityType.SPIDER, spawnPosition);
        setBoundingBox(1.4f, 0.9f, 1.4f);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 15);
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 15) {
            packet.writeByte((byte) 15);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(climbing);
        }
    }

    public boolean isClimbing() {
        return climbing;
    }

    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
        sendMetadataIndex(15);
    }
}
