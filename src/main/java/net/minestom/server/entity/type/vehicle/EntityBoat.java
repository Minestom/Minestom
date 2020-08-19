package net.minestom.server.entity.type.vehicle;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Vehicle;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;

import java.util.function.Consumer;

public class EntityBoat extends ObjectEntity implements Vehicle {

    private BoatType boatType;
    private boolean leftPaddleTurning;
    private boolean rightPaddleTurning;

    public EntityBoat(Position spawnPosition) {
        super(EntityType.BOAT, spawnPosition);
        setBoundingBox(1.375f, 0.5625f, 1.375f);
        this.boatType = BoatType.OAK;
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 10);
            fillMetadataIndex(packet, 11);
            fillMetadataIndex(packet, 12);

            // TODO all remaining metadata
        };
    }

    @Override
    protected void fillMetadataIndex(BinaryWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 10) {
            packet.writeByte((byte) 10);
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(boatType.ordinal());
        } else if (index == 11) {
            packet.writeByte((byte) 11);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(leftPaddleTurning);
        } else if (index == 12) {
            packet.writeByte((byte) 12);
            packet.writeByte(METADATA_BOOLEAN);
            packet.writeBoolean(rightPaddleTurning);
        }
    }

    /**
     * Get the boat type
     *
     * @return the boat type
     */
    public BoatType getBoatType() {
        return boatType;
    }

    /**
     * Change the boat type
     *
     * @param boatType the new boat type
     */
    public void setBoatType(BoatType boatType) {
        Check.notNull(boatType, "The boat type cannot be null");
        this.boatType = boatType;
        sendMetadataIndex(10);
    }

    public void refreshPaddle(boolean left, boolean right) {
        this.leftPaddleTurning = left;
        this.rightPaddleTurning = right;
        sendMetadataIndex(11);
        sendMetadataIndex(12);
    }

    public enum BoatType {
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK
    }


}
