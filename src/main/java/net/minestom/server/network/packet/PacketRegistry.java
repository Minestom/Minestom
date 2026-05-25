package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;

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

    record PacketInfo<T>(Class<T> packetClass, int id, NetworkBuffer.Type<T> serializer) {
    }

    @SafeVarargs
    static <T> PacketRegistry<T> registry(ConnectionState state, ConnectionSide side,
                                          Map.Entry<? extends Class<?>, ? extends NetworkBuffer.Type<?>>... entries) {
        return new PacketRegistryImpl<>(state, side, entries);
    }

    enum ConnectionSide {
        CLIENT,
        SERVER
    }
}
