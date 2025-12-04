package net.minestom.server.network.foreign;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.network.NetworkBufferProvider;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

public final class NetworkBufferSegmentProvider implements NetworkBufferProvider {

    @Override
    public NetworkBufferFactory createStaticFactory() {
        return new NetworkBufferFactoryImpl(Arena::ofAuto, null, null);
    }

    @Override
    public NetworkBufferFactory createResizeableFactory() {
        return createStaticFactory().autoResize(NetworkBuffer.AutoResize.DOUBLE);
    }

    @Override
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, Registries registries) {
        Objects.requireNonNull(segment, "segment");
        Objects.requireNonNull(registries, "registries");
        return NetworkBufferSegmentImpl.wrap(segment, readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex) {
        Objects.requireNonNull(segment, "segment");
        return NetworkBufferSegmentImpl.wrap(segment, readIndex, writeIndex, null);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, Registries registries) {
        Objects.requireNonNull(bytes, "bytes");
        Objects.requireNonNull(registries, "registries");
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        Objects.requireNonNull(bytes, "bytes");
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex);
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries) {
        Objects.requireNonNull(writing, "writing");
        Objects.requireNonNull(registries, "registries");
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing) {
        Objects.requireNonNull(writing, "writing");
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value, Registries registries) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(registries, "registries");
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value) {
        Objects.requireNonNull(type, "type");
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
    }

    @Override
    public <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value, @Nullable Registries registries) {
        NetworkBuffer buffer = NetworkBufferSegmentImpl.dummy(registries);
        type.write(buffer, value);
        return buffer.writeIndex();
    }

    @Override
    public <T> long sizeOf(NetworkBuffer.Type<T> type, T value) {
        return sizeOf(type, value, null);
    }
}
