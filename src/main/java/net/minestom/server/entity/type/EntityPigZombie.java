package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityPigZombie extends EntityCreature {

    private boolean baby;
    private boolean becomingDrowned;

    public EntityPigZombie(Position spawnPosition) {
        super(EntityType.ZOMBIE_PIGMAN, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 15);
            fillMetadataIndex(packet, 17);
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 15) {
            packet.writeByte((byte) 15);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(baby);
        } else if (index == 17) {
            packet.writeByte((byte) 17);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(becomingDrowned);
        }
    }

    public boolean isBaby() {
        return baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
        sendMetadataIndex(15);
    }

    public boolean isBecomingDrowned() {
        return becomingDrowned;
    }

    public void setBecomingDrowned(boolean becomingDrowned) {
        this.becomingDrowned = becomingDrowned;
        sendMetadataIndex(17);
    }

}
