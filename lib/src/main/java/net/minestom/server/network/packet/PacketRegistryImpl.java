package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

final class PacketRegistryImpl<T> implements PacketRegistry<T> {
    private final ConnectionState state;
    private final ConnectionSide side;
    private final List<PacketInfo<? extends T>> packets;
    private final Map<Class<?>, PacketInfo<? extends T>> packetIds;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    PacketRegistryImpl(ConnectionState state, ConnectionSide side,
                       Map.Entry<? extends Class<? extends T>, ? extends NetworkBuffer.Type<? extends T>>... entries) {
        this.state = state;
        this.side = side;
        final PacketInfo<? extends T>[] packetInfos = new PacketInfo[entries.length];
        final Map.Entry<Class<?>, PacketInfo<? extends T>>[] packetIdEntries = new Map.Entry[entries.length];
        for (int i = 0; i < entries.length; i++) {
            final Map.Entry<? extends Class<? extends T>, ? extends NetworkBuffer.Type<? extends T>> entry = entries[i];
            Objects.requireNonNull(entry);
            final PacketInfo<? extends T> packetInfo = new PacketInfo<>(entry.getKey(), i, (NetworkBuffer.Type<T>) entry.getValue());
            packetInfos[i] = packetInfo;
            packetIdEntries[i] = entry(packetInfo.packetClass(), packetInfo);
        }
        this.packets = List.of(packetInfos);
        this.packetIds = Map.ofEntries(packetIdEntries);
    }

    @Override
    public ConnectionState state() {
        return state;
    }

    @Override
    public ConnectionSide side() {
        return side;
    }

    @Override
    public T create(int packetId, NetworkBuffer reader) {
        final PacketInfo<T> info = packetInfo(packetId);
        final NetworkBuffer.Type<@UnknownNullability T> serializer = info.serializer();
        final T packet = serializer.read(reader);
        if (packet == null) {
            throw new IllegalStateException("Packet " + info.packetClass() + " failed to read!");
        }
        return packet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketInfo<T> packetInfo(Class<?> packetClass) {
        final PacketInfo<? extends T> packetInfo = packetIds.get(packetClass);
        if (packetInfo == null) {
            throw new IllegalStateException("Packet type " + packetClass + " cannot be sent in state " + side.name() + "_" + state.name() + "!");
        }
        return (PacketInfo<T>) packetInfo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketInfo<T> packetInfo(int packetId) {
        if (packetId < 0 || packetId >= packets.size())
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(packetId) + " isn't registered!");
        return (PacketInfo<T>) packets.get(packetId);
    }

    @Override
    public Iterator<PacketInfo<? extends T>> iterator() {
        return packets.iterator();
    }
}
