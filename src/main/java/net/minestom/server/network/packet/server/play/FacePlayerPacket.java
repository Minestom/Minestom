package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record FacePlayerPacket(FacePosition facePosition,
                               Point target, int entityId, FacePosition entityFacePosition) implements ServerPacket.Play {
    public FacePlayerPacket(@NotNull NetworkBuffer reader) {
        this(FacePosition.values()[reader.read(VAR_INT)],
                new Vec(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE)),
                reader.read(BOOLEAN) ? reader.read(VAR_INT) : 0,
                reader.readableBytes() > 0 ? reader.readEnum(FacePosition.class) : null);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, facePosition.ordinal());
        writer.write(DOUBLE, target.x());
        writer.write(DOUBLE, target.y());
        writer.write(DOUBLE, target.z());
        final boolean isEntity = entityId > 0;
        writer.write(BOOLEAN, isEntity);
        if (isEntity) {
            writer.write(VAR_INT, entityId);
            writer.writeEnum(FacePosition.class, entityFacePosition);
        }
    }

    public enum FacePosition {
        FEET, EYES
    }
}
