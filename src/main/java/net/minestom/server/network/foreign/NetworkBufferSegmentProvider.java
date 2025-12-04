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
import java.util.function.Consumer;

/**
 * The provider used to interface with {@link net.minestom.server.ServerProcess#networkBufferProvider()}.
 * <br>
 * This uses {@link MemorySegment} as the backing with {@link NetworkBufferSegmentAllocator} for faster malloc implementations if available.
 * <br>
 * The implementation assumes all preconditions are valid as checked in {@link NetworkBuffer}
 */
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
        return new NetworkBufferStaticSegmentImpl(null, segment, readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex) {
        return new NetworkBufferStaticSegmentImpl(null, segment, readIndex, writeIndex, null);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, Registries registries) {
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex);
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries) {
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing) {
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value, Registries registries) {
        try (Arena arena = Arena.ofConfined()) {
            final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value) {
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
    public <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value) {
        return sizeOf(type, value, null);
    }
}
