package net.minestom.server.network.foreign;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

/**
 * The provider used as the implementation of {@link NetworkBuffer} for {@link MemorySegment}
 * <br>
 * This uses {@link MemorySegment} as the backing with {@link NetworkBufferSegmentAllocator} for faster malloc implementations if available.
 * <br>
 * The implementation assumes all preconditions are valid as checked in {@link NetworkBuffer}
 */
public final class NetworkBufferSegmentProvider {
    public static final NetworkBufferSegmentProvider INSTANCE = new NetworkBufferSegmentProvider();
    private static final NetworkBufferFactory STATIC_FACTORY = new NetworkBufferFactoryImpl(Arena::ofAuto, null, null);
    private static final NetworkBufferFactory RESIZEABLE_FACTORY = STATIC_FACTORY.autoResize(NetworkBuffer.AutoResize.DOUBLE);

    /**
     * Creates the static factory instance used in {@link NetworkBuffer#staticBuffer(long)}
     * <br>
     * Note: this should not have a resize strategy.
     *
     * @return the new static factory
     */
    public NetworkBufferFactory createStaticFactory() {
        return STATIC_FACTORY;
    }

    /**
     * Creates the resizable factory instance used in {@link NetworkBuffer#resizableBuffer()}
     *
     * @return the new resizable factory
     */
    public NetworkBufferFactory createResizeableFactory() {
        return RESIZEABLE_FACTORY;
    }

    /**
     * Wrap the {@link MemorySegment} into a {@link NetworkBuffer} with the registries.
     * <br>
     * Useful when you already have a memory segment.
     *
     * @param segment    the segment
     * @param readIndex  the {@link NetworkBuffer#readIndex()}
     * @param writeIndex the {@link NetworkBuffer#writeIndex()}
     * @param registries the {@link NetworkBuffer#registries()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _, _ -> new")
    @ApiStatus.Experimental
    public NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        return new NetworkBufferStaticSegmentImpl(null, segment, readIndex, writeIndex, registries);
    }

    /**
     * Wrap the byte array into a {@link NetworkBuffer} with the registries.
     * Useful when you already have a {@code byte[]}.
     *
     * @param bytes      the bytes
     * @param readIndex  the {@link NetworkBuffer#readIndex()}
     * @param writeIndex the {@link NetworkBuffer#writeIndex()}
     * @param registries the {@link NetworkBuffer#registries()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _, _ -> new")
    public NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, @Nullable Registries registries) {
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
    }

    /**
     * Creates a byte array from the consumer and with registries.
     * <br>
     * Note: only the current thread can use the buffer.
     *
     * @param writing    consumer of the {@link NetworkBuffer}
     * @param registries the registries to use in serialization
     * @return the smallest byte array to represent the contents of {@link NetworkBuffer}
     */
    @Contract("_, _ -> new")
    public byte[] makeArray(Consumer<? super NetworkBuffer> writing, @Nullable Registries registries) {
        try (Arena arena = Arena.ofConfined()) {
            NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena);
            factory = registries != null ? factory.registry(registries) : factory;
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
    }

    /**
     * Creates a byte array from the type and value registries.
     * <br>
     * Note: only the current thread can use the buffer.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     *
     * @param type       the {@link NetworkBuffer.Type} for {@link T}
     * @param value      the value
     * @param registries the registries to use in serialization
     * @param <T>        the type
     * @return the smallest byte array to represent {@link T}
     */
    @Contract("_ ,_, _ -> new")
    public <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value, @Nullable Registries registries) {
        try (Arena arena = Arena.ofConfined()) {
            NetworkBufferFactory factory = NetworkBufferFactory.resizeableFactory().arena(arena);
            factory = registries != null ? factory.registry(registries) : factory;
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
    }

    /**
     * Get the byte size of the serialized {@link T}, this should be deterministic.
     * <br>
     * The written length known as {@link NetworkBuffer#writeIndex()} minus the inital writeIndex, should equal the size below.
     *
     * @param type       the type
     * @param value      the value
     * @param registries the registries used
     * @param <T>        the type
     * @return the number of bytes that {@link T} occupies.
     */
    public <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value, @Nullable Registries registries) {
        NetworkBuffer buffer = NetworkBufferSegmentImpl.dummy(registries);
        type.write(buffer, value);
        return buffer.writeIndex();
    }

    /**
     * Get the underlying {@link MemorySegment} of the {@link NetworkBufferSegmentImpl}.
     * <br>
     * You avoid using this method and instead wrap a segment instead.
     *
     * @param buffer the buffer
     * @return the memory segment
     * @throws IllegalStateException if the buffer is not a supported implementation
     */
    @ApiStatus.Experimental
    public static MemorySegment segment(NetworkBuffer buffer) {
        if (!(buffer instanceof NetworkBufferSegmentImpl bufferImpl))
            throw new IllegalArgumentException("Unsupported NetworkBuffer implementation: " + buffer.getClass());
        return bufferImpl.segment();
    }

    private NetworkBufferSegmentProvider() {}
}
