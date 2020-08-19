package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityZombie extends EntityCreature implements Monster {

    private boolean baby;
    private boolean becomingDrowned;

    public EntityZombie(Position spawnPosition) {
        super(EntityType.ZOMBIE, spawnPosition);
        setBoundingBox(0.6f, 1.95f, 0.6f);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 15);
            fillMetadataIndex(packet, 17);
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
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

    @Override
    public float getEyeHeight() {
        return isBaby() ? 0.93f : 1.74f;
    }
}
