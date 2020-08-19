package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Constructable;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntitySnowman extends EntityCreature implements Constructable {

    private boolean pumpkinHat;

    public EntitySnowman(Position spawnPosition) {
        super(EntityType.SNOW_GOLEM, spawnPosition);
        setBoundingBox(0.7f, 1.9f, 0.7f);
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
            packet.writeByte((byte) (pumpkinHat ? 0x10 : 0x00));
        }
    }

    public boolean hasPumpkinHat() {
        return pumpkinHat;
    }

    public void setPumpkinHat(boolean pumpkinHat) {
        this.pumpkinHat = pumpkinHat;
        sendMetadataIndex(15);
    }
}
