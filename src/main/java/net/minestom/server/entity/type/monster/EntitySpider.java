package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntitySpider extends EntityCreature implements Monster {

    private boolean climbing;

    public EntitySpider(Position spawnPosition) {
        super(EntityType.SPIDER, spawnPosition);
        setBoundingBox(1.4f, 0.9f, 1.4f);
        setEyeHeight(0.65f);
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
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(climbing);
        }
    }

    /**
     * Get if the spider is climbing
     *
     * @return true if the spider is climbing, false otherwise
     */
    public boolean isClimbing() {
        return climbing;
    }

    /**
     * Make the spider climbs
     *
     * @param climbing true to make the spider climbs, false otherwise
     */
    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
        sendMetadataIndex(15);
    }
}
