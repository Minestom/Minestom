package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityPolarBear extends EntityCreature implements Animal {

    private boolean standingUp;

    public EntityPolarBear(Position spawnPosition) {
        super(EntityType.POLAR_BEAR, spawnPosition);
        setBoundingBox(1.3f, 1.4f, 1.3f);
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
            packet.writeBoolean(standingUp);
        }
    }

    public boolean isStandingUp() {
        return standingUp;
    }

    public void setStandingUp(boolean standingUp) {
        this.standingUp = standingUp;
        sendMetadataIndex(16);
    }
}
