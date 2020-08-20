package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityBlaze extends EntityCreature implements Monster {

    public EntityBlaze(Position spawnPosition) {
        super(EntityType.BLAZE, spawnPosition);
        setBoundingBox(0.6f, 1.8f, 0.6f);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 15);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 15) {
            packet.writeByte((byte) 15);
            packet.writeByte(METADATA_BYTE);
            packet.writeByte((byte) (isOnFire() ? 1 : 0));
        }
    }
}
