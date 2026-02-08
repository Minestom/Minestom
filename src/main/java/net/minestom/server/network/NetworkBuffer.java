package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.network.foreign.NetworkBufferSegmentAllocator;
import net.minestom.server.network.foreign.NetworkBufferSegmentProvider;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.Functions;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.crypto.KeyUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.*;

import javax.crypto.Cipher;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.zip.DataFormatException;

/**
 * A mutable byte buffer for reading and writing network protocol data with type-safe operations.
 * <p>
 * Buffers maintain separate read and write indices for bidirectional operations.
 * They come in two flavors:
 * <ul>
 *   <li><b>Static buffers</b> - Fixed capacity, created via {@link #staticBuffer(long)}</li>
 *   <li><b>Resizable buffers</b> - Resizeable capacity, created via {@link #resizableBuffer()}</li>
 * </ul>
 *
 * <b>Basic Usage:</b>
 * <pre>{@code
 * NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
 * buffer.write(NetworkBuffer.INT, 42);
 * buffer.write(NetworkBuffer.STRING, "Hello");
 *
 * int value = buffer.read(NetworkBuffer.INT);
 * String text = buffer.read(NetworkBuffer.STRING);
 * }</pre>
 *
 * <b>Custom Types with Templates:</b>
 * <pre>{@code
 *     record MyData(int id, String name) {
 *         static final NetworkBuffer.Type<MyData> SERIALIZER = NetworkBufferTemplate.template(
 *          NetworkBuffer.INT, MyData::id,
 *          NetworkBuffer.STRING, MyData::name,
 *          MyData::new
 *         );
 *     }
 *     ...
 *     MyData data = new MyData(1, "Test");
 *     byte[] bytes = NetworkBuffer.makeArray(MyData.SERIALIZER, data);
 *     System.out.println("Bytes: " + Arrays.toString(bytes));
 *     NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
 *     MyData value = buffer.read(MyData.SERIALIZER);
 *     System.out.println("Value: " + value); // Value: MyData[id=1, name="Test"]
 * }</pre>
 * <br>
 *
 * <b>Note:</b> These are not thread safe, because of their index tracking,
 * also buffers will attempt to use native allocation through {@link NetworkBufferSegmentAllocator} if available.
 *
 * @see Type for custom types
 * @see NetworkBufferTemplate for templating
 * @see NetworkBufferFactory to create custom allocators
 * @see IOView to interface with existing code
 */
public interface NetworkBuffer {
    Type<Unit> UNIT = NetworkBufferTemplate.template(Unit.INSTANCE);
    Type<Boolean> BOOLEAN = new NetworkBufferTypeImpl.BooleanType();
    Type<Byte> BYTE = new NetworkBufferTypeImpl.ByteType();
    Type<Short> UNSIGNED_BYTE = new NetworkBufferTypeImpl.UnsignedByteType();
    Type<Short> SHORT = new NetworkBufferTypeImpl.ShortType();
    Type<Integer> UNSIGNED_SHORT = new NetworkBufferTypeImpl.UnsignedShortType();
    Type<Integer> INT = new NetworkBufferTypeImpl.IntType();
    Type<Long> UNSIGNED_INT = new NetworkBufferTypeImpl.UnsignedIntType();
    Type<Long> LONG = new NetworkBufferTypeImpl.LongType();
    Type<Float> FLOAT = new NetworkBufferTypeImpl.FloatType();
    Type<Double> DOUBLE = new NetworkBufferTypeImpl.DoubleType();
    Type<Integer> VAR_INT = new NetworkBufferTypeImpl.VarIntType();
    Type<@Nullable Integer> OPTIONAL_VAR_INT = new NetworkBufferTypeImpl.OptionalVarIntType();
    Type<Integer> VAR_INT_3 = new NetworkBufferTypeImpl.VarInt3Type();
    Type<Long> VAR_LONG = new NetworkBufferTypeImpl.VarLongType();
    Type<byte[]> RAW_BYTES = new NetworkBufferTypeImpl.RawBytesType(-1);
    Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    Type<Key> KEY = STRING.transform(Key::key, Key::asString);
    Type<String> STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    Type<String> STRING_IO_UTF8 = new NetworkBufferTypeImpl.StringIOUTFType();
    Type<BinaryTag> NBT = NetworkBufferTypeImpl.NbtType.typed();
    Type<CompoundBinaryTag> NBT_COMPOUND = NetworkBufferTypeImpl.NbtType.typed();
    // TAG_END special encoding for nullables.
    Type<@Nullable BinaryTag> OPTIONAL_NBT = NetworkBufferTypeImpl.OptionalNBTType.typed();
    // TAG_END special encoding for nullables.
    Type<@Nullable CompoundBinaryTag> OPTIONAL_NBT_COMPOUND = NetworkBufferTypeImpl.OptionalNBTType.typed();
    Type<Point> BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    Type<Component> COMPONENT = new ComponentNetworkBufferTypeImpl();
    Type<Component> JSON_COMPONENT = new NetworkBufferTypeImpl.JsonComponentType();
    Type<UUID> UUID = new NetworkBufferTypeImpl.UUIDType();
    Type<Pos> POS = new NetworkBufferTypeImpl.PosType();

    Type<byte[]> BYTE_ARRAY = new NetworkBufferTypeImpl.ByteArrayType();
    Type<long[]> LONG_ARRAY = new NetworkBufferTypeImpl.LongArrayType();
    Type<int[]> VAR_INT_ARRAY = new NetworkBufferTypeImpl.VarIntArrayType();
    Type<long[]> VAR_LONG_ARRAY = new NetworkBufferTypeImpl.VarLongArrayType();

    Type<BitSet> BITSET = LONG_ARRAY.transform(BitSet::valueOf, BitSet::toLongArray);
    Type<Instant> INSTANT_MS = LONG.transform(Instant::ofEpochMilli, Instant::toEpochMilli);
    Type<PublicKey> PUBLIC_KEY = BYTE_ARRAY.transform(KeyUtils::publicRSAKeyFrom, PublicKey::getEncoded);

    Type<Point> VECTOR3 = new NetworkBufferTypeImpl.Vector3Type();
    Type<Point> VECTOR3D = new NetworkBufferTypeImpl.Vector3DType();
    Type<Point> VECTOR3I = new NetworkBufferTypeImpl.Vector3IType();
    Type<Point> VECTOR3B = new NetworkBufferTypeImpl.Vector3BType();
    Type<Vec> LP_VECTOR3 = new NetworkBufferTypeImpl.LpVector3Type();
    Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();
    Type<Float> LP_ANGLE = BYTE.transform(to -> to * 360f / 256f, from -> (byte) (from * 256f / 360f));

    Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();

    Type<Direction> DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    /**
     * Creates an enum type from the enum class
     * <br>
     * Encoded as a {@link #VAR_INT} from the ordinal value, unless the enum has less than 128 values,
     * in which case it will be encoded as a {@link #BYTE}, which should be a single VarInt value.
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the new enum type
     */
    @Contract(pure = true, value = "_ -> new")
    static <E extends Enum<E>> Type<E> Enum(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        final E[] values = enumClass.getEnumConstants();
        // Use byte transform for small enums (likely the case).
        if (values.length < 128)
            // 0x7F (127) is the max value for a single byte VarInt.
            return BYTE.transform(integer -> values[integer], it -> (byte) it.ordinal());
        // Otherwise use VAR_INT
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    /**
     * Creates an enum set type from the enum class
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the new enum set type
     */
    @Contract(pure = true, value = "_ -> new")
    static <E extends Enum<E>> Type<EnumSet<E>> EnumSet(Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, values, FixedBitSet(values.length));
    }

    /**
     * Creates a fixed bit set type with the specified length.
     * <br>
     * Note: If there aren't enough bits set during writing, the value will be padded with 0's.
     *
     * @param length the length
     * @return the type
     * @throws IllegalArgumentException if {@code length} is less than zero
     */
    @Contract(pure = true, value = "_ -> new")
    static Type<BitSet> FixedBitSet(int length) {
        return new NetworkBufferTypeImpl.FixedBitSetType(length, FixedRawBytes((length + 7) / Long.BYTES));
    }

    /**
     * Creates a type that reads/writes in {@code length} bytes.
     *
     * @param length the length
     * @return the new type
     * @throws IllegalArgumentException if {@code length} is less than zero
     */
    @Contract(pure = true, value = "_ -> new")
    static Type<byte[]> FixedRawBytes(int length) {
        Check.argCondition(length < 0, "Length is negative found {0}", length);
        return new NetworkBufferTypeImpl.RawBytesType(length); // Cannot check in here since -1 is used for RAW_BYTES.
    }

    /**
     * Lazily compute the Type required for serialization.
     * <br>
     * Note your implementation should be thread safe, and should normally be called once. This may be updated to become a stable value.
     *
     * @param supplier the supplier
     * @param <T>      the type
     * @return the new type
     */
    @Contract(pure = true, value = "_ -> new")
    static <T> Type<T> Lazy(Supplier<Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }

    /**
     * Pass the type required for serialization for an inner part. Useful to break initialization where you only need one layer deep.
     * <br>
     * Note your implementation should be thread safe, and should normally be called once. This may be updated to become a stable value.
     *
     * @param supplier the supplier
     * @param <T>      the type
     * @return the new type
     */
    @Contract(pure = true, value = "_ -> new")
    static <T> Type<T> Recursive(UnaryOperator<Type<T>> supplier) {
        return new NetworkBufferTypeImpl.RecursiveType<>(supplier).delegate();
    }

    /**
     * Creates a typed NBT serializer using a {@link Codec}
     *
     * @param serializer the serializer
     * @param <T>        the codec type
     * @return the new type
     */
    @Contract(pure = true, value = "_ -> new")
    static <T> Type<T> TypedNBT(Codec<T> serializer) {
        return new NetworkBufferTypeImpl.TypedNbtType<>(serializer);
    }

    /**
     * Either type for {@link L} and {@link R}
     *
     * @param left  the left type
     * @param right the right type
     * @param <L>   left type
     * @param <R>   right type
     * @return the new type for Either
     */
    @Contract(pure = true, value = "_, _ -> new")
    static <L, R> Type<Either<L, R>> Either(NetworkBuffer.Type<L> left, NetworkBuffer.Type<R> right) {
        return new NetworkBufferTypeImpl.EitherType<>(left, right);
    }

    /**
     * Creates a new static buffer using {@link NetworkBufferFactory#staticFactory()}.
     *
     * @param size       the size to use for {@link NetworkBufferFactory#allocate(long)}
     * @param registries the registries to use
     * @return the new network buffer
     */
    @Contract("_, _ -> new")
    static NetworkBuffer staticBuffer(long size, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return NetworkBufferFactory.staticFactory().registry(registries).allocate(size);
    }

    /**
     * Creates a new static buffer using {@link NetworkBufferFactory#staticFactory()}.
     *
     * @param size the size to use for {@link NetworkBufferFactory#allocate(long)}
     * @return the new network buffer
     */
    @Contract("_ -> new")
    static NetworkBuffer staticBuffer(long size) {
        return NetworkBufferFactory.staticFactory().allocate(size);
    }

    /**
     * Creates a resizeable buffer using {@link NetworkBufferFactory#resizeableFactory()}
     *
     * @param initialSize the initial size to use for {@link NetworkBufferFactory#allocate(long)}
     * @param registries  the registries to use
     * @return the new buffer
     */
    @Contract("_, _ -> new")
    static NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return NetworkBufferFactory.resizeableFactory()
                .registry(registries)
                .allocate(initialSize);
    }

    /**
     * Creates a resizeable buffer using {@link NetworkBufferFactory#resizeableFactory()}
     *
     * @param initialSize the initial size to use for {@link NetworkBufferFactory#allocate(long)}
     * @return the new buffer
     */
    @Contract("_ -> new")
    static NetworkBuffer resizableBuffer(int initialSize) {
        return NetworkBufferFactory.resizeableFactory().allocate(initialSize);
    }

    /**
     * Creates a resizeable buffer using {@link #resizableBuffer(long, Registries)}
     * with an initial size of 256, determined by {@link ServerFlag#DEFAULT_RESIZEABLE_SIZE}.
     *
     * @param registries the registries to use if required during encoding/decoding.
     * @return the new buffer
     */
    @Contract("_ -> new")
    static NetworkBuffer resizableBuffer(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return resizableBuffer(ServerFlag.DEFAULT_RESIZEABLE_SIZE, registries);
    }

    /**
     * Creates a resizeable buffer
     * with an initial size of 256, determined by {@link ServerFlag#DEFAULT_RESIZEABLE_SIZE}.
     *
     * @return the new buffer
     */
    @Contract("-> new")
    static NetworkBuffer resizableBuffer() {
        return resizableBuffer(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
    }

    /**
     * Wrap the {@link MemorySegment} into a {@link NetworkBuffer} with the registries.
     * <br>
     * Useful when you already have a memory segment.
     *
     * @param segment    the segment
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @param registries the {@link #registries()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _, _ -> new")
    @ApiStatus.Experimental
    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        Objects.requireNonNull(segment, "segment");
        return NetworkBufferSegmentProvider.INSTANCE.wrap(segment, readIndex, writeIndex, registries);
    }

    /**
     * Wrap the {@link MemorySegment} into a {@link NetworkBuffer} without registries.
     * Useful when you already have a memory segment.
     *
     * @param segment    the segment
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _ -> new")
    @ApiStatus.Experimental
    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex) {
        return wrap(segment, readIndex, writeIndex, null);
    }

    /**
     * Wrap the byte array into a {@link NetworkBuffer} with the registries.
     * Useful when you already have a {@code byte[]}.
     *
     * @param bytes      the bytes
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @param registries the {@link #registries()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _, _ -> new")
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, @Nullable Registries registries) {
        Objects.requireNonNull(bytes, "bytes");
        return NetworkBufferSegmentProvider.INSTANCE.wrap(bytes, readIndex, writeIndex, registries);
    }

    /**
     * Wrap the byte array into a {@link NetworkBuffer}.
     * Useful when you already have a {@code byte[]}.
     *
     * @param bytes      the bytes
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _ -> new")
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return wrap(bytes, readIndex, writeIndex, null);
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
    static byte[] makeArray(Consumer<? super NetworkBuffer> writing, @Nullable Registries registries) {
        Objects.requireNonNull(writing, "writing");
        return NetworkBufferSegmentProvider.INSTANCE.makeArray(writing, registries);
    }

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
    static byte[] makeArray(Consumer<? super NetworkBuffer> writing) {
        return makeArray(writing, null);
    }

    /**
     * Creates a byte array from the type and value registries.
     * <br>
     * Note: only the current thread can use the buffer.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     *
     * @param type       the {@link Type} for {@link T}
     * @param value      the value
     * @param registries the registries to use in serialization
     * @param <T>        the type
     * @return the smallest byte array to represent {@link T}
     */
    @Contract("_ ,_, _ -> new")
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value, @Nullable Registries registries) {
        Objects.requireNonNull(type, "type");
        return NetworkBufferSegmentProvider.INSTANCE.makeArray(type, value, registries);
    }

    /**
     * Creates a byte array from the type and value without registries.
     * <br>
     * Note: only the current thread can use the buffer.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     *
     * @param type  the {@link Type} for {@link T}
     * @param value the value
     * @param <T>   the type
     * @return the smallest byte array to represent {@link T}
     */
    @Contract("_, _ -> new")
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value) {
        return makeArray(type, value, null);
    }

    /**
     * Copies the src {@link NetworkBuffer} into the destination {@link NetworkBuffer}
     * <br>
     *
     * @param srcBuffer the source
     * @param srcOffset the source offset
     * @param dstBuffer the destination
     * @param dstOffset the destination offset
     * @param length    the length to copy
     * @throws UnsupportedOperationException if {@code srcBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is read-only
     */
    @Contract(mutates = "param3")
    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        Objects.requireNonNull(srcBuffer, "srcBuffer");
        Objects.requireNonNull(dstBuffer, "dstBuffer");
        srcBuffer.copyTo(srcOffset, dstBuffer, dstOffset, length);
    }

    /**
     * @param buffer1 the buffer
     * @param buffer2 the buffer
     * @return if they are equals
     * @deprecated Use NetworkBuffer#contentEquals instead.
     */
    @Deprecated(forRemoval = true)
    static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        return contentEquals(buffer1, buffer2);
    }

    /**
     * Checks if the contents of one buffer in its entirety.
     * Buffers with the same address and capacity will always be true.
     * <br>
     * Note: Dummy buffers are never equal in content.
     *
     * @param buffer1 the left buffer
     * @param buffer2 the right buffer
     * @return true if the content is equal
     */
    @Contract(pure = true)
    static boolean contentEquals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        Objects.requireNonNull(buffer1, "buffer1");
        Objects.requireNonNull(buffer2, "buffer2");
        return buffer1.contentEquals(buffer2);
    }

    /**
     * Creates a dummy buffer, useful for size calculations
     * <br>
     * A dummy buffer is one that can always be written, modified, but never read from.
     * Therefore, has an observed blank state, which could be reused over and over, also the benefit of no native allocations.
     * <br>
     * Operations that require the dummy buffer to be read or passed into logic where it's required will throw an exception.
     *
     * @param registries the registries to use if applicable
     * @return the new dummy buffer
     * @throws UnsupportedOperationException during usage, if directly called to read.
     * @throws RuntimeException if used on another implementation, that requires more underlying access.
     */
    @Contract(pure = true, value = "_ -> new")
    static NetworkBuffer dummy(@Nullable Registries registries) {
        return new NetworkBufferDummy(0, registries);
    }

    /**
     * Writes the value of {@link T} at {@link #writeIndex()}
     * <br>
     * Writing may require resizing so any side effects of {@link #resize(long)} could happen.
     *
     * @param type  the type
     * @param value the value to write
     * @param <T>   the type
     * @throws IndexOutOfBoundsException if the write index is out of bounds.
     */
    @Contract(mutates = "this")
    default <T extends @UnknownNullability Object> void write(Type<T> type, T value) throws IndexOutOfBoundsException {
        type.write(this, value);
    }

    /**
     * Reads the value of {@link T} at {@link #readIndex()}
     *
     * @param type type
     * @param <T>  the type
     * @return the value
     * @throws IndexOutOfBoundsException if the read index is out of bounds.
     */
    @Contract(mutates = "this")
    default <T extends @UnknownNullability Object> T read(Type<T> type) throws IndexOutOfBoundsException {
        return type.read(this);
    }

    /**
     * Write the value of {@link T} using at {@code index}
     * <br>
     * Note: Temporarily sets the write index to {@code index} to be used then restored at the end.
     *
     * @param index the index to write at
     * @param type  the type
     * @param value the value of T
     * @param <T>   the type
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    @Contract(mutates = "this")
    default <T extends @UnknownNullability Object> void writeAt(long index, Type<T> type, T value) throws IndexOutOfBoundsException {
        final long oldWriteIndex = writeIndex();
        writeIndex(index);
        try {
            write(type, value);
        } finally {
            writeIndex(oldWriteIndex);
        }
    }

    /**
     * Read the value of {@link T} using at {@code index}
     * <br>
     * Note: Temporarily sets the read index to {@code index} to be used then restored at the end.
     *
     * @param index the index to read at
     * @param type  the type
     * @param <T>   the type
     * @return the value {@link T}
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    @Contract(mutates = "this", value = "_, _ -> new")
    default <T extends @UnknownNullability Object> T readAt(long index, Type<T> type) throws IndexOutOfBoundsException {
        final long oldReadIndex = readIndex();
        readIndex(index);
        try {
            return read(type);
        } finally {
            readIndex(oldReadIndex);
        }
    }

    /**
     * @param srcOffset  the source offset
     * @param dest       the dest buffer
     * @param destOffset the destination offset
     * @param length     the length
     * @deprecated Use {@link #copyTo(long, byte[], int, int)} instead as the length and destination offsets are integers.
     */
    @Deprecated(forRemoval = true) // No longer long's
    default void copyTo(long srcOffset, byte[] dest, long destOffset, long length) {
        this.copyTo(srcOffset, dest, Math.toIntExact(destOffset), Math.toIntExact(length));
    }

    /**
     * Copies the buffer from {@code sourceOffset} to the {@code length}.
     *
     * @param srcOffset  the source offset
     * @param dest       the dest buffer
     * @param destOffset the destination offset
     * @param length     the length
     */
    @Contract(mutates = "param2")
    void copyTo(long srcOffset, byte[] dest, int destOffset, int length);


    /**
     * Copies the src {@link NetworkBuffer} into the destination {@link NetworkBuffer}
     * <br>
     *
     * @param srcOffset  the source offset
     * @param destBuffer the destination
     * @param destOffset the destination offset
     * @param length     the length to copy
     * @throws UnsupportedOperationException if {@code srcBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is read-only
     */
    @Contract(mutates = "param2")
    void copyTo(long srcOffset, NetworkBuffer destBuffer, long destOffset, long length);

    /**
     * Fill the buffer with the byte value specified.
     * <br>
     * Useful if you want to zero a buffer after use if required.
     *
     * @param srcOffset the buffer
     * @param length    the length
     * @param value     the value to fill
     * @throws UnsupportedOperationException if this buffer is a dummy
     * @throws UnsupportedOperationException if this buffer is a read-only
     */
    void fill(long srcOffset, long length, byte value);

    /**
     * @param extractor the consumer of the network buffer
     * @return the bytes extracted
     * @deprecated Use {@link #extractReadBytes(Consumer)}
     * Consume read bytes from the extractor. Using {@link #readIndex()}
     * <br>
     * If you require the written bytes use {@link #extractWrittenBytes(Consumer)}
     */
    @Contract("_ -> new")
    @Deprecated(forRemoval = true)
    default byte[] extractBytes(Consumer<NetworkBuffer> extractor) {
        return extractReadBytes(extractor);
    }

    /**
     * Consume read bytes from the extractor. Using {@link #readIndex()}
     * <br>
     * If you require the write index bytes use {@link #extractWrittenBytes(Consumer)}
     *
     * @param type the type to extract
     * @return the bytes extracted
     */
    @Contract(mutates = "this", value = "_ -> new")
    default byte[] extractReadBytes(Type<?> type) {
        Objects.requireNonNull(type, "type");
        return extractReadBytes(buffer -> buffer.read(type));
    }

    /**
     * Consume read bytes from the extractor. Using {@link #readIndex()}
     * <br>
     * If you require the write index bytes use {@link #extractWrittenBytes(Consumer)}
     *
     * @param extractor the consumer of the network buffer
     * @return the bytes extracted
     */
    @Contract(mutates = "this", value = "_ -> new")
    byte[] extractReadBytes(Consumer<? super NetworkBuffer> extractor);

    /**
     * Consume read bytes from the extractor. Using {@link #readIndex()}
     * <br>
     * If you require the write index bytes use {@link #extractWrittenBytes(Consumer)}
     *
     * @param type the type to extract
     * @return the bytes extracted
     */
    @Contract(mutates = "this", value = "_, _ -> new")
    default <T extends @UnknownNullability Object> byte[] extractWrittenBytes(Type<T> type, T value) {
        Objects.requireNonNull(type, "type");
        return extractWrittenBytes(buffer -> buffer.write(type, value));
    }

    /**
     * Consume written bytes from the extractor. Using {@link #writeIndex()}
     * <br>
     * If you require the read index bytes use {@link #extractReadBytes(Consumer)}
     *
     * @param extractor the consumer of the network buffer
     * @return the bytes extracted
     */
    @Contract(mutates = "this", value = "_ -> new")
    byte[] extractWrittenBytes(Consumer<? super NetworkBuffer> extractor);

    /**
     * Clears the data tracked by this buffer by setting the {@link #index(long, long)} to 0.
     * <br>
     * Note: the implementation does not require zeroing of the previously stored data,
     * instead use {@link NetworkBuffer#fill(long, long, byte)} if you require this.
     *
     * @return this
     */
    @Contract("-> this")
    default NetworkBuffer clear() {
        return index(0, 0);
    }

    /**
     * Returns the write index tracked by this buffer
     *
     * @return the write index
     */
    long writeIndex();

    /**
     * Returns the read index tracked by this buffer
     *
     * @return the read index
     */
    long readIndex();

    /**
     * Sets the write index
     *
     * @param writeIndex the new write index
     * @return this
     */
    @Contract("_ -> this")
    NetworkBuffer writeIndex(long writeIndex);

    /**
     * Sets the read index
     *
     * @param readIndex the new read index
     * @return this
     */
    @Contract("_ -> this")
    NetworkBuffer readIndex(long readIndex);

    /**
     * Sets both {@link #writeIndex()} and {@link #writeIndex()} to the specified ones
     *
     * @param readIndex  the new read index
     * @param writeIndex the new write index
     * @return this
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    default NetworkBuffer index(long readIndex, long writeIndex) {
        writeIndex(writeIndex);
        readIndex(readIndex);
        return this;
    }

    /**
     * Advances the write index and returns the previous index, while storing the new index into {@link #writeIndex()}
     *
     * @param length the length to advance
     * @return the previous write index
     * @throws IllegalArgumentException if {@code length < 0}
     */
    @Contract(mutates = "this")
    default long advanceWrite(@Range(from = 0, to = Long.MAX_VALUE) long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        final long oldWriteIndex = writeIndex();
        writeIndex(oldWriteIndex + length);
        return oldWriteIndex;
    }

    /**
     * Advances the read index and returns the previous index, while storing the new index into {@link #readIndex()}
     *
     * @param length the length to advance
     * @return the previous read index
     * @throws IllegalArgumentException if {@code length < 0}
     */
    @Contract(mutates = "this")
    default long advanceRead(@Range(from = 0, to = Long.MAX_VALUE) long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        final long oldReadIndex = readIndex();
        readIndex(oldReadIndex + length);
        return oldReadIndex;
    }

    /**
     * Readable bytes are the number of bytes that have been written to the {@link #writeIndex()}
     * The readable bytes can be calculated by {@link #writeIndex()} - {@link #readIndex()}.
     *
     * @return the readable bytes
     */
    @Contract(pure = true)
    default long readableBytes() {
        return writeIndex() - readIndex();
    }

    /**
     * Writeable bytes are the number of bytes that are left in the buffer from the {@link #writeIndex()}
     * The writeable bytes can be calculated by {@link #capacity()} - {@link #writeIndex()}.
     *
     * @return the writeable bytes
     */
    @Contract(pure = true)
    default long writableBytes() {
        return capacity() - writeIndex();
    }

    /**
     * Gets the capacity for the buffer or its length.
     *
     * @return the capacity/length
     */
    @Contract(pure = true)
    @Range(from = 0, to = Long.MAX_VALUE)
    long capacity();

    /**
     * Creates a read-only version of this buffer.
     * <br>
     * Note: While this can be a view, during resizing of the original buffer this may no longer be valid.
     *
     * @return new static buffer
     */
    @Contract(pure = true)
    NetworkBuffer readOnly();

    /**
     * Returns true if the buffer has previously been {@link #readOnly()}
     *
     * @return true if the buffer is read-only
     */
    @Contract(pure = true)
    boolean isReadOnly();

    /**
     * Returns true if the buffer is a resizable buffer.
     * <br>
     * If false, the buffer is static and cannot be resized and {@link #resize(long)} will always fail.
     *
     * @return true if the buffer is resizable
     */
    @Contract(pure = true)
    boolean isResizable();

    /**
     * Resize the buffer to {@code length} the new {@link #capacity()}.
     * <br>
     * Note: This throws away the existing arena so it can be freed.
     * You can set a fixed arena by {@link NetworkBufferFactory#arena(Arena)}
     *
     * @param length the new size
     * @throws IllegalArgumentException      if {@code length < 0}
     * @throws IllegalArgumentException      if the new size is less than or equal to the current {@link #capacity()}.
     * @throws UnsupportedOperationException if this buffer cannot be resized
     * @throws UnsupportedOperationException if this buffer is a dummy
     * @throws UnsupportedOperationException if this buffer is read-only
     */
    @Contract(mutates = "this")
    void resize(@Range(from = 0, to = Long.MAX_VALUE) long length);

    /**
     * Ensures that the buffer {@link #writableBytes()} is greater or equal to {@code length}.
     * Otherwise, the buffer will be resized using {@link #resize(long)} if {@link #isResizable()} is true.
     *
     * @param length the length to ensure
     * @throws IllegalArgumentException  if {@code length < 0}
     * @throws IndexOutOfBoundsException if the upsize does not permit the length to be written
     */
    @Contract(mutates = "this")
    default void ensureWritable(@Range(from = 0, to = Long.MAX_VALUE) long length) throws IndexOutOfBoundsException {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative found %d".formatted(length));
        if (writableBytes() < length && !requestCapacity(writeIndex() + length))
            throw new IndexOutOfBoundsException("%d is too long to be writeable: %d".formatted(length, writableBytes()));
    }

    /**
     * Attempts to resize the current buffer, to the targetSize or greater using {@link AutoResize} strategy,
     * then uses {@link #resize(long)}.
     *
     * @param targetSize the request size minimum we need
     * @return true if successful, so at least targetSize is the new capacity.
     */
    @Contract(mutates = "this")
    boolean requestCapacity(long targetSize);

    /**
     * Ensures that the buffer {@link #readableBytes()} is greater or equal to {@code length}.
     *
     * @param length the length to ensure
     * @throws IllegalArgumentException  if {@code length < 0}
     * @throws IndexOutOfBoundsException if the buffer does not have enough data for this length.
     */
    @Contract(pure = true)
    default void ensureReadable(@Range(from = 0, to = Long.MAX_VALUE) long length) throws IndexOutOfBoundsException {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative found %d".formatted(length));
        if (readableBytes() < length)
            throw new IndexOutOfBoundsException("%d is too long to be readable: %s".formatted(length, readableBytes()));
    }

    /**
     * Compact (copies) all the data from the {@link #readIndex()} to the {@link #writeIndex()} to be zero aligned.
     * This does not change the buffer capacity, instead it's a simple copy.
     */
    @Contract(mutates = "this")
    default void compact() {
        if (readIndex() == 0) return;
        copyTo(readIndex(), this, 0, readableBytes());
        writeIndex(readableBytes());
        readIndex(0);
    }

    /**
     * Resizes this buffer to be trimmed and assigns it to this {@link NetworkBuffer}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the occupied data.
     * <br>
     * Like {@link #compact()} the buffer will be zero aligned (by copy), but unlike compact the capacity may be shrunk.
     *
     * @throws UnsupportedOperationException if this buffer cannot be trimmed (resized)
     * @see #trimmed()
     */
    @Contract(mutates = "this")
    default void trim() {
        compact();
    }

    /**
     * Creates a copy of the buffer trimmed using the factory to {@link NetworkBufferFactory#staticFactory()}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the only occupied data.
     *
     * @return the trimmed buffer
     * @see #trim()
     */
    @Contract("-> new")
    default NetworkBuffer trimmed() {
        return trimmed(NetworkBufferFactory.staticFactory());
    }

    /**
     * Creates a copy of the buffer trimmed using the factory to {@link NetworkBufferFactory#allocate(long)}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the only occupied data.
     *
     * @param factory the factory to allocate from
     * @return the trimmed buffer
     * @see #trim()
     */
    @Contract("_, -> new")
    default NetworkBuffer trimmed(NetworkBufferFactory factory) {
        final long readableBytes = readableBytes();
        return copy(factory, readIndex(), readableBytes, 0, readableBytes);
    }

    /**
     * Copies the current buffer using the factory specified {@link NetworkBufferFactory#staticFactory()}
     * with the index to the length using {@link #readIndex()} and {@link #writeIndex()}.
     *
     * @param index  the starting index
     * @param length the length
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _ -> new")
    default NetworkBuffer copy(long index, long length) {
        return copy(index, length, readIndex(), writeIndex());
    }

    /**
     * Copies the current buffer using the {@link NetworkBufferFactory} with the index to the length with
     * the using {@link #readIndex()} and {@link #writeIndex()}.
     *
     * @param factory the {@link NetworkBufferFactory} which {@link NetworkBufferFactory#allocate(long)} will be used for the new buffer.
     * @param index   the index
     * @param length  the length
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _, _ -> new")
    default NetworkBuffer copy(NetworkBufferFactory factory, long index, long length) {
        return copy(factory, index, length, readIndex(), writeIndex());
    }

    /**
     * Copies the current buffer using the factory specified {@link NetworkBufferFactory#staticFactory()}
     * with the index to the length with the new specified read and write indexes.
     *
     * @param index      the starting index
     * @param length     the length
     * @param readIndex  the read index
     * @param writeIndex the write index
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _, _, _ -> new")
    default NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        return copy(NetworkBufferFactory.staticFactory(), index, length, readIndex, writeIndex);
    }

    /**
     * Copies the current buffer using the {@link NetworkBufferFactory} with the index to the length with the new specified read and write indexes.
     *
     * @param factory    the {@link NetworkBufferFactory} which {@link NetworkBufferFactory#allocate(long)} will be used for the new buffer.
     * @param index      the starting index
     * @param length     the length
     * @param readIndex  the new read index
     * @param writeIndex the new write index
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _, _, _, _ -> new")
    NetworkBuffer copy(NetworkBufferFactory factory, long index, long length, long readIndex, long writeIndex);

    /**
     * Creates a slice from the starting index to the length passing the read index and write index supplied
     * backed by the current {@link NetworkBuffer}
     * <br>
     * Note: if the buffer is resizable, this cannot be guaranteed to be a view.
     *
     * @param index      the starting index
     * @param length     the length
     * @param readIndex  the new read index
     * @param writeIndex the new write index
     * @return the network buffer slice
     */
    @Contract(pure = true, value = "_, _, _, _ -> new")
    NetworkBuffer slice(long index, long length, long readIndex, long writeIndex);

    /**
     * Creates a slice from the starting index to the length
     * backed by the current {@link NetworkBuffer}
     *
     * @param index  the starting index
     * @param length the length
     * @return a slice defined in {@link #slice(long, long, long, long)}
     */
    @Contract(pure = true, value = "_, _ -> new")
    default NetworkBuffer slice(long index, long length) {
        return slice(index, length, readIndex(), writeIndex());
    }

    /**
     * Reads the current buffer with the {@link ReadableByteChannel}
     * <br>
     * Uses the {@link #writableBytes()} starting from {@link #writeIndex()}
     *
     * @param channel the channel to write to
     * @return the amount of bytes read
     * @throws IOException if -1 bytes were read.
     */
    int readChannel(ReadableByteChannel channel) throws IOException;

    /**
     * Write the current buffer into the {@link WritableByteChannel}
     * <br>
     * Uses the {@link #readableBytes()} starting from {@link #readIndex()}
     *
     * @param channel the channel to write to
     * @return true if fully written, false otherwise
     * @throws IOException if -1 bytes were written.
     */
    boolean writeChannel(WritableByteChannel channel) throws IOException;

    /**
     * Encrypt/Decrypt this network buffer using a {@link Cipher}
     *
     * @param cipher the cipher to use
     * @param start  the start index
     * @param length the length
     */
    void cipher(Cipher cipher, long start, long length);

    /**
     * Compress this buffer into the output using {@link java.util.zip.Deflater}
     *
     * @param start  the start index
     * @param length the length
     * @param output the output buffer
     * @return the number of bytes that were compressed
     */
    long compress(long start, long length, NetworkBuffer output);

    /**
     * Decompress this buffer into the output using {@link java.util.zip.Inflater}
     *
     * @param start  the start index
     * @param length the length
     * @param output the output buffer
     * @return the number of bytes that were decompressed
     * @throws DataFormatException if the data is invalid
     */
    long decompress(long start, long length, NetworkBuffer output) throws DataFormatException;

    /**
     * The registries used when creating with {@link NetworkBufferFactory#registry(Registries)}
     *
     * @return the registries
     */
    @Nullable Registries registries();

    /**
     * Creates a new {@link IOView} of this buffer.
     * <br>
     * Useful to interface with API's that support {@link DataInput} or {@link DataOutput}.
     *
     * @return the io view.
     */
    @Contract(pure = true, value = "-> new")
    default IOView ioView() {
        return () -> NetworkBuffer.this;
    }

    /**
     * Gets the direct view of this buffer.
     * <br>
     * Used for direct access to read and write at indexes. Used for implementations like {@link #BYTE}
     *
     * @return the direct view
     */
    @ApiStatus.OverrideOnly
    @Contract(pure = true, value = "-> this")
    Direct direct();

    /**
     * Checks if the contents of one buffer in its entirety.
     * Buffers with the same address and capacity will always be true.
     * <br>
     * Note: Dummy buffers are never equal in content.
     *
     * @param buffer the right buffer
     * @return true if the content is equal
     */
    @Contract(pure = true)
    boolean contentEquals(NetworkBuffer buffer);

    /**
     * Tests to see if the current buffer equals in identity to the other buffer.
     * <br>
     * Note: This relies on {@code this == obj}.
     *
     * @param obj the reference object with which to compare.
     * @return true if equal in identity
     */
    @Override
    boolean equals(@Nullable Object obj);

    /**
     * The unique hashcode conforming to {@link Object#hashCode()}.
     * <br>
     * Note: This relies on identity using {@link System#identityHashCode(Object)}.
     *
     * @return the hash code.
     * @see #equals(Object)
     */
    @Override
    int hashCode();

    /**
     * A type is a writer/reader for {@link T} it attempts to provide a bidirectional guarantee.
     * Through {@link #write(NetworkBuffer, Object)} and {@link #read(NetworkBuffer)}.
     * <br>
     * Unlike {@link net.minestom.server.codec.StructCodec} types are always written linearly into a {@link NetworkBuffer}
     * <br>
     * You should use templates wherever possibly to ensure bidirectional serialization.
     *
     * @param <T> the type, nullable.
     */
    interface Type<T extends @UnknownNullability Object> {
        /**
         * Write {@link T} to a {@link NetworkBuffer}.
         *
         * @param buffer the buffer to use
         * @param value  the value
         */
        @Contract(mutates = "param1")
        void write(NetworkBuffer buffer, T value);

        /**
         * Read the value from the {@link NetworkBuffer}
         *
         * @param buffer the buffer
         * @return {@link T}
         */
        @Contract(mutates = "param1")
        T read(NetworkBuffer buffer);

        /**
         * Determines the sizeOf {@link T} using the registries provided.
         * <br>
         * Consider overriding this version as {@link #sizeOf(Object)} calls into this.
         *
         * @param value      the value to get the size of
         * @param registries the registries
         * @return the size
         */
        @Contract(pure = true)
        @Range(from = 0, to = Long.MAX_VALUE)
        default long sizeOf(T value, @Nullable Registries registries) {
            final NetworkBuffer dummy = NetworkBuffer.dummy(registries);
            dummy.write(this, value);
            return dummy.writeIndex();
        }

        /**
         * Determines the sizeOf {@link T}.
         * <br>
         * Consider overriding {@link #sizeOf(Object, Registries)} instead if applicable.
         *
         * @param value the value to get the size of
         * @return the size
         * @see #sizeOf(Object, Registries)
         */
        @Contract(pure = true)
        @Range(from = 0, to = Long.MAX_VALUE)
        default long sizeOf(T value) {
            return sizeOf(value, null);
        }

        /**
         * Transform the current type {@link T} to {@link S} and {@link S} to {@link T}.
         *
         * @param to   the function to call when reading your value
         * @param from the function to call when writing your value
         * @param <S>  type to
         * @return the new type that transforms {@link T}
         */
        @Contract(pure = true, value = "_, _ -> new")
        default <S extends @UnknownNullability Object> Type<S> transform(Function<? super T, ? extends S> to, Function<? super S, ? extends T> from) {
            return new NetworkBufferTypeImpl.TransformType<>(this, to, from);
        }

        /**
         * Transform the current type {@link T} to {@link S} and {@link S} to {@link T}.
         * <br>
         * This call site is more optimized as we can look at the underlying implementation, please use this for templates.
         *
         * @param to   the function to call when reading your value
         * @param from the function to call when writing your value
         * @param <S>  type to
         * @return the new type that transforms {@link T}
         */
        @Contract(pure = true, value = "_, _ -> new")
        default <S extends @UnknownNullability Object> Type<S> transform(Functions.SF1<? super T, ? extends S> to, Functions.SF1<? super S, ? extends T> from) {
            // Delegate back, we just want them to use the SF1 if possible.
            return transform((Function<? super T, ? extends S>) to, from);
        }

        /**
         * Creates a map type to map the value of {@link T} with {@link V} into an unmodifiable map.
         *
         * @param valueType the value type
         * @param maxSize   the max size before throwing
         * @param <V>       the value type
         * @return the type
         */
        @Contract(pure = true, value = "_, _ -> new")
        default <V> Type<@Unmodifiable Map<T, V>> mapValue(Type<V> valueType, int maxSize) {
            return new NetworkBufferTypeImpl.MapType<>(this, valueType, maxSize);
        }

        /**
         * Creates a map type to map the value of {@link T} with {@link V} into an unmodifiable map.
         * <br>
         * Note the max length allowed is {@link Integer#MAX_VALUE}, if you have a strict upperbound use {@link #mapValue(Type, int)}
         *
         * @param valueType the value type
         * @param <V>       the value type
         * @return the type
         */
        @Contract(pure = true, value = "_ -> new")
        default <V> Type<@Unmodifiable Map<T, V>> mapValue(Type<V> valueType) {
            return mapValue(valueType, Integer.MAX_VALUE);
        }

        /**
         * Creates an unmodifiable list type for {@link T} with its max sized defined
         * <br>
         * Note the encoding for null lists is a 0 byte.
         *
         * @param maxSize the max size before throwing.
         * @return the list type for {@link T}
         */
        @Contract(pure = true, value = "_ -> new")
        default Type<@Unmodifiable @UnknownNullability List<T>> list(int maxSize) {
            return new NetworkBufferTypeImpl.ListType<>(this, maxSize);
        }

        /**
         * Creates an unmodifiable list type for {@link T} with no max size defined.
         * <br>
         * Note the max length allowed is {@link Integer#MAX_VALUE}, if you have a strict upperbound use {@link #list(int)}
         * <br>
         * Note the encoding for null lists is a 0 byte.
         *
         * @return the list type for {@link T}
         */
        @Contract(pure = true, value = "-> new")
        default Type<@Unmodifiable @UnknownNullability List<T>> list() {
            return list(Integer.MAX_VALUE);
        }

        /**
         * Creates an unmodifiable set type for {@link T} with no max size defined.
         * <br>
         * Note the max length allowed is {@link Integer#MAX_VALUE}, if you have a strict upperbound use {@link #list(int)}
         * <br>
         * Note the encoding for null lists is a 0 byte.
         *
         * @return the list type for {@link T}
         */
        @Contract(pure = true, value = "_ -> new")
        default Type<@Unmodifiable @UnknownNullability Set<T>> set(int maxSize) {
            return new NetworkBufferTypeImpl.SetType<>(this, maxSize);
        }

        /**
         * Creates an unmodifiable set type for {@link T} with no max size defined.
         * <br>
         * Note the max length allowed is {@link Integer#MAX_VALUE}, if you have a strict upperbound use {@link #list(int)}
         * <br>
         * Note the encoding for null lists is a 0 byte.
         *
         * @return the list type for {@link T}
         */
        @Contract(pure = true, value = "-> new")
        default Type<@Unmodifiable @UnknownNullability Set<T>> set() {
            return set(Integer.MAX_VALUE);
        }

        /**
         * Creates an optional type for {@link T}, which allows it to have null values.
         * <br>
         * Note the encoding prefixes all {@link T} behind {@link #BOOLEAN} where its value if {@link T} is not null.
         * For example, a not null {@link T} would be true, and {@code null} would be false.
         *
         * @return the new optional type
         */
        @Contract(pure = true, value = "-> new")
        default Type<@Nullable T> optional() {
            return new NetworkBufferTypeImpl.OptionalType<>(this);
        }

        /**
         * Creates a union type for {@link T}, this allows you to map subtypes of {@link T} useful for sealed interfaces.
         *
         * @param serializers the map of {@link T} to the serializer
         * @param keyFunc     the key to use from {@link R} into {@link T} into {@code serializers}
         * @param <R>         the union type
         * @return the new union type for {@link T} using {@link R}
         */
        @Contract(pure = true, value = "_, _ -> new")
        default <R> Type<R> unionType(Function<T, NetworkBuffer.Type<? extends R>> serializers, Function<? super R, ? extends T> keyFunc) {
            return new NetworkBufferTypeImpl.UnionType<>(this, keyFunc, serializers);
        }

        /**
         * Creates a type where it prefixes the length
         *
         * @param maxLength the max length before throwing
         * @return the new length prefixed type
         */
        @Contract(pure = true, value = "_ -> new")
        default Type<T> lengthPrefixed(int maxLength) {
            return new NetworkBufferTypeImpl.LengthPrefixedType<>(this, maxLength);
        }
    }

    /**
     * Resize strategy for a {@link NetworkBuffer}.
     */
    @FunctionalInterface
    interface AutoResize {
        AutoResize DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        /**
         * Provide the buffer a new size, guaranteeing that the new size is greater than its original.
         *
         * @param capacity   the current capacity of the buffer
         * @param targetSize the target size of the buffer
         * @return the new capacity of the buffer
         */
        @Contract(pure = true)
        long resize(long capacity, long targetSize);
    }

    /**
     * Self-contained interface
     * that extends {@link DataInput} and {@link DataOutput} for mostly reading/writing binary tags.
     * <br>
     * You can access the io view of a network buffer with {@link NetworkBuffer#ioView()}
     * <br>
     * This interface is separate from {@link NetworkBuffer}
     * because we don't want DataInput and DataOutput to be part of the public API.
     * You should use {@link NetworkBuffer} instead where possible.
     * <br>
     * Note: this implementation removes checked exceptions as the backing {@link NetworkBuffer} would not throw {@link IOException}'s.
     * Also {@link #readLine()} is not implemented as it's already deprecated in {@link DataInputStream}.
     * <br>
     * You should never rely on the identity of {@link IOView} as it is a value class candidate.
     */
    interface IOView extends DataInput, DataOutput {

        @Deprecated(forRemoval = true)
        @Override
        @Contract("-> fail")
        default String readLine() {
            throw new UnsupportedOperationException("Deprecated method readLine() called, not implemented");
        }

        @Override
        default void readFully(byte[] bytes) {
            readFully(bytes, 0, bytes.length);
        }

        @Override
        default void readFully(byte[] bytes, int off, int len) {
            Objects.requireNonNull(bytes, "bytes");
            NetworkBuffer buffer = buffer();
            buffer.ensureReadable(len);
            buffer.copyTo(buffer.readIndex(), bytes, off, len);
            buffer.advanceRead(len);
        }

        @Override
        default int skipBytes(int n) {
            NetworkBuffer buffer = buffer();
            long readableBytes = buffer.readableBytes();
            if (n > readableBytes) {
                n = (int) readableBytes;
            }
            if (n > 0) buffer.advanceRead(n);
            return n;
        }

        @Override
        default boolean readBoolean() {
            return buffer().read(BOOLEAN);
        }

        @Override
        default byte readByte() {
            return buffer().read(BYTE);
        }

        @Override
        default int readUnsignedByte() {
            return buffer().read(UNSIGNED_BYTE);
        }

        @Override
        default short readShort() {
            return buffer().read(SHORT);
        }

        @Override
        default int readUnsignedShort() {
            return buffer().read(UNSIGNED_SHORT);
        }

        @Override
        default char readChar() {
            return (char) readUnsignedShort();
        }

        @Override
        default int readInt() {
            return buffer().read(INT);
        }

        @Override
        default long readLong() {
            return buffer().read(LONG);
        }

        @Override
        default float readFloat() {
            return buffer().read(FLOAT);
        }

        @Override
        default double readDouble() {
            return buffer().read(DOUBLE);
        }

        @Override
        default String readUTF() {
            return buffer().read(STRING_IO_UTF8);
        }

        @Override
        default void write(int lower) {
            buffer().write(BYTE, (byte) lower);
        }

        @Override
        default void write(byte[] bytes) {
            Objects.requireNonNull(bytes, "bytes");
            buffer().write(RAW_BYTES, bytes);
        }

        @Override
        default void write(byte[] bytes, int off, int len) {
            Objects.requireNonNull(bytes, "bytes");
            buffer().write(RAW_BYTES, Arrays.copyOfRange(bytes, off, off + len));
        }

        @Override
        default void writeBoolean(boolean value) {
            buffer().write(BOOLEAN, value);
        }

        @Override
        default void writeByte(int value) {
            buffer().write(BYTE, (byte) value);
        }

        @Override
        default void writeShort(int value) {
            buffer().write(UNSIGNED_SHORT, value);
        }

        @Override
        default void writeChar(int value) {
            buffer().write(UNSIGNED_SHORT, value);
        }

        @Override
        default void writeInt(int value) {
            buffer().write(INT, value);
        }

        @Override
        default void writeLong(long value) {
            buffer().write(LONG, value);
        }

        @Override
        default void writeFloat(float value) {
            buffer().write(FLOAT, value);
        }

        @Override
        default void writeDouble(double value) {
            buffer().write(DOUBLE, value);
        }

        @Override
        default void writeBytes(String value) {
            Objects.requireNonNull(value, "value");
            NetworkBuffer buffer = buffer();
            for (int i = 0; i < value.length(); i++) {
                buffer.write(BYTE, (byte) value.charAt(i)); // Low byte only
            }
        }

        @Override
        default void writeChars(String value) {
            Objects.requireNonNull(value, "value");
            NetworkBuffer buffer = buffer();
            for (int i = 0; i < value.length(); i++) {
                buffer.write(UNSIGNED_SHORT, (int) value.charAt(i));
            }
        }

        @Override
        default void writeUTF(String value) {
            Objects.requireNonNull(value, "value");
            buffer().write(STRING_IO_UTF8, value);
        }

        /**
         * You should avoid using this in your code and instead pass the buffer around.
         *
         * @return the backing buffer
         */
        @ApiStatus.OverrideOnly
        NetworkBuffer buffer();
    }

    /**
     * Used in a {@link NetworkBuffer} implementation to allow {@link #BYTE} to write to the backing buffer.
     */
    @ApiStatus.OverrideOnly
    interface Direct {
        void putBytes(long index, byte[] value);

        void getBytes(long index, byte[] value);

        void putByte(long index, byte value);

        byte getByte(long index);

        void putShort(long index, short value);

        short getShort(long index);

        void putInt(long index, int value);

        int getInt(long index);

        void putLong(long index, long value);

        long getLong(long index);

        void putFloat(long index, float value);

        float getFloat(long index);

        void putDouble(long index, double value);

        double getDouble(long index);

        // Warning this is writing a null terminated string
        void putString(long index, String value);

        // Warning this is reading a null terminated string
        String getString(long index);

        // Non prefixed variant
        String getString(long index, long byteLength);
    }
}
