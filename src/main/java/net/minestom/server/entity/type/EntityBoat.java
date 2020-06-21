package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityBoat extends ObjectEntity {

    private boolean leftPaddleTurning;
    private boolean rightPaddleTurning;

    public EntityBoat(Position spawnPosition) {
        super(EntityType.BOAT, spawnPosition);
        setBoundingBox(1.375f, 0.5625f, 1.375f);
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 11);
            fillMetadataIndex(packet, 12);

            // TODO all remaining metadata
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 11) {
            packet.writeByte((byte) 11);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(leftPaddleTurning);
        } else if (index == 12) {
            packet.writeByte((byte) 12);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(rightPaddleTurning);
        }
    }

    public void refreshPaddle(boolean left, boolean right) {
        this.leftPaddleTurning = left;
        this.rightPaddleTurning = right;
        sendMetadataIndex(11);
        sendMetadataIndex(12);
    }

}
