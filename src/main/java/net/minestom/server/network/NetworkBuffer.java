package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.*;

import javax.crypto.Cipher;
import java.io.*;
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
 * A {@link NetworkBuffer} is a mutable byte buffer that can be used to read and write various types of data.
 * <p>
 * It provides a set of predefined types for reading and writing data, as well as methods for resizing, copying, and slicing the buffer.
 * <p>
 * The buffer supports both reading and writing operations, with separate indices for read and write operations.
 * <p>
 * This interface is designed to be used in junction with the protocol directly, but can also be used for other purposes.
 * <p>
 * We provide basic {@link NetworkBuffer.Type} here, which can be used to read and write data in a bidirectional way.
 * For example, you can write an integer to the buffer and then read it back:
 * <pre><code>
 * NetworkBuffer buffer = NetworkBuffer.staticBuffer(1024);
 * buffer.write(NetworkBuffer.INT, 42);
 * int value = buffer.read(NetworkBuffer.INT);
 * System.out.println("Value: " + value); // Output: Value: 42
 * </code></pre>
 * Or make an array of bytes with {@link NetworkBuffer#makeArray(Type, Object)} by a {@link NetworkBuffer.Type}:
 * <pre><code>
 *     byte[] bytes = NetworkBuffer.makeArray(NetworkBuffer.STRING, "Hello, World!");
 *     System.out.println("Bytes: " + Arrays.toString(bytes));
 * </code></pre>
 * Or use a {@link NetworkBufferTemplate} to function as a bidirectional serializer/deserializer for your objects:
 * <pre><code>
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
 *     System.out.println("Value: " + value); // Value: MyData(id=1, name="Test")
 * </code></pre>
 */
public sealed interface NetworkBuffer permits NetworkBufferImpl {
    Type<Unit> UNIT = new NetworkBufferTypeImpl.UnitType();
    Type<Boolean> BOOLEAN = new NetworkBufferTypeImpl.BooleanType();
    Type<Byte> BYTE = new NetworkBufferTypeImpl.ByteType();
    Type<Short> SHORT = new NetworkBufferTypeImpl.ShortType();
    Type<Integer> UNSIGNED_SHORT = new NetworkBufferTypeImpl.UnsignedShortType();
    Type<Integer> INT = new NetworkBufferTypeImpl.IntType();
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
    Type<String> STRING_IO_UTF8 = new NetworkBufferTypeImpl.IOUTF8StringType();
    Type<BinaryTag> NBT = new NetworkBufferTypeImpl.NbtType<>();
    Type<CompoundBinaryTag> NBT_COMPOUND = new NetworkBufferTypeImpl.NbtType<>();
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
    Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();

    Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();
    Type<@Nullable UUID> OPT_UUID = UUID.optional();

    Type<Direction> DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    static <E extends Enum<E>> Type<E> Enum(Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    static <E extends Enum<E>> Type<EnumSet<E>> EnumSet(Class<E> enumClass) {
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, enumClass.getEnumConstants());
    }

    static Type<BitSet> FixedBitSet(int length) {
        return new NetworkBufferTypeImpl.FixedBitSetType(length);
    }

    static Type<byte[]> FixedRawBytes(int length) {
        return new NetworkBufferTypeImpl.RawBytesType(length);
    }

    static <T> Type<T> Lazy(Supplier<Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }

    static <T> Type<T> TypedNBT(Codec<T> serializer) {
        return new NetworkBufferTypeImpl.TypedNbtType<>(serializer);
    }

    static <L, R> Type<Either<L, R>> Either(NetworkBuffer.Type<L> left, NetworkBuffer.Type<R> right) {
        return new NetworkBufferTypeImpl.EitherType<>(left, right);
    }

    <T> void write(Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T read(Type<T> type) throws IndexOutOfBoundsException;

    <T> void writeAt(long index, Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T readAt(long index, Type<T> type) throws IndexOutOfBoundsException;

    @Deprecated(forRemoval = true) // No longer long's
    default void copyTo(long srcOffset, byte[] dest, long destOffset, long length) {
        this.copyTo(srcOffset, dest, Math.toIntExact(destOffset), Math.toIntExact(length));
    }

    void copyTo(long srcOffset, byte[] dest, int destOffset, int length);

    byte[] extractBytes(Consumer<NetworkBuffer> extractor);

    NetworkBuffer clear();

    long writeIndex();

    long readIndex();

    NetworkBuffer writeIndex(long writeIndex);

    NetworkBuffer readIndex(long readIndex);

    NetworkBuffer index(long readIndex, long writeIndex);

    long advanceWrite(long length);

    long advanceRead(long length);

    long readableBytes();

    long writableBytes();

    long capacity();

    void readOnly();

    boolean isReadOnly();

    void resize(long newSize);

    void ensureWritable(long length);

    /**
     * Compact (copies) all the data from the readIndex to the writing index to be zero aligned.
     * This does not change the buffer capacity.
     */
    void compact();

    @Contract(pure = true)
    default NetworkBuffer trim() {
        return trim(staticBuilder());
    }

    @Contract(pure = true)
    NetworkBuffer trim(Settings builder);

    @ApiStatus.Experimental
    @Contract(pure = true)
    NetworkBuffer trim(Arena arena);

    @Contract(pure = true)
    default NetworkBuffer copy(long index, long length) {
        return copy(index, length, readIndex(), writeIndex());
    }

    @Contract(pure = true)
    default NetworkBuffer copy(Settings builder, long index, long length) {
        return copy(builder, index, length, readIndex(), writeIndex());
    }

    @Contract(pure = true)
    default NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        return copy(staticBuilder(), index, length, readIndex, writeIndex);
    }

    @Contract(pure = true, value = "_, _, _, _, _ -> new")
    NetworkBuffer copy(Settings builder, long index, long length, long readIndex, long writeIndex);

    @ApiStatus.Experimental
    @Contract(pure = true, value = "_, _, _, _, _ -> new")
    NetworkBuffer copy(Arena arena, long index, long length, long readIndex, long writeIndex);

    @Contract(pure = true)
    NetworkBuffer slice(long index, long length, long readIndex, long writeIndex);

    @Contract(pure = true)
    default NetworkBuffer slice(long index, long length) {
        return slice(index, length, readIndex(), writeIndex());
    }

    int readChannel(ReadableByteChannel channel) throws IOException;

    boolean writeChannel(WritableByteChannel channel) throws IOException;

    void cipher(Cipher cipher, long start, long length);

    long compress(long start, long length, NetworkBuffer output);

    long decompress(long start, long length, NetworkBuffer output) throws DataFormatException;

    @Nullable Registries registries();

    /**
     * Creates a new {@link IOView} of this buffer.
     * <br>
     * Useful to interface with API's that support {@link DataInput} or {@link DataOutput}.
     * @return the io view.
     */
    @ApiStatus.Experimental
    @Contract(pure = true, value = "->new")
    IOView ioView();

    interface Type<T extends @UnknownNullability Object> {
        void write(NetworkBuffer buffer, T value);

        T read(NetworkBuffer buffer);

        default long sizeOf(T value, Registries registries) {
            Objects.requireNonNull(registries, "registries");
            return NetworkBufferTypeImpl.sizeOf(this, value, registries);
        }

        default long sizeOf(T value) {
            return NetworkBufferTypeImpl.sizeOf(this, value, null);
        }

        default <S> Type<S> transform(Function<T, S> to, Function<S, T> from) {
            return new NetworkBufferTypeImpl.TransformType<>(this, to, from);
        }

        default <V> Type<Map<T, V>> mapValue(Type<V> valueType, int maxSize) {
            return new NetworkBufferTypeImpl.MapType<>(this, valueType, maxSize);
        }

        default <V> Type<Map<T, V>> mapValue(Type<V> valueType) {
            return mapValue(valueType, Integer.MAX_VALUE);
        }

        default Type<List<T>> list(int maxSize) {
            return new NetworkBufferTypeImpl.ListType<>(this, maxSize);
        }

        default Type<List<T>> list() {
            return list(Integer.MAX_VALUE);
        }

        default Type<Set<T>> set(int maxSize) {
            return new NetworkBufferTypeImpl.SetType<>(this, maxSize);
        }

        default Type<Set<T>> set() {
            return set(Integer.MAX_VALUE);
        }

        default Type<@Nullable T> optional() {
            return new NetworkBufferTypeImpl.OptionalType<>(this);
        }

        default <R, TR extends R> Type<R> unionType(Function<T, NetworkBuffer.Type<TR>> serializers, Function<R, ? extends T> keyFunc) {
            return new NetworkBufferTypeImpl.UnionType<>(this, keyFunc, serializers);
        }

        default Type<T> lengthPrefixed(int maxLength) {
            return new NetworkBufferTypeImpl.LengthPrefixedType<>(this, maxLength);
        }
    }

    @Contract(pure = true)
    static Settings staticBuilder() {
        return NetworkBufferImpl.Settings.STATIC;
    }

    @Contract(pure = true)
    static Settings resizeableBuilder() {
        return NetworkBufferImpl.Settings.RESIZEABLE;
    }

    static NetworkBuffer staticBuffer(long size, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return staticBuilder().registry(registries).build(size);
    }

    static NetworkBuffer staticBuffer(long size) {
        return staticBuilder().build(size);
    }

    static NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return resizeableBuilder()
                .registry(registries)
                .build(initialSize);
    }

    static NetworkBuffer resizableBuffer(int initialSize) {
        return resizeableBuilder().build(initialSize);
    }

    static NetworkBuffer resizableBuffer(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return resizableBuffer(256, registries);
    }

    static NetworkBuffer resizableBuffer() {
        return resizableBuffer(256);
    }
    /**
     * Wrap the byte array into a {@link NetworkBuffer} with the registries.
     * Useful when you already have a memory segment.
     * @param segment the segment
     * @param readIndex the {@link #readIndex()}
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
     * Wrap the byte array into a {@link NetworkBuffer} with the registries.
     * Useful when you already have a memory segment.
     * @param segment the segment
     * @param readIndex the {@link #readIndex()}
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
     * @param bytes the bytes
     * @param readIndex the {@link #readIndex()}
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
     * @param bytes the bytes
     * @param readIndex the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @return the new {@link NetworkBuffer}
     */
    @Contract("_, _, _ -> new")
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        Objects.requireNonNull(bytes, "bytes");
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex);
    }

    /**
     * Builder for creating a {@link NetworkBuffer} through {@link NetworkBuffer#staticBuilder()}.
     * <br>
     * Useful for creating buffers with specific configuration like arenas, auto resizing, and registries.
     * <br>
     * Builders are immutable and can be used across threads. You also shouldn't rely on the identity of builders due to being a value class candidate.
     */
    sealed interface Settings permits NetworkBufferImpl.Settings {
        /**
         * Sets the arena used for allocations.
         * <br>
         * Otherwise if left unset the default arena will be used.
         * @param arena the arena
         * @return the new settings
         */
        @ApiStatus.Experimental
        @Contract(pure = true, value = "_ -> new")
        Settings arena(Arena arena);

        /**
         * Sets the new arena strategy. Called when we want to reallocate memory to a fresh arena, for example during copy or initialization.
         * <br>
         * Note you should use {@link #arena(Arena)} if you use a singleton instance.
         * <br>
         * Otherwise if left unset the default arena will be used.
         * @param arenaSupplier the supplier
         * @return the new settings
         */
        @ApiStatus.Experimental
        @Contract(pure = true,  value = "_ -> new")
        Settings arena(Supplier<Arena> arenaSupplier);

        /**
         * Sets the auto resizing strategy.
         * <br>
         * Otherwise if left unset the buffer will never be resized and is considered a static buffer.
         * @param autoResize the {@link AutoResize} strategy
         * @return the new settings
         */
        @Contract(pure = true, value = "_ -> new")
        Settings autoResize(AutoResize autoResize);

        /**
         * Sets a registry for buffers to use.
         * @param registries the registry
         * @return the new settings
         */
        @Contract(pure = true, value = "_ -> new")
        Settings registry(Registries registries);

        /**
         * Builds a new network buffer from these settings with {@code initialSize} allocated.
         * @param initialSize the initial size of the buffer, or size if {@link AutoResize} is unset.
         * @return the new network buffer
         */
        @Contract("_ -> new")
        NetworkBuffer build(long initialSize);
    }

    /**
     * Resize strategy for a {@link NetworkBuffer}.
     */
    @FunctionalInterface
    interface AutoResize {
        AutoResize DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        /**
         * Provide the buffer a new size, guaranteeing that the new size is greater than its original.
         * @param capacity the current capacity of the buffer
         * @param targetSize the target size of the buffer
         * @return the new capacity of the buffer
         */
        long resize(long capacity, long targetSize);
    }

    /**
     * Creates a byte array from the consumer and with registries.
     * @param writing consumer of the {@link NetworkBuffer}
     * @param registries the registries to use in serialization
     * @return the smallest byte array to represent the contents of {@link NetworkBuffer}
     */
    static byte[] makeArray(Consumer<NetworkBuffer> writing, Registries registries) {
        NetworkBuffer buffer = resizableBuffer(256, registries);
        writing.accept(buffer);
        return buffer.read(RAW_BYTES);
    }

    /**
     * Creates a byte array from the consumer and without registries.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     * @param writing consumer of the {@link NetworkBuffer}
     * @return the smallest byte array to represent the contents of {@link NetworkBuffer}
     */
    static byte[] makeArray(Consumer<NetworkBuffer> writing) {
        NetworkBuffer buffer = resizableBuffer(256);
        writing.accept(buffer);
        return buffer.read(RAW_BYTES);
    }

    /**
     * Creates a byte array from the type and value registries.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     * @param type the {@link Type} for {@link T}
     * @param value the value
     * @param registries the registries to use in serialization
     * @return the smallest byte array to represent {@link T}
     * @param <T> the type
     */
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value, Registries registries) {
        Objects.requireNonNull(type, "type");
        // value is nullable when T is optional.
        Objects.requireNonNull(registries, "registries");
        return makeArray(buffer -> buffer.write(type, value), registries); // TODO can optimize fixed values here.
    }

    /**
     * Creates a byte array from the type and value without registries.
     * Similar to {@link NetworkBuffer#makeArray(Consumer, Registries)}
     * @param type the {@link Type} for {@link T}
     * @param value the value
     * @return the smallest byte array to represent {@link T}
     * @param <T> the type
     */
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value) {
        Objects.requireNonNull(type, "type");
        // value is nullable when T is optional.
        return makeArray(buffer -> buffer.write(type, value)); // TODO can optimize fixed values here.
    }

    /**
     * Copies the src {@link NetworkBuffer} into the destination {@link NetworkBuffer}
     * <br>
     * @param srcBuffer the source
     * @param srcOffset the source offset
     * @param dstBuffer the destination
     * @param dstOffset the destination offset
     * @param length the length to copy
     * @throws UnsupportedOperationException if {@code srcBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is a dummy
     * @throws UnsupportedOperationException if {@code dstBuffer} is read only
     */
    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        Objects.requireNonNull(srcBuffer, "srcBuffer");
        Objects.requireNonNull(dstBuffer, "dstBuffer");
        NetworkBufferImpl.copy(srcBuffer, srcOffset, dstBuffer, dstOffset, length);
    }

    /**
     * Fill the buffer with the byte value specified.
     * <br>
     * Useful if you want to zero a buffer after use if required.
     * @param buffer the buffer
     * @param offset the offset
     * @param value the value to fill
     * @param length the length
     * @throws UnsupportedOperationException if {@code buffer} is a dummy
     * @throws UnsupportedOperationException if {@code buffer} is a read only
     */
    static void fill(NetworkBuffer buffer, long offset, byte value, long length) {
        Objects.requireNonNull(buffer, "buffer");
        NetworkBufferImpl.fill(buffer, offset, value, length);
    }

    /**
     * @deprecated Use contentEquals instead.
     * @param buffer1 the buffer
     * @param buffer2 the buffer
     * @return if they are equals
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
    static boolean contentEquals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        Objects.requireNonNull(buffer1, "buffer1");
        Objects.requireNonNull(buffer2, "buffer2");
        return NetworkBufferImpl.contentEquals(buffer1, buffer2);
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
     * You should never rely on the identity properties of {@link IOView} as it is a value class candidate.
     */
    sealed interface IOView extends DataInput, DataOutput permits NetworkBufferIOViewImpl {
        /**
         * @throws UnsupportedOperationException not implemented.
         */
        @Override
        @Deprecated
        String readLine();
    }
}
