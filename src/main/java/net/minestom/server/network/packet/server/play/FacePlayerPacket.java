package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record FacePlayerPacket(FacePosition facePosition,
                               Point target, @Nullable EntityData entityData) implements ServerPacket.Play {

    public static final NetworkBuffer.Type<FacePlayerPacket> SERIALIZER = NetworkBufferTemplate.template(
            FacePosition.SERIALIZER, FacePlayerPacket::facePosition,
            NetworkBuffer.VECTOR3D, FacePlayerPacket::target,
            EntityData.SERIALIZER.optional(), FacePlayerPacket::entityData,
            FacePlayerPacket::new
    );

    @Deprecated(forRemoval = true)
    public FacePlayerPacket(FacePosition facePosition,
                            Point target, int entityId,
                            @Nullable FacePosition entityFacePosition) {
        this(facePosition, target, entityId > 0 && entityFacePosition != null ? new EntityData(entityId, entityFacePosition) : null);
    }

    @Deprecated(forRemoval = true)
    public int entityId() {
        if (entityData == null) return 0;
        return entityData.id;
    }

    @Deprecated(forRemoval = true)
    public @Nullable FacePosition entityFacePosition() {
        if (entityData == null) return null;
        return entityData.facePosition;
    }

    public record EntityData(int id, FacePosition facePosition) {
        public EntityData {
            Objects.requireNonNull(facePosition, "facePosition");
        }

        private static final NetworkBuffer.Type<EntityData> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.VAR_INT, EntityData::id,
                FacePosition.SERIALIZER, EntityData::facePosition,
                EntityData::new
        );
    }

    public enum FacePosition {
        FEET,
        EYES;

        private static final NetworkBuffer.Type<FacePosition> SERIALIZER = NetworkBuffer.Enum(FacePosition.class);

        public static FacePosition fromFacePoint(Player.FacePoint facePoint) {
            return switch (facePoint) {
                case FEET -> FEET;
                case EYE -> EYES;
            };
        }
    }
}
