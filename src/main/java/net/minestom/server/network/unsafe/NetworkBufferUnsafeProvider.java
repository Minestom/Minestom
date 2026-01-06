package net.minestom.server.network.unsafe;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.network.NetworkBufferProvider;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.function.Consumer;

public final class NetworkBufferUnsafeProvider implements NetworkBufferProvider {
    private static final NetworkBufferFactory STATIC_FACTORY = new NetworkBufferUnsafeFactoryImpl(null, null);
    private static final NetworkBufferFactory RESIZEABLE_FACTORY = STATIC_FACTORY.autoResize(NetworkBuffer.AutoResize.DOUBLE);

    @Override
    public NetworkBufferFactory createStaticFactory() {
        return STATIC_FACTORY;
    }

    @Override
    public NetworkBufferFactory createResizeableFactory() {
        return RESIZEABLE_FACTORY;
    }

    @Override
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, Registries registries) {
        return NetworkBufferUnsafeImpl.wrap(segment.toArray(ValueLayout.JAVA_BYTE), readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex) {
        return NetworkBufferUnsafeImpl.wrap(segment.toArray(ValueLayout.JAVA_BYTE), readIndex, writeIndex, null);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, Registries registries) {
        return NetworkBufferUnsafeImpl.wrap(bytes, readIndex, writeIndex, registries);
    }

    @Override
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return NetworkBufferUnsafeImpl.wrap(bytes, readIndex, writeIndex, null);
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries) {
        final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().registry(registries);
        final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
        return buffer.extractWrittenBytes(writing);
    }

    @Override
    public byte[] makeArray(Consumer<NetworkBuffer> writing) {
        final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory();
        final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
        return buffer.extractWrittenBytes(writing);
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value, Registries registries) {
        final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().registry(registries);
        final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
        return buffer.extractWrittenBytes(type, value);
    }

    @Override
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value) {
        final NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory();
        final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
        return buffer.extractWrittenBytes(type, value);
    }

    @Override
    public <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value, @Nullable Registries registries) {
        NetworkBuffer buffer = NetworkBufferUnsafeImpl.dummy(registries);
        type.write(buffer, value);
        return buffer.writeIndex();
    }

    @Override
    public <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value) {
        return sizeOf(type, value, null);
    }
}
