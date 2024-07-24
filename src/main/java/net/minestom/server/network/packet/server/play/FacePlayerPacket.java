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
        public void write(@NotNull NetworkBuffer buffer, @NotNull FacePlayerPacket value) {
            buffer.write(VAR_INT, value.facePosition.ordinal());
            buffer.write(DOUBLE, value.target.x());
            buffer.write(DOUBLE, value.target.y());
            buffer.write(DOUBLE, value.target.z());
            final boolean isEntity = value.entityId > 0;
            buffer.write(BOOLEAN, isEntity);
            if (isEntity) {
                buffer.write(VAR_INT, value.entityId);
                buffer.writeEnum(FacePosition.class, value.entityFacePosition);
            }
        }

        @Override
        public @NotNull FacePlayerPacket read(@NotNull NetworkBuffer buffer) {
            return new FacePlayerPacket(FacePosition.values()[buffer.read(VAR_INT)],
                    new Vec(buffer.read(DOUBLE), buffer.read(DOUBLE), buffer.read(DOUBLE)),
                    buffer.read(BOOLEAN) ? buffer.read(VAR_INT) : 0,
                    buffer.readableBytes() > 0 ? buffer.readEnum(FacePosition.class) : null);
        }
    };

    public enum FacePosition {
        FEET, EYES
    }
}
