package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;
import java.util.Objects;

public interface PacketRegistry<T> extends Iterable<PacketRegistry.PacketInfo<? extends T>> {
    @UnknownNullability
    T create(int packetId, NetworkBuffer reader);

    PacketInfo<T> packetInfo(Class<?> packetClass);

    default PacketInfo<T> packetInfo(T packet) {
        return packetInfo(packet.getClass());
    }

    PacketInfo<T> packetInfo(int packetId);

    ConnectionState state();

    ConnectionSide side();

    record PacketInfo<T>(Class<? extends T> packetClass, int id, NetworkBuffer.Type<T> serializer) {
        public PacketInfo {
            Objects.requireNonNull(packetClass);
            Objects.requireNonNull(serializer);
        }
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    static <T> PacketRegistry<T> registry(ConnectionState state, ConnectionSide side,
                                          Map.Entry<? extends Class<? extends T>, ? extends NetworkBuffer.Type<? extends T>>... entries) {
        return new PacketRegistryImpl<>(state, side, entries);
    }

    enum ConnectionSide {
        CLIENT,
        SERVER
    }
}
