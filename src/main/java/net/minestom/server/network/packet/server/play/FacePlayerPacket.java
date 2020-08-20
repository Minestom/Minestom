package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class FacePlayerPacket implements ServerPacket {

    public FacePosition facePosition;
    public double targetX, targetY, targetZ;
    public int entityId;
    public FacePosition entityFacePosition;


    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(facePosition.ordinal());
        writer.writeDouble(targetX);
        writer.writeDouble(targetY);
        writer.writeDouble(targetZ);

        final boolean isEntity = entityId > 0;
        writer.writeBoolean(isEntity);
        if (isEntity) {
            writer.writeVarInt(entityId);
            writer.writeVarInt(entityFacePosition.ordinal());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.FACE_PLAYER;
    }

    public enum FacePosition {
        FEET, EYES
    }

}
