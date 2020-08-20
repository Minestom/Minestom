package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Constructable;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityIronGolem extends EntityCreature implements Constructable {

    private boolean playerCreated;

    public EntityIronGolem(Position spawnPosition) {
        super(EntityType.IRON_GOLEM, spawnPosition);
        setBoundingBox(1.4f, 2.7f, 1.4f);
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
            packet.writeByte((byte) (playerCreated ? 1 : 0));
        }
    }

    public boolean isPlayerCreated() {
        return playerCreated;
    }

    public void setPlayerCreated(boolean playerCreated) {
        this.playerCreated = playerCreated;
        sendMetadataIndex(15);
    }
}
