package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.function.Consumer;

public class EntityMooshroom extends EntityCreature {

    private MooshroomType mooshroomType;

    public EntityMooshroom(Position spawnPosition) {
        super(EntityType.MOOSHROOM, spawnPosition);
        setBoundingBox(0.9f, 1.4f, 0.9f);
        setMooshroomType(MooshroomType.RED);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 16);
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 16) {
            packet.writeByte((byte) 16);
            packet.writeByte(METADATA_STRING);
            packet.writeSizedString(mooshroomType.getIdentifier());
        }
    }

    public MooshroomType getMooshroomType() {
        return mooshroomType;
    }

    public void setMooshroomType(MooshroomType mooshroomType) {
        this.mooshroomType = mooshroomType;
        sendMetadataIndex(16);
    }

    public enum MooshroomType {
        RED("red"),
        BROWN("brown");

        private String identifier;

        MooshroomType(String identifier) {
            this.identifier = identifier;
        }

        private String getIdentifier() {
            return identifier;
        }
    }
}
