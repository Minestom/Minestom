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
 *     System.out.println("Value: " + value); // Output: Value: MyData
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
    NetworkBuffer trim();

    @ApiStatus.Experimental
    @Contract(pure = true)
    NetworkBuffer trim(Arena arena);

    @Contract(pure = true)
    default NetworkBuffer copy(long index, long length) {
        return copy(index, length, readIndex(), writeIndex());
    }

    @ApiStatus.Experimental
    @Contract(pure = true)
    default NetworkBuffer copy(Arena arena, long index, long length) {
        return copy(arena, index, length, readIndex(), writeIndex());
    }

    @Contract(pure = true)
    default NetworkBuffer copy(long index, long length, long readIndex, long writeIndex){
        return copy(NetworkBufferImpl.defaultArena(), index, length, readIndex, writeIndex);
    }

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

        default long sizeOf(T value, @Nullable Registries registries) {
            return NetworkBufferTypeImpl.sizeOf(this, value, registries);
        }

        default long sizeOf(T value) {
            return sizeOf(value, null);
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

    static Builder builder(long size) {
        return new NetworkBufferImpl.Builder(size);
    }

    static NetworkBuffer staticBuffer(long size, @Nullable Registries registries) {
        return builder(size).registry(registries).build();
    }

    static NetworkBuffer staticBuffer(long size) {
        return staticBuffer(size, null);
    }

    static NetworkBuffer resizableBuffer(long initialSize, @Nullable Registries registries) {
        return builder(initialSize)
                .autoResize(AutoResize.DOUBLE)
                .registry(registries)
                .build();
    }

    static NetworkBuffer resizableBuffer(int initialSize) {
        return resizableBuffer(initialSize, null);
    }

    static NetworkBuffer resizableBuffer(@Nullable Registries registries) {
        return resizableBuffer(256, registries);
    }

    static NetworkBuffer resizableBuffer() {
        return resizableBuffer(null);
    }


    @ApiStatus.Experimental
    static NetworkBuffer wrap(MemorySegment segment, int readIndex, int writeIndex, @Nullable Registries registries) {
        return NetworkBufferImpl.wrap(segment, readIndex, writeIndex, registries);
    }

    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, @Nullable Registries registries) {
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
    }

    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return wrap(bytes, readIndex, writeIndex, null);
    }

    /**
     * Builder for creating a {@link NetworkBuffer} through {@link NetworkBuffer#builder(long)}.
     * <br>
     *  Useful for creating buffers with specific configurations, arenas, and registries.
     */
    sealed interface Builder permits NetworkBufferImpl.Builder {
        @ApiStatus.Experimental
        @Contract(pure = true)
        Builder arena(Arena arena);

        @Contract(pure = true)
        Builder autoResize(@Nullable AutoResize autoResize);

        @Contract(pure = true)
        Builder registry(@Nullable Registries registries);

        @Contract("-> new")
        NetworkBuffer build();
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

    static byte[] makeArray(Consumer<NetworkBuffer> writing, @Nullable Registries registries) {
        NetworkBuffer buffer = resizableBuffer(256, registries);
        writing.accept(buffer);
        return buffer.read(RAW_BYTES);
    }

    static byte[] makeArray(Consumer<NetworkBuffer> writing) {
        return makeArray(writing, null);
    }

    static <T> byte[] makeArray(Type<T> type, T value, @Nullable Registries registries) {
        return makeArray(buffer -> buffer.write(type, value), registries);
    }

    static <T> byte[] makeArray(Type<T> type, T value) {
        return makeArray(type, value, null);
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        NetworkBufferImpl.copy(srcBuffer, srcOffset, dstBuffer, dstOffset, length);
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
        String readLine() throws IOException;
    }
}
