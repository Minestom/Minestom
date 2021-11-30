package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record FacePlayerPacket(FacePosition facePosition,
                               Point target, int entityId, FacePosition entityFacePosition) implements ServerPacket {
    public FacePlayerPacket(BinaryReader reader) {
        this(FacePosition.values()[reader.readVarInt()],
                new Vec(reader.readDouble(), reader.readDouble(), reader.readDouble()),
                reader.readBoolean() ? reader.readVarInt() : 0,
                reader.available() > 0 ? FacePosition.values()[reader.readVarInt()] : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(facePosition.ordinal());
        writer.writeDouble(target.x());
        writer.writeDouble(target.y());
        writer.writeDouble(target.z());
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
