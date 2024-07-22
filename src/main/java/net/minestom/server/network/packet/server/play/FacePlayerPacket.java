package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record FacePlayerPacket(FacePosition facePosition,
                               Point target, int entityId,
                               FacePosition entityFacePosition) implements ServerPacket.Play {

    public static final NetworkBuffer.Type<FacePlayerPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, @NotNull FacePlayerPacket value) {
            writer.write(VAR_INT, value.facePosition.ordinal());
            writer.write(DOUBLE, value.target.x());
            writer.write(DOUBLE, value.target.y());
            writer.write(DOUBLE, value.target.z());
            final boolean isEntity = value.entityId > 0;
            writer.write(BOOLEAN, isEntity);
            if (isEntity) {
                writer.write(VAR_INT, value.entityId);
                writer.writeEnum(FacePosition.class, value.entityFacePosition);
            }
        }

        @Override
        public @NotNull FacePlayerPacket read(@NotNull NetworkBuffer reader) {
            return new FacePlayerPacket(FacePosition.values()[reader.read(VAR_INT)],
                    new Vec(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE)),
                    reader.read(BOOLEAN) ? reader.read(VAR_INT) : 0,
                    reader.readableBytes() > 0 ? reader.readEnum(FacePosition.class) : null);
        }
    };

    public enum FacePosition {
        FEET, EYES
    }
}
