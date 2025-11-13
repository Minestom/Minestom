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
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Either;
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
 * also buffers will attempt to use native allocation through {@link NetworkBufferAllocator} if available.
 *
 * @see Type for custom types
 * @see NetworkBufferTemplate for templating
 * @see Factory to create custom allocators
 * @see IOView to interface with existing code
 */
@SuppressWarnings("GrazieInspection")
public sealed interface NetworkBuffer permits NetworkBufferImpl {
    Type<Unit> UNIT = new NetworkBufferTypeImpl.UnitType();
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
    Type<Float> LP_ROTATION = BYTE.transform(to -> to * 360f / 256f, from -> (byte) (from * 256 / 360));

    Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();

    Type<Direction> DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    /**
     * Creates an enum type from the enum class
     * <br>
     * Encoded as a {@link #VAR_INT} from the ordinal
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the new enum type
     */
    @Contract(pure = true, value = "_ -> new")
    static <E extends Enum<E>> Type<E> Enum(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    /**
     * Creates a byte enum type from the enum class.
     * <br>
     * Encoded as a {@link #BYTE} from the ordinal
     * <br>
     * Note: You should use {@link #Enum(Class)} instead if possible.
     * As this type does not save space under normal use.
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the new enum type
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "_ -> new")
    static <E extends Enum<E>> Type<E> ByteEnum(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");
        final E[] values = enumClass.getEnumConstants();
        return UNSIGNED_BYTE.transform(integer -> values[integer], enumObject -> (short) enumObject.ordinal());
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
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, enumClass.getEnumConstants());
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
        return new NetworkBufferTypeImpl.FixedBitSetType(length);
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
     * Creates a new static buffer using {@link Factory#staticFactory()}.
     *
     * @param size       the size to use for {@link Factory#allocate(long)}
     * @param registries the registries to use
     * @return the new network buffer
     */
    @Contract("_, _ -> new")
    static NetworkBuffer staticBuffer(long size, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return NetworkBuffer.Factory.staticFactory().registry(registries).allocate(size);
    }

    /**
     * Creates a new static buffer using {@link Factory#staticFactory()}.
     *
     * @param size the size to use for {@link Factory#allocate(long)}
     * @return the new network buffer
     */
    @Contract("_ -> new")
    static NetworkBuffer staticBuffer(long size) {
        return NetworkBuffer.Factory.staticFactory().allocate(size);
    }

    /**
     * Creates a resizeable buffer using {@link Factory#resizeableFactory()}
     *
     * @param initialSize the initial size to use for {@link Factory#allocate(long)}
     * @param registries  the registries to use
     * @return the new buffer
     */
    @Contract("_, _ -> new")
    static NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return NetworkBuffer.Factory.resizeableFactory()
                .registry(registries)
                .allocate(initialSize);
    }

    /**
     * Creates a resizeable buffer using {@link Factory#resizeableFactory()}
     *
     * @param initialSize the initial size to use for {@link Factory#allocate(long)}
     * @return the new buffer
     */
    @Contract("_ -> new")
    static NetworkBuffer resizableBuffer(int initialSize) {
        return NetworkBuffer.Factory.resizeableFactory().allocate(initialSize);
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
    static NetworkBuffer wrap(MemorySegment segment, int readIndex, int writeIndex, Registries registries) {
        Objects.requireNonNull(segment, "segment");
        Objects.requireNonNull(registries, "registries");
        return NetworkBufferImpl.wrap(segment, readIndex, writeIndex, registries);
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
    static NetworkBuffer wrap(MemorySegment segment, int readIndex, int writeIndex) {
        Objects.requireNonNull(segment, "segment");
        return NetworkBufferImpl.wrap(segment, readIndex, writeIndex, null);
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
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, Registries registries) {
        Objects.requireNonNull(bytes, "bytes");
        Objects.requireNonNull(registries, "registries");
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
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
        Objects.requireNonNull(bytes, "bytes");
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex);
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
    static byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries) {
        Objects.requireNonNull(writing, "writing");
        Objects.requireNonNull(registries, "registries");
        try (Arena arena = Arena.ofConfined()) {
            final Factory factory = NetworkBuffer.Factory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(writing);
        }
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
    static byte[] makeArray(Consumer<NetworkBuffer> writing) {
        Objects.requireNonNull(writing, "writing");
        try (Arena arena = Arena.ofConfined()) {
            final Factory factory = NetworkBuffer.Factory.resizeableFactory().arena(arena);
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
     * @param type       the {@link Type} for {@link T}
     * @param value      the value
     * @param registries the registries to use in serialization
     * @param <T>        the type
     * @return the smallest byte array to represent {@link T}
     */
    @Contract("_ ,_, _ -> new")
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value, Registries registries) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(registries, "registries");
        try (Arena arena = Arena.ofConfined()) {
            final Factory factory = NetworkBuffer.Factory.resizeableFactory().arena(arena).registry(registries);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
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
        Objects.requireNonNull(type, "type");
        try (Arena arena = Arena.ofConfined()) {
            final Factory factory = NetworkBuffer.Factory.resizeableFactory().arena(arena);
            final NetworkBuffer buffer = factory.allocate(ServerFlag.DEFAULT_RESIZEABLE_SIZE);
            return buffer.extractWrittenBytes(type, value);
        }
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
        NetworkBufferImpl.copy(srcBuffer, srcOffset, dstBuffer, dstOffset, length);
    }

    /**
     * @param buffer1 the buffer
     * @param buffer2 the buffer
     * @return if they are equals
     * @deprecated Use contentEquals instead.
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
        return NetworkBufferImpl.contentEquals(buffer1, buffer2);
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
    <T extends @UnknownNullability Object> void write(Type<T> type, T value) throws IndexOutOfBoundsException;

    /**
     * Reads the value of {@link T} at {@link #readIndex()}
     *
     * @param type type
     * @param <T>  the type
     * @return the value
     * @throws IndexOutOfBoundsException if the read index is out of bounds.
     */
    @Contract(mutates = "this")
    <T extends @UnknownNullability Object> T read(Type<T> type) throws IndexOutOfBoundsException;

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
    <T extends @UnknownNullability Object> void writeAt(long index, Type<T> type, T value) throws IndexOutOfBoundsException;

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
    <T extends @UnknownNullability Object> T readAt(long index, Type<T> type) throws IndexOutOfBoundsException;

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
    byte[] extractReadBytes(Consumer<NetworkBuffer> extractor);

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
    byte[] extractWrittenBytes(Consumer<NetworkBuffer> extractor);

    /**
     * Clears the data tracked by this buffer by setting the {@link #index(long, long)} to 0.
     * <br>
     * Note: the implementation does not require zeroing of the previously stored data,
     * instead use {@link NetworkBuffer#fill(long, long, byte)} if you require this.
     *
     * @return this
     */
    @Contract("-> this")
    NetworkBuffer clear();

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
    NetworkBuffer index(long readIndex, long writeIndex);

    /**
     * Advances the write index and returns the previous index, while storing the new index into {@link #writeIndex()}
     *
     * @param length the length to advance
     * @return the previous write index
     * @throws IllegalArgumentException if {@code length < 0}
     */
    @Contract(mutates = "this")
    long advanceWrite(@Range(from = 0, to = Long.MAX_VALUE) long length);

    /**
     * Advances the read index and returns the previous index, while storing the new index into {@link #readIndex()}
     *
     * @param length the length to advance
     * @return the previous read index
     * @throws IllegalArgumentException if {@code length < 0}
     */
    @Contract(mutates = "this")
    long advanceRead(@Range(from = 0, to = Long.MAX_VALUE) long length);

    /**
     * Readable bytes are the number of bytes that have been written to the {@link #writeIndex()}
     * The readable bytes can be calculated by {@link #writeIndex()} - {@link #readIndex()}.
     *
     * @return the readable bytes
     */
    @Contract(pure = true)
    long readableBytes();

    /**
     * Writeable bytes are the number of bytes that are left in the buffer from the {@link #writeIndex()}
     * The writeable bytes can be calculated by {@link #capacity()} - {@link #writeIndex()}.
     *
     * @return the writeable bytes
     */
    @Contract(pure = true)
    long writableBytes();

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
    @Contract(pure = true, value = "-> new")
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
     * You can set a fixed arena by {@link Factory#arena(Arena)}
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
     * @throws IndexOutOfBoundsException if the resize does not permit the length to be written
     * @throws IndexOutOfBoundsException if the buffer is static and needs to be resized.
     */
    @Contract(mutates = "this")
    void ensureWritable(@Range(from = 0, to = Long.MAX_VALUE) long length);

    /**
     * Ensures that the buffer {@link #readableBytes()} is greater or equal to {@code length}.
     *
     * @param length the length to ensure
     * @throws IllegalArgumentException  if {@code length < 0}
     * @throws IndexOutOfBoundsException if the buffer does not have enough data for this length.
     */
    @Contract(pure = true)
    void ensureReadable(@Range(from = 0, to = Long.MAX_VALUE) long length);

    /**
     * Compact (copies) all the data from the {@link #readIndex()} to the {@link #writeIndex()} to be zero aligned.
     * This does not change the buffer capacity, instead it's a simple copy.
     */
    @Contract(mutates = "this")
    void compact();

    /**
     * Resizes this buffer to be trimmed and assigns it to this {@link NetworkBuffer}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the only occupied data.
     */
    @Contract(mutates = "this")
    void trim();

    /**
     * Creates a copy of the buffer trimmed using the factory to {@link Factory#staticFactory()}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the only occupied data.
     *
     * @return the trimmed buffer
     */
    @Contract("-> new")
    default NetworkBuffer trimmed() {
        return trimmed(NetworkBuffer.Factory.staticFactory());
    }

    /**
     * Creates a copy of the buffer trimmed using the factory to {@link Factory#allocate(long)}.
     * <br>
     * A trimmed buffer is one that's from its {@link #readIndex()} to its {@link #readableBytes()} is the only occupied data.
     *
     * @param factory the factory to allocate from
     * @return the trimmed buffer
     */
    @Contract("_, -> new")
    NetworkBuffer trimmed(Factory factory);

    /**
     * Copies the current buffer using the factory specified {@link Factory#staticFactory()}
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
     * Copies the current buffer using the {@link Factory} with the index to the length with
     * the using {@link #readIndex()} and {@link #writeIndex()}.
     *
     * @param factory the {@link Factory} which {@link Factory#allocate(long)} will be used for the new buffer.
     * @param index   the index
     * @param length  the length
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _, _ -> new")
    default NetworkBuffer copy(Factory factory, long index, long length) {
        return copy(factory, index, length, readIndex(), writeIndex());
    }

    /**
     * Copies the current buffer using the factory specified {@link Factory#staticFactory()}
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
        return copy(NetworkBuffer.Factory.staticFactory(), index, length, readIndex, writeIndex);
    }

    /**
     * Copies the current buffer using the {@link Factory} with the index to the length with the new specified read and write indexes.
     *
     * @param factory    the {@link Factory} which {@link Factory#allocate(long)} will be used for the new buffer.
     * @param index      the starting index
     * @param length     the length
     * @param readIndex  the new read index
     * @param writeIndex the new write index
     * @return the copy of the current buffer into a new buffer
     */
    @Contract("_, _, _, _, _ -> new")
    NetworkBuffer copy(Factory factory, long index, long length, long readIndex, long writeIndex);

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
     * The registries used when creating with {@link Factory#registry(Registries)}
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
    IOView ioView();

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
         *
         * @param value      the value to get the size of
         * @param registries the registries
         * @return the size
         */
        @Contract(pure = true)
        @Range(from = 0, to = Long.MAX_VALUE)
        default long sizeOf(T value, Registries registries) {
            Objects.requireNonNull(registries, "registries");
            return NetworkBufferTypeImpl.sizeOf(this, value, registries);
        }

        /**
         * Determines the sizeOf {@link T}.
         *
         * @param value the value to get the size of
         * @return the size
         */
        @Contract(pure = true)
        @Range(from = 0, to = Long.MAX_VALUE)
        default long sizeOf(T value) {
            return NetworkBufferTypeImpl.sizeOf(this, value, null);
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
        default <S extends @UnknownNullability Object> Type<S> transform(Function<T, S> to, Function<S, T> from) {
            return new NetworkBufferTypeImpl.TransformType<>(this, to, from);
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
         * For example a not null {@link T} would be true, and {@code null} would be false.
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
        default <R> Type<R> unionType(Function<T, NetworkBuffer.Type<? extends R>> serializers, Function<R, ? extends T> keyFunc) {
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
     * Factory for creating a {@link NetworkBuffer} through {@link Factory#staticFactory()}
     * or {@link Factory#resizeableFactory()}.
     * <br>
     * Useful for creating buffers with specific configuration like arenas, auto resizing, and registries.
     * <br>
     * Factories are immutable and can be used across threads if the {@link Arena} supports it.
     * You also shouldn't rely on the identity of them due to being a value class candidate.
     * <br>
     * For example, using a confined arena for a manged lifetime.
     * <pre>{@code
     * try (Arena arena = Arena.ofConfined()) {
     *      var factory = NetworkBuffer.Factory.staticFactory().arena(arena);
     *      NetworkBuffer buffer = factory.allocate(1024);
     *      // Do things with the buffer
     * }}</pre>
     */
    sealed interface Factory permits NetworkBufferImpl.FactoryImpl {
        /**
         * Gets the static factory where {@link #arena(Supplier)} is set.
         *
         * @return the static factory.
         */
        @Contract(pure = true)
        static Factory staticFactory() {
            return NetworkBufferImpl.FactoryImpl.STATIC;
        }

        /**
         * Gets the resizeable factory where {@link #autoResize(AutoResize)} is set and built off {@link #staticFactory()}
         * using the {@link #autoResize(AutoResize)} of {@link AutoResize#DOUBLE}.
         *
         * @return the resizeable factory.
         */
        @Contract(pure = true)
        static Factory resizeableFactory() {
            return NetworkBufferImpl.FactoryImpl.RESIZEABLE;
        }

        /**
         * Sets the arena used for allocations.
         * <br>
         * Otherwise, if left unset, the default arena will be used.
         *
         * @param arena the arena
         * @return the new factory
         */
        @ApiStatus.Experimental
        @Contract(pure = true, value = "_ -> new")
        Factory arena(Arena arena);

        /**
         * Sets the new arena strategy.
         * Called when we want to reallocate memory to a fresh arena, for example, during copy or initialization.
         * <br>
         * Note you should use {@link #arena(Arena)} if you use a singleton instance.
         * <br>
         * Otherwise, if left unset, the default arena will be used.
         *
         * @param arenaSupplier the supplier
         * @return the new factory
         */
        @ApiStatus.Experimental
        @Contract(pure = true, value = "_ -> new")
        Factory arena(Supplier<Arena> arenaSupplier);

        /**
         * Sets the auto-resizing strategy.
         * <br>
         * Otherwise, if left unset, the buffer will never be resized and is considered a static buffer
         * unless it's a {@link #resizeableFactory()}.
         *
         * @param autoResize the {@link AutoResize} strategy
         * @return the new factory
         */
        @Contract(pure = true, value = "_ -> new")
        Factory autoResize(AutoResize autoResize);

        /**
         * Sets a registry for buffers to use.
         *
         * @param registries the registry
         * @return the new factory
         */
        @Contract(pure = true, value = "_ -> new")
        Factory registry(Registries registries);

        /**
         * Builds a new network buffer from this factory with {@code length} allocated.
         *
         * @param length the size of the buffer, or initial size if {@link AutoResize} is set.
         * @return the new network buffer
         */
        @Contract("_ -> new")
        NetworkBuffer allocate(long length);
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
    sealed interface IOView extends DataInput, DataOutput permits NetworkBufferIOViewImpl {

        /**
         * @throws UnsupportedOperationException not implemented.
         */
        @Override
        @Deprecated
        @Contract("-> fail")
        String readLine();

        // Override DataInput methods to remove checked exceptions
        @Override
        void readFully(byte[] b);

        @Override
        void readFully(byte[] b, int off, int len);

        @Override
        int skipBytes(int n);

        @Override
        boolean readBoolean();

        @Override
        byte readByte();

        @Override
        int readUnsignedByte();

        @Override
        short readShort();

        @Override
        int readUnsignedShort();

        @Override
        char readChar();

        @Override
        int readInt();

        @Override
        long readLong();

        @Override
        float readFloat();

        @Override
        double readDouble();

        @Override
        String readUTF();

        // Override DataOutput methods to remove checked exceptions
        @Override
        void write(int b);

        @Override
        void write(byte[] b);

        @Override
        void write(byte[] b, int off, int len);

        @Override
        void writeBoolean(boolean v);

        @Override
        void writeByte(int v);

        @Override
        void writeShort(int v);

        @Override
        void writeChar(int v);

        @Override
        void writeInt(int v);

        @Override
        void writeLong(long v);

        @Override
        void writeFloat(float v);

        @Override
        void writeDouble(double v);

        @Override
        void writeBytes(String s);

        @Override
        void writeChars(String s);

        @Override
        void writeUTF(String s);
    }
}
