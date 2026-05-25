package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class PacketRegistryImpl<T> implements PacketRegistry<T> {
    private final ConnectionState state;
    private final ConnectionSide side;
    private final PacketInfo<? extends T>[] suppliers;
    private final ClassValue<PacketInfo<T>> packetIds;

    @SafeVarargs
    @SuppressWarnings({"unchecked", "rawtypes"})
    PacketRegistryImpl(ConnectionState state, ConnectionSide side,
                       Map.Entry<? extends Class<?>, ? extends NetworkBuffer.Type<?>>... entries) {
        this.state = state;
        this.side = side;
        final String errorSuffix = side.name() + "_" + state.name();
        final PacketInfo<? extends T>[] packetInfos = new PacketInfo[entries.length];
        for (int i = 0; i < entries.length; i++) {
            final Map.Entry<? extends Class<?>, ? extends NetworkBuffer.Type<?>> entry = entries[i];
            Objects.requireNonNull(entry);
            packetInfos[i] = new PacketInfo(entry.getKey(), i, entry.getValue());
        }
        this.suppliers = packetInfos;
        this.packetIds = new ClassValue<>() {
            @Override
            protected PacketInfo<T> computeValue(Class<?> type) {
                for (PacketInfo<? extends T> info : suppliers) {
                    if (info.packetClass() == type) return (PacketInfo<T>) info;
                }
                throw new IllegalStateException("Packet type " + type + " cannot be sent in state " + errorSuffix + "!");
            }
        };
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
    public @UnknownNullability T create(int packetId, NetworkBuffer reader) {
        final PacketInfo<T> info = packetInfo(packetId);
        final NetworkBuffer.Type<T> serializer = info.serializer();
        final T packet = serializer.read(reader);
        if (packet == null) {
            throw new IllegalStateException("Packet " + info.packetClass() + " failed to read!");
        }
        return packet;
    }

    @Override
    public PacketInfo<T> packetInfo(Class<?> packetClass) {
        return packetIds.get(packetClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketInfo<T> packetInfo(int packetId) {
        if (packetId < 0 || packetId >= suppliers.length)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(packetId) + " isn't registered!");
        return (PacketInfo<T>) suppliers[packetId];
    }

    @Override
    public Iterator<PacketInfo<T>> iterator() {
        //noinspection unchecked
        return (Iterator<PacketInfo<T>>) List.of(suppliers);
    }
}
