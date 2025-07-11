package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record TrackedWaypointPacket(
        @NotNull Operation operation,
        @NotNull Waypoint waypoint
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<TrackedWaypointPacket> SERIALIZER = NetworkBufferTemplate.template(
            Operation.NETWORK_TYPE, TrackedWaypointPacket::operation,
            Waypoint.NETWORK_TYPE, TrackedWaypointPacket::waypoint,
            TrackedWaypointPacket::new);

    public enum Operation {
        TRACK,
        UNTRACK,
        UPDATE;

        public static final NetworkBuffer.Type<Operation> NETWORK_TYPE = NetworkBuffer.Enum(Operation.class);
    }

    public record Waypoint(
            @NotNull Either<UUID, String> id,
            @NotNull Icon icon,
            @NotNull Target target
    ) {
        public static final NetworkBuffer.Type<Waypoint> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.Either(NetworkBuffer.UUID, NetworkBuffer.STRING), Waypoint::id,
                Icon.NETWORK_TYPE, Waypoint::icon,
                Target.NETWORK_TYPE, Waypoint::target,
                Waypoint::new);
    }

    public record Icon(
            @NotNull Key style,
            @Nullable RGBLike color
    ) {
        public static final Key DEFAULT_STYLE = Key.key("default");
        public static final Icon DEFAULT = new Icon(DEFAULT_STYLE, null);

        public static final NetworkBuffer.Type<Icon> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.KEY, Icon::style,
                Color.RGB_BYTE_NETWORK_TYPE.optional(),
                Icon::color,
                Icon::new);

        public Icon(@NotNull Key style) {
            this(style, null);
        }
    }

    public sealed interface Target {
        @NotNull NetworkBuffer.Type<Target> NETWORK_TYPE = Type.NETWORK_TYPE
                .unionType(Target::dataSerializer, Target::targetToType);

        record Empty() implements Target {
            public static final NetworkBuffer.Type<Empty> NETWORK_TYPE = NetworkBufferTemplate.template(Empty::new);
        }

        record Vec3i(@NotNull Point point) implements Target {
            public static final NetworkBuffer.Type<Vec3i> NETWORK_TYPE = NetworkBufferTemplate.template(
                    NetworkBuffer.VECTOR3I, Vec3i::point,
                    Vec3i::new);
        }

        record Chunk(int chunkX, int chunkZ) implements Target {
            public static final NetworkBuffer.Type<Chunk> NETWORK_TYPE = NetworkBufferTemplate.template(
                    NetworkBuffer.VAR_INT, Chunk::chunkX,
                    NetworkBuffer.VAR_INT, Chunk::chunkZ,
                    Chunk::new);
        }

        record Azimuth(float angle) implements Target {
            public static final NetworkBuffer.Type<Azimuth> NETWORK_TYPE = NetworkBufferTemplate.template(
                    NetworkBuffer.FLOAT, Azimuth::angle,
                    Azimuth::new);
        }

        enum Type {
            EMPTY, VEC3I, CHUNK, AZIMUTH;

            public static final NetworkBuffer.Type<Type> NETWORK_TYPE = NetworkBuffer.Enum(Type.class);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static NetworkBuffer.Type<Target> dataSerializer(@NotNull Type type) {
            return (NetworkBuffer.Type) switch (type) {
                case EMPTY -> Empty.NETWORK_TYPE;
                case VEC3I -> Vec3i.NETWORK_TYPE;
                case CHUNK -> Chunk.NETWORK_TYPE;
                case AZIMUTH -> Azimuth.NETWORK_TYPE;
            };
        }

        private static @NotNull Type targetToType(@NotNull Target target) {
            return switch (target) {
                case Empty ignored -> Type.EMPTY;
                case Vec3i ignored -> Type.VEC3I;
                case Chunk ignored -> Type.CHUNK;
                case Azimuth ignored -> Type.AZIMUTH;
            };
        }
    }
}