package net.minestom.server.network;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.ServerProcess;
import net.minestom.server.network.foreign.NetworkBufferSegmentProvider;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a provider for factories and static methods found in {@link NetworkBuffer}.
 * <br>
 * This can be used to notify Minestom of your own NetworkBuffer implementation to be used.
 * Note: Implementations should be able to assume invariants like nullability is already conformed.
 */
@ApiStatus.Experimental
public interface NetworkBufferProvider {

    /**
     * Creates the static factory instance used in {@link NetworkBuffer#staticBuffer(long)}
     * <br>
     * Note: this should not have a resize strategy.
     *
     * @return the new static factory
     */
    NetworkBufferFactory createStaticFactory();

    /**
     * Creates the resizable factory instance used in {@link NetworkBuffer#resizableBuffer()}
     *
     * @return the new resizable factory
     */
    NetworkBufferFactory createResizeableFactory();

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
    NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, Registries registries);

    /**
     * Wrap the {@link MemorySegment} into a {@link NetworkBuffer} without registries.
     * Useful when you already have a memory segment.
     *
     * @param segment    the segment
     * @param readIndex  the {@link NetworkBuffer#readIndex()}
     * @param writeIndex the {@link NetworkBuffer#writeIndex()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _ -> new")
    @ApiStatus.Experimental
    NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex);

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
    NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, Registries registries);

    /**
     * Wrap the byte array into a {@link NetworkBuffer}.
     * Useful when you already have a {@code byte[]}.
     *
     * @param bytes      the bytes
     * @param readIndex  the {@link NetworkBuffer#readIndex()}
     * @param writeIndex the {@link NetworkBuffer#writeIndex()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _ -> new")
    NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex);

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
    byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries);

    /**
     * Creates a byte array from the consumer and without registries.
     * <br>
     * Note: only the current thread can use the buffer.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     *
     * @param writing consumer of the {@link NetworkBuffer}
     * @return the smallest byte array to represent the contents of {@link NetworkBuffer}
     */
    @Contract("_ -> new")
    byte[] makeArray(Consumer<NetworkBuffer> writing);

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
    <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value, Registries registries);

    /**
     * Creates a byte array from the type and value without registries.
     * <br>
     * Note: only the current thread can use the buffer.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     *
     * @param type  the {@link NetworkBuffer.Type} for {@link T}
     * @param value the value
     * @param <T>   the type
     * @return the smallest byte array to represent {@link T}
     */
    @Contract("_, _ -> new")
    <T extends @UnknownNullability Object> byte[] makeArray(NetworkBuffer.Type<T> type, T value);

    /**
     * Get the byte size of the serialized {@link T}, this should be deterministic.
     *
     * @param type       the type
     * @param value      the value
     * @param registries the registries used
     * @param <T>        the type
     * @return the number of bytes that {@link T} occupies.
     */
    <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value, Registries registries);

    /**
     * Get the byte size of the serialized {@link T}, this should be deterministic.
     *
     * @param type  the type
     * @param value the value
     * @param <T>   the type
     * @return the number of bytes that {@link T} occupies.
     */
    <T extends @UnknownNullability Object> long sizeOf(NetworkBuffer.Type<T> type, T value);

    /**
     * Gets the current provider instance
     * <br>
     * Note: currently only one instance per lifetime due to pooling not being under the server process.
     * @return the current {@link NetworkBufferProvider}.
     */
    static NetworkBufferProvider get() {
        final class Holder {
            static final NetworkBufferProvider INSTANCE = determineProvider();
        }
        return Holder.INSTANCE;
    }

    private static NetworkBufferProvider determineProvider() {
        ServerProcess serverProcess = MinecraftServer.process();
        if (serverProcess == null && ServerFlag.INSIDE_TEST) return new NetworkBufferSegmentProvider(); // Temporary for tests
        return Objects.requireNonNull(serverProcess, "Network buffers cannot be created without a server.").networkBufferProvider();
    }
}
