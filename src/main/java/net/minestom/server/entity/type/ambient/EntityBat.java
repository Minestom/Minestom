package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityBat extends EntityCreature implements Animal {

    private boolean hanging;

    public EntityBat(Position spawnPosition) {
        super(EntityType.BAT, spawnPosition);
        setBoundingBox(0.5f, 0.9f, 0.5f);
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
