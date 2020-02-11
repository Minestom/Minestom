package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class FacePlayerPacket implements ServerPacket {

    public FacePosition facePosition;
    public double targetX, targetY, targetZ;
    public boolean isEntity;
    public int entityId;
    public FacePosition entityFacePosition;


    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(facePosition.ordinal());
        writer.writeDouble(targetX);
        writer.writeDouble(targetY);
        writer.writeDouble(targetZ);

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
        FEET, EYES;
    }

}
