package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class FacePlayerPacket implements ServerPacket {

    public FacePosition facePosition;
    public double targetX, targetY, targetZ;
    public int entityId;
    public FacePosition entityFacePosition;

    public FacePlayerPacket() {
        facePosition = FacePosition.EYES;
        entityFacePosition = FacePosition.EYES;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
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
    public void read(@NotNull BinaryReader reader) {
        facePosition = FacePosition.values()[reader.readVarInt()];
        targetX = reader.readDouble();
        targetY = reader.readDouble();
        targetZ = reader.readDouble();

        boolean isEntity = reader.readBoolean();
        if(isEntity) {
            entityId = reader.readVarInt();
            entityFacePosition = FacePosition.values()[reader.readVarInt()];
        } else {
            entityId = 0;
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
