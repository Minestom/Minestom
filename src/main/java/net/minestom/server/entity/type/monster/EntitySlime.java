package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntitySlime extends EntityCreature implements Monster {

    private int size;

    public EntitySlime(Position spawnPosition) {
        super(EntityType.SLIME, spawnPosition);
        setSize(1);
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
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(size);
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        final float boxSize = 0.51000005f * size;
        setBoundingBox(boxSize, boxSize, boxSize);
        sendMetadataIndex(15);
    }
}
