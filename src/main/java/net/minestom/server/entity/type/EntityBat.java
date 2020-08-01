package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityBat extends EntityCreature {

    private boolean hanging;

    public EntityBat(Position spawnPosition) {
        super(EntityType.BAT, spawnPosition);
        setBoundingBox(0.5f, 0.9f, 0.5f);
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
            packet.writeByte(METADATA_BYTE);
            packet.writeByte((byte) (hanging ? 1 : 0));
        }
    }

    /**
     * Get if the bat is hanging
     *
     * @return true if the bat is hanging, false otherwise
     */
    public boolean isHanging() {
        return hanging;
    }

    /**
     * Make the bat hanging or cancel
     *
     * @param hanging true to make the bat hanging, false otherwise
     */
    public void setHanging(boolean hanging) {
        this.hanging = hanging;
    }
}
