package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import javax.crypto.Cipher;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.PublicKey;
import java.time.Instant;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * A mutable byte buffer that reads and writes network protocol data through {@link Type}s.
 * <p>
 * Buffers keep separate read and write indexes. They come in two flavors:
 * <ul>
 *   <li><b>Static buffers</b> have no resize strategy, so writes past their capacity fail. They are created with {@link #staticBuffer(long)}.</li>
 *   <li><b>Resizable buffers</b> grow when a write needs more room and are created with {@link #resizableBuffer()}.</li>
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
 * record MyData(int id, String name) {
 *     static final NetworkBuffer.Type<MyData> SERIALIZER = NetworkBufferTemplate.template(
 *             NetworkBuffer.INT, MyData::id,
 *             NetworkBuffer.STRING, MyData::name,
 *             MyData::new);
 * }
 *
 * MyData data = new MyData(1, "Test");
 * byte[] bytes = NetworkBuffer.makeArray(MyData.SERIALIZER, data);
 * NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
 * MyData value = buffer.read(MyData.SERIALIZER); // MyData[id=1, name=Test]
 * }</pre>
 * <p>
 * Buffers are not thread safe, because they track their read and write indexes.
 *
 * @see Type for custom types
 * @see NetworkBufferTemplate for templating
 */
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
    Type<short[]> RAW_SHORTS = new NetworkBufferTypeImpl.RawShortsType(-1);
    Type<int[]> RAW_INTS = new NetworkBufferTypeImpl.RawIntsType(-1);
    Type<long[]> RAW_LONGS = new NetworkBufferTypeImpl.RawLongsType(-1);
    Type<float[]> RAW_FLOATS = new NetworkBufferTypeImpl.RawFloatsType(-1);
    Type<double[]> RAW_DOUBLES = new NetworkBufferTypeImpl.RawDoublesType(-1);
    Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    Type<Key> KEY = STRING.transform(Key::key, Key::asString);
    Type<String> STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    Type<String> STRING_IO_UTF8 = new NetworkBufferTypeImpl.IOUTF8StringType();
    Type<BinaryTag> NBT = BinaryTagTypeImpl.INSTANCE;
    Type<CompoundBinaryTag> NBT_COMPOUND = BinaryTagTypeImpl.INSTANCE_COMPOUND;
    Type<BinaryTag> UNTRUSTED_NBT = BinaryTagTypeImpl.UNTRUSTED_INSTANCE;
    Type<CompoundBinaryTag> UNTRUSTED_NBT_COMPOUND = BinaryTagTypeImpl.UNTRUSTED_INSTANCE_COMPOUND;
    Type<Point> BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    Type<Component> COMPONENT = new ComponentNetworkBufferTypeImpl();
    Type<Component> JSON_COMPONENT = new NetworkBufferTypeImpl.JsonComponentType();
    Type<java.util.UUID> UUID = new NetworkBufferTypeImpl.UUIDType();
    Type<Pos> POS = new NetworkBufferTypeImpl.PosType();

    Type<byte[]> BYTE_ARRAY = new NetworkBufferTypeImpl.ByteArrayType();
    Type<long[]> LONG_ARRAY = new NetworkBufferTypeImpl.LongArrayType();
    Type<int[]> VAR_INT_ARRAY = new NetworkBufferTypeImpl.VarIntArrayType();
    Type<long[]> VAR_LONG_ARRAY = new NetworkBufferTypeImpl.VarLongArrayType();

    Type<BitSet> BITSET = BYTE_ARRAY.transform(BitSet::valueOf, BitSet::toByteArray);
    Type<Instant> INSTANT_MS = LONG.transform(Instant::ofEpochMilli, Instant::toEpochMilli);
    Type<PublicKey> PUBLIC_KEY = BYTE_ARRAY.transform(KeyUtils::publicRSAKeyFrom, PublicKey::getEncoded);

    Type<Point> VECTOR3 = new NetworkBufferTypeImpl.Vector3Type();
    Type<Point> VECTOR3D = new NetworkBufferTypeImpl.Vector3DType();
    Type<Point> VECTOR3I = new NetworkBufferTypeImpl.Vector3IType();
    Type<Point> VECTOR3B = new NetworkBufferTypeImpl.Vector3BType();
    Type<Vec> LP_VECTOR3 = new NetworkBufferTypeImpl.LpVector3Type();
    Type<float[]> QUATERNION = FixedRawFloats(4);

    Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();

    Type<Direction> DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    /**
     * Creates a type for the constants of an enum, encoded as a {@link #VAR_INT} of the ordinal.
     * Reading an ordinal with no matching constant throws {@link IndexOutOfBoundsException}.
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the enum type
     */
    static <E extends Enum<E>> Type<E> Enum(Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    /**
     * Creates a type for a set of enum constants, encoded as a {@link #FixedBitSet(int)} with one bit per constant
     * in ordinal order.
     *
     * @param enumClass the enum class
     * @param <E>       the enum type
     * @return the enum set type
     */
    static <E extends Enum<E>> Type<EnumSet<E>> EnumSet(Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, values);
    }

    /**
     * Creates a type for a bit set of exactly {@code length} bits, stored in {@code (length + 7) / 8} bytes.
     *
     * @param length the bit count, which must not be negative
     * @return the fixed length bit set type
     */
    static Type<BitSet> FixedBitSet(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.FixedBitSetType(length);
    }

    /**
     * Creates a type for exactly {@code length} bytes, unlike {@link #RAW_BYTES} which reads every byte left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length byte type
     */
    static Type<byte[]> FixedRawBytes(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawBytesType(length);
    }

    /**
     * Creates a type for exactly {@code length} shorts, unlike {@link #RAW_SHORTS} which reads every short left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length short type
     */
    static Type<short[]> FixedRawShorts(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawShortsType(length);
    }

    /**
     * Creates a type for exactly {@code length} ints, unlike {@link #RAW_INTS} which reads every int left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length int type
     */
    static Type<int[]> FixedRawInts(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawIntsType(length);
    }

    /**
     * Creates a type for exactly {@code length} longs, unlike {@link #RAW_LONGS} which reads every long left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length long type
     */
    static Type<long[]> FixedRawLongs(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawLongsType(length);
    }

    /**
     * Creates a type for exactly {@code length} floats, unlike {@link #RAW_FLOATS} which reads every float left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length float type
     */
    static Type<float[]> FixedRawFloats(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawFloatsType(length);
    }

    /**
     * Creates a type for exactly {@code length} doubles, unlike {@link #RAW_DOUBLES} which reads every double left in the buffer.
     *
     * @param length the element count, which must not be negative
     * @return the fixed length double type
     */
    static Type<double[]> FixedRawDoubles(int length) {
        Check.argCondition(length < 0, "Length cannot be negative: {0}", length);
        return new NetworkBufferTypeImpl.RawDoublesType(length);
    }

    /**
     * Creates a type that gets its implementation from {@code supplier} on first use.
     * The supplier should be thread safe, because first uses from several threads at once can each call it.
     *
     * @param supplier the supplier of the type
     * @param <T>      the value type
     * @return the lazy type
     */
    static <T extends @UnknownNullability Object> Type<T> Lazy(Supplier<Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }

    /**
     * Creates a type that encodes values with {@code serializer} as {@link #NBT}, and reads them with {@link #UNTRUSTED_NBT}.
     * Reading and writing throw {@link IllegalStateException} when the buffer has no {@link #registries()},
     * and {@link IllegalArgumentException} when the codec fails.
     *
     * @param serializer the codec for the values
     * @param <T>        the value type
     * @return the NBT type
     */
    static <T extends @UnknownNullability Object> Type<T> TypedNBT(Codec<T> serializer) {
        return new NetworkBufferTypeImpl.TypedNbtType<>(serializer);
    }

    /**
     * Creates a type for an {@link Either}, encoded as a {@link #BOOLEAN} that is {@code true} for a left value,
     * followed by the value.
     *
     * @param left  the type of left values
     * @param right the type of right values
     * @param <L>   the left type
     * @param <R>   the right type
     * @return the either type
     */
    static <L, R> Type<Either<L, R>> Either(NetworkBuffer.Type<L> left, NetworkBuffer.Type<R> right) {
        return new NetworkBufferTypeImpl.EitherType<>(left, right);
    }

    /**
     * Creates a type that refers to itself, such as a node that contains other nodes.
     * {@code func} is called once, right away, with a type that delegates to the type it returns.
     * That reference must not read or write before {@code func} returns.
     *
     * @param func the function that builds the type from a reference to itself
     * @param <T>  the value type
     * @return the type returned by {@code func}
     */
    static <T extends @UnknownNullability Object> Type<T> Recursive(Function<? super Type<T>, ? extends Type<T>> func) {
        return new NetworkBufferTypeImpl.RecursiveType<>(func).delegate;
    }

    /**
     * Creates a type for the subtypes of {@code T}, encoded as the discriminator of each value
     * followed by the value in the serializer mapped to that discriminator.
     * The fallback serializer handles discriminators missing from {@code serializerMap}. Without a fallback,
     * reading and writing throw {@link UnsupportedOperationException} for a missing discriminator.
     *
     * @param discriminator          the type of the discriminator
     * @param discriminatorFromValue the discriminator of a value
     * @param serializerMap          the serializer for each discriminator, which this method copies
     * @param fallback               the serializer for other discriminators, or {@code null} for none
     * @param <T>                    the value type
     * @param <D>                    the discriminator type
     * @return the tagged type
     * @throws NullPointerException if {@code serializerMap} contains a null key or value
     */
    static <T extends @UnknownNullability Object, D> Type<T> Tagged(Type<D> discriminator, Function<? super T, ? extends D> discriminatorFromValue,
                                                                    Map<? super D, ? extends Type<? extends T>> serializerMap, @Nullable Type<? extends T> fallback) {
        // Map.copyOf does some trickery with the generic bounds here.
        return new NetworkBufferTypeImpl.TaggedType<>(discriminator, discriminatorFromValue, Map.copyOf(serializerMap), fallback);
    }

    /**
     * Creates a tagged type with no fallback, so reading and writing throw {@link UnsupportedOperationException}
     * for a discriminator missing from {@code serializerMap}.
     *
     * @param discriminator          the type of the discriminator
     * @param discriminatorFromValue the discriminator of a value
     * @param serializerMap          the serializer for each discriminator, which this method copies
     * @param <T>                    the value type
     * @param <D>                    the discriminator type
     * @return the tagged type
     * @throws NullPointerException if {@code serializerMap} contains a null key or value
     * @see #Tagged(Type, Function, Map, Type)
     */
    static <T extends @UnknownNullability Object, D> Type<T> Tagged(Type<D> discriminator, Function<? super T, ? extends D> discriminatorFromValue,
                                                                    Map<? super D, ? extends Type<? extends T>> serializerMap) {
        return Tagged(discriminator, discriminatorFromValue, serializerMap, null);
    }

    /**
     * Writes a value at the {@link #writeIndex()} and advances it past the value.
     * The buffer is resized first when it needs more room and has a resize strategy.
     *
     * @param type  the type of the value
     * @param value the value to write
     * @param <T>   the value type
     * @throws IndexOutOfBoundsException if the value does not fit and the buffer cannot grow
     * @throws IllegalArgumentException  if this buffer is read-only
     */
    <T extends @UnknownNullability Object> void write(Type<T> type, T value) throws IndexOutOfBoundsException;

    /**
     * Reads a value at the {@link #readIndex()} and advances it past the value.
     *
     * @param type the type of the value
     * @param <T>  the value type
     * @return the value
     * @throws IndexOutOfBoundsException if the value extends past the {@link #capacity()}
     * @throws IllegalArgumentException  if this buffer is a dummy
     */
    <T extends @UnknownNullability Object> T read(Type<T> type) throws IndexOutOfBoundsException;

    /**
     * Writes a value at {@code index}, leaving the {@link #writeIndex()} unchanged.
     *
     * @param index the index to write at
     * @param type  the type of the value
     * @param value the value to write
     * @param <T>   the value type
     * @throws IndexOutOfBoundsException if the value does not fit and the buffer cannot grow
     * @throws IllegalArgumentException  if this buffer is read-only
     */
    <T extends @UnknownNullability Object> void writeAt(long index, Type<T> type, T value) throws IndexOutOfBoundsException;

    /**
     * Reads a value at {@code index}, leaving the {@link #readIndex()} unchanged.
     *
     * @param index the index to read at
     * @param type  the type of the value
     * @param <T>   the value type
     * @return the value
     * @throws IndexOutOfBoundsException if the value extends past the {@link #capacity()}
     * @throws IllegalArgumentException  if this buffer is a dummy
     */
    <T extends @UnknownNullability Object> T readAt(long index, Type<T> type) throws IndexOutOfBoundsException;

    /**
     * Copies {@code length} bytes starting at {@code srcOffset} into {@code dest}, ignoring the read and write indexes.
     *
     * @param srcOffset  the index of the first byte to copy
     * @param dest       the destination array
     * @param destOffset the index in {@code dest} to copy to
     * @param length     the number of bytes to copy
     * @throws IndexOutOfBoundsException if either range is out of bounds
     * @throws IllegalArgumentException  if this buffer is a dummy
     */
    void copyTo(long srcOffset, byte[] dest, int destOffset, int length);

    /**
     * Copies {@code length} bytes starting at {@code srcOffset} into {@code dest}, ignoring the read and write indexes.
     *
     * @param srcOffset  the index of the first byte to copy
     * @param dest       the destination segment
     * @param destOffset the index in {@code dest} to copy to
     * @param length     the number of bytes to copy
     * @throws IndexOutOfBoundsException if either range is out of bounds
     * @throws IllegalArgumentException  if this buffer is a dummy, or {@code dest} is read-only
     */
    void copyTo(long srcOffset, MemorySegment dest, long destOffset, long length);

    /**
     * Runs {@code extractor} on this buffer and returns a copy of the bytes it read,
     * from the {@link #readIndex()} before the call to the one after it.
     *
     * @param extractor the reads to capture
     * @return the bytes read by {@code extractor}
     * @throws IllegalArgumentException if this buffer is a dummy
     */
    byte[] extractBytes(Consumer<? super NetworkBuffer> extractor);

    /**
     * Sets the read and write indexes to 0. The stored bytes are not zeroed.
     *
     * @return this buffer
     */
    NetworkBuffer clear();

    /**
     * Returns the index of the next byte to write.
     *
     * @return the write index
     */
    long writeIndex();

    /**
     * Returns the index of the next byte to read.
     *
     * @return the read index
     */
    long readIndex();

    /**
     * Sets the write index. The index is not validated.
     *
     * @param writeIndex the new write index
     * @return this buffer
     */
    NetworkBuffer writeIndex(long writeIndex);

    /**
     * Sets the read index. The index is not validated.
     *
     * @param readIndex the new read index
     * @return this buffer
     */
    NetworkBuffer readIndex(long readIndex);

    /**
     * Sets both the read index and the write index. The indexes are not validated.
     *
     * @param readIndex  the new read index
     * @param writeIndex the new write index
     * @return this buffer
     */
    NetworkBuffer index(long readIndex, long writeIndex);

    /**
     * Moves the write index forward by {@code length}.
     *
     * @param length the number of bytes to advance
     * @return the previous write index
     */
    long advanceWrite(long length);

    /**
     * Moves the read index forward by {@code length}.
     *
     * @param length the number of bytes to advance
     * @return the previous read index
     */
    long advanceRead(long length);

    /**
     * Returns the number of bytes between the read index and the write index, {@code writeIndex() - readIndex()}.
     *
     * @return the readable bytes
     */
    long readableBytes();

    /**
     * Returns the number of bytes between the write index and the capacity, {@code capacity() - writeIndex()}.
     * A buffer with a resize strategy can grow past this.
     *
     * @return the writable bytes
     */
    long writableBytes();

    /**
     * Returns the size of the memory backing this buffer. A dummy buffer reports {@link Long#MAX_VALUE}.
     *
     * @return the capacity
     */
    long capacity();

    /**
     * Creates a read-only view of this buffer with the same indexes and registries, and no resize strategy.
     * The view shares memory with this buffer until this buffer is resized, after which it keeps the old bytes.
     *
     * @return the read-only view
     * @throws IllegalArgumentException if this buffer is a dummy
     */
    @Contract(pure = true, value = "-> new")
    NetworkBuffer readOnly();

    /**
     * Returns whether this buffer rejects writes, as the views created by {@link #readOnly()} do.
     *
     * @return true if this buffer is read-only
     */
    boolean isReadOnly();

    /**
     * Moves the contents of this buffer into new memory of {@code newSize} bytes, keeping the indexes.
     * Buffers without a resize strategy can be resized this way too.
     * Views created by {@link #readOnly()} or {@link #slice(long, long, long, long)} keep the old memory.
     *
     * @param newSize the new capacity, which must be greater than the current capacity
     * @throws IllegalArgumentException if {@code newSize} is not greater than the current capacity
     * @throws IllegalArgumentException if this buffer is read-only or a dummy
     */
    void resize(long newSize);

    /**
     * Ensures that at least {@code length} bytes can be written at the {@link #writeIndex()},
     * resizing the buffer with its resize strategy when needed.
     *
     * @param length the number of bytes, which must not be negative
     * @throws IllegalArgumentException  if {@code length} is negative, or this buffer is read-only
     * @throws IndexOutOfBoundsException if the buffer is too small and has no resize strategy,
     *                                   or the strategy returns the current capacity
     */
    void ensureWritable(long length) throws IndexOutOfBoundsException;

    /**
     * Ensures that at least {@code length} bytes are readable, as counted by {@link #readableBytes()}.
     *
     * @param length the number of bytes, which must not be negative
     * @throws IllegalArgumentException  if {@code length} is negative
     * @throws IndexOutOfBoundsException if fewer than {@code length} bytes are readable
     */
    void ensureReadable(long length) throws IndexOutOfBoundsException;

    /**
     * Moves the readable bytes to the start of the buffer and lowers both indexes by the old read index.
     * The capacity does not change.
     *
     * @throws IllegalArgumentException if this buffer is read-only or a dummy
     */
    void compact();

    /**
     * Copies {@code length} bytes starting at {@code index} into a new buffer with the given indexes.
     * The copy keeps the resize strategy and registries of this buffer.
     *
     * @param index      the index of the first byte to copy
     * @param length     the number of bytes to copy, which becomes the capacity of the copy
     * @param readIndex  the read index of the copy
     * @param writeIndex the write index of the copy
     * @return the new buffer
     * @throws IndexOutOfBoundsException if the range is outside the capacity
     * @throws IllegalArgumentException  if this buffer is a dummy
     */
    NetworkBuffer copy(long index, long length, long readIndex, long writeIndex);

    /**
     * Copies {@code length} bytes starting at {@code index} into a new buffer with the current indexes of this buffer.
     *
     * @param index  the index of the first byte to copy
     * @param length the number of bytes to copy, which becomes the capacity of the copy
     * @return the new buffer
     * @throws IndexOutOfBoundsException if the range is outside the capacity
     * @throws IllegalArgumentException  if this buffer is a dummy
     * @see #copy(long, long, long, long)
     */
    default NetworkBuffer copy(long index, long length) {
        return copy(index, length, readIndex(), writeIndex());
    }

    /**
     * Reads from {@code channel} into the {@link #writableBytes()} of this buffer and advances the {@link #writeIndex()}.
     * The buffer is not resized.
     *
     * @param channel the channel to read from
     * @return the number of bytes read
     * @throws IOException              if the channel fails or has reached the end of its stream
     * @throws IllegalArgumentException if this buffer is a dummy
     */
    int readChannel(ReadableByteChannel channel) throws IOException;

    /**
     * Writes the {@link #readableBytes()} of this buffer to {@code channel} and advances the {@link #readIndex()}
     * by the number of bytes written.
     *
     * @param channel the channel to write to
     * @return true if every readable byte was written
     * @throws IOException              if the channel fails
     * @throws IllegalArgumentException if this buffer is a dummy
     */
    boolean writeChannel(WritableByteChannel channel) throws IOException;

    /**
     * Encrypts or decrypts {@code length} bytes starting at {@code start} in place with {@code cipher}.
     *
     * @param cipher the cipher to update with the bytes
     * @param start  the index of the first byte
     * @param length the number of bytes
     * @throws IllegalArgumentException if this buffer is a dummy
     */
    void cipher(Cipher cipher, long start, long length);

    /**
     * Compresses {@code length} bytes starting at {@code start} with a {@link Deflater} into {@code output}
     * at its write index, and advances the write index of {@code output}.
     * The output is not resized, so it needs enough writable bytes for the compressed data.
     *
     * @param start  the index of the first byte
     * @param length the number of bytes
     * @param output the buffer to write the compressed bytes to
     * @return the number of compressed bytes written
     * @throws IllegalArgumentException if either buffer is a dummy, or {@code output} is read-only
     */
    long compress(long start, long length, NetworkBuffer output);

    /**
     * Decompresses {@code length} bytes starting at {@code start} with an {@link Inflater} into {@code output}
     * at its write index, and advances the write index of {@code output}. The output is not resized.
     *
     * @param start  the index of the first byte
     * @param length the number of bytes
     * @param output the buffer to write the decompressed bytes to
     * @return the number of decompressed bytes written
     * @throws DataFormatException      if the data is invalid or incomplete, or does not fit in the writable bytes of {@code output}
     * @throws IllegalArgumentException if either buffer is a dummy, or {@code output} is read-only
     */
    long decompress(long start, long length, NetworkBuffer output) throws DataFormatException;

    /**
     * Returns the registries for types that need them, given when the buffer was created
     * or set with {@link #registries(Registries)}.
     *
     * @return the registries, or {@code null} if there are none
     */
    @Nullable Registries registries();

    /**
     * Sets the registries for types that need them, replacing the ones given when the buffer was created.
     * Buffers already created from this one by {@link #readOnly()}, {@link #slice(long, long, long, long)},
     * or {@link #copy(long, long, long, long)} keep their own registries.
     *
     * @param registries the registries, or {@code null} for none
     */
    void registries(@Nullable Registries registries);

    /**
     * A slice is an operation where the backing memory is used for the new buffer. Consider it a view over the backing memory region.
     *
     * @param offset     the position to start the slice
     * @param byteLength the length of the slice, equal to {@link #capacity()}
     * @param readIndex  the read index
     * @param writeIndex the write index
     * @return a new buffer
     * @throws IllegalArgumentException if this buffer is a dummy
     * @apiNote With resized segments, you could observe behavior where you may see stale data or close arenas.
     * To avoid this, do not use resizable segments in a way where they are written or resized like {@link #resize(long)}.
     * @implNote {@link #registries()} are copied into the new buffer
     */
    @Contract(pure = true, value = "_, _, _, _ -> new")
    NetworkBuffer slice(long offset, long byteLength, long readIndex, long writeIndex);

    /**
     * Reads and writes values of {@code T} in a {@link NetworkBuffer}, so that reading returns the value that was written.
     * Unlike {@link StructCodec}, types are always written linearly into a {@link NetworkBuffer}.
     * Prefer {@link NetworkBufferTemplate} to build types for records, which keeps both directions in sync.
     *
     * @param <T> the value type, which may be nullable
     */
    interface Type<T extends @UnknownNullability Object> {
        /**
         * Writes a value to {@code buffer} at its write index.
         *
         * @param buffer the buffer to write to
         * @param value  the value
         */
        void write(NetworkBuffer buffer, T value);

        /**
         * Reads a value from {@code buffer} at its read index.
         *
         * @param buffer the buffer to read from
         * @return the value
         */
        T read(NetworkBuffer buffer);

        /**
         * Returns the number of bytes that {@link #write(NetworkBuffer, Object)} uses for {@code value}.
         * The value is written to a dummy buffer, which counts bytes without storing them and cannot be read.
         * Override this method rather than {@link #sizeOf(Object)}, which calls it without registries.
         *
         * @param value      the value to measure
         * @param registries the registries for types that need them, or {@code null}
         * @return the size in bytes
         */
        default long sizeOf(T value, @Nullable Registries registries) {
            return NetworkBufferTypeImpl.sizeOf(this, value, registries);
        }

        /**
         * Returns the number of bytes that {@link #write(NetworkBuffer, Object)} uses for {@code value}, without registries.
         *
         * @param value the value to measure
         * @return the size in bytes
         * @see #sizeOf(Object, Registries)
         */
        default long sizeOf(T value) {
            return sizeOf(value, null);
        }

        /**
         * Creates a type that converts between the values of this type and {@code S}.
         *
         * @param to   the function applied to each value read by this type
         * @param from the function applied to each value before this type writes it
         * @param <S>  the converted type
         * @return the converted type
         */
        default <S extends @UnknownNullability Object> Type<S> transform(Function<? super T, ? extends S> to, Function<? super S, ? extends T> from) {
            return new NetworkBufferTypeImpl.TransformType<>(this, to, from);
        }

        /**
         * Creates a type for an unmodifiable map with keys of this type,
         * encoded as a {@link #VAR_INT} size followed by each key and value.
         * Reading throws {@link IllegalArgumentException} when the size is negative or greater than {@code maxSize},
         * or a key repeats.
         *
         * @param valueType the type of the values
         * @param maxSize   the largest size accepted when reading
         * @param <V>       the value type
         * @return the map type
         */
        default <V extends @UnknownNullability Object> Type<@Unmodifiable @UnknownNullability Map<T, V>> mapValue(Type<V> valueType, int maxSize) {
            return new NetworkBufferTypeImpl.MapType<>(this, valueType, maxSize);
        }

        /**
         * Creates a type for an unmodifiable map with keys of this type, accepting up to {@link Integer#MAX_VALUE} entries.
         * Use {@link #mapValue(Type, int)} when the size has a strict upper bound.
         *
         * @param valueType the type of the values
         * @param <V>       the value type
         * @return the map type
         */
        default <V extends @UnknownNullability Object> Type<@Unmodifiable @UnknownNullability Map<T, V>> mapValue(Type<V> valueType) {
            return mapValue(valueType, Integer.MAX_VALUE);
        }

        /**
         * Creates a type for an unmodifiable list of this type, encoded as a {@link #VAR_INT} size followed by each element.
         * Writing {@code null} encodes an empty list. Reading throws {@link IllegalArgumentException}
         * when the size is negative or greater than {@code maxSize}.
         *
         * @param maxSize the largest size accepted when reading
         * @return the list type
         */
        default Type<@Unmodifiable @UnknownNullability List<T>> list(int maxSize) {
            return new NetworkBufferTypeImpl.ListType<>(this, maxSize);
        }

        /**
         * Creates a type for an unmodifiable list of this type, accepting up to {@link Integer#MAX_VALUE} elements.
         * Use {@link #list(int)} when the size has a strict upper bound.
         *
         * @return the list type
         */
        default Type<@Unmodifiable @UnknownNullability List<T>> list() {
            return list(Integer.MAX_VALUE);
        }

        /**
         * Creates a type for an unmodifiable set of this type, encoded as a {@link #VAR_INT} size followed by each element.
         * Writing {@code null} encodes an empty set. Reading throws {@link IllegalArgumentException}
         * when the size is negative or greater than {@code maxSize}, or an element repeats.
         *
         * @param maxSize the largest size accepted when reading
         * @return the set type
         */
        default Type<@Unmodifiable @UnknownNullability Set<T>> set(int maxSize) {
            return new NetworkBufferTypeImpl.SetType<>(this, maxSize);
        }

        /**
         * Creates a type for an unmodifiable set of this type, accepting up to {@link Integer#MAX_VALUE} elements.
         * Use {@link #set(int)} when the size has a strict upper bound.
         *
         * @return the set type
         */
        default Type<@Unmodifiable @UnknownNullability Set<T>> set() {
            return set(Integer.MAX_VALUE);
        }

        /**
         * Creates a type that also accepts {@code null}, encoded as a {@link #BOOLEAN} that is {@code true}
         * when a value of this type follows.
         *
         * @return the optional type
         */
        default Type<@Nullable T> optional() {
            return new NetworkBufferTypeImpl.OptionalType<>(this);
        }

        /**
         * Creates a type for the subtypes of {@code R}, such as the permitted types of a sealed interface.
         * Each value is encoded as its key in this type, followed by the value in the serializer for that key.
         * Reading and writing throw {@link UnsupportedOperationException} when {@code serializers} returns {@code null}.
         *
         * @param serializers the serializer for the values of each key
         * @param keyFunc     the key of a value
         * @param <R>         the union type
         * @return the union type
         */
        default <R extends @UnknownNullability Object> Type<R> unionType(Function<? super T, NetworkBuffer.Type<? extends R>> serializers, Function<? super R, ? extends T> keyFunc) {
            return new NetworkBufferTypeImpl.UnionType<>(this, keyFunc, serializers);
        }

        /**
         * Creates a type that encodes each value as a {@link #BYTE_ARRAY}, which is a {@link #VAR_INT} length
         * followed by the bytes of the value. Reading throws {@link IllegalArgumentException} when the length
         * is greater than {@code maxLength} or the readable bytes, or the value does not use exactly that many bytes.
         *
         * @param maxLength the largest length accepted when reading
         * @return the length prefixed type
         */
        default Type<T> lengthPrefixed(int maxLength) {
            return new NetworkBufferTypeImpl.LengthPrefixedType<>(this, maxLength);
        }

        /**
         * Creates a type that encodes values the same way as this type, but rejects values longer than {@code maxLength} bytes.
         * Both throw {@link IllegalArgumentException} for a value that is too long.
         *
         * @param maxLength the largest encoded size in bytes
         * @return the length limited type
         */
        default Type<T> maxLength(long maxLength) {
            return new NetworkBufferTypeImpl.MaxLength<>(this, maxLength);
        }
    }

    /**
     * Creates a builder for a buffer with an initial capacity of {@code size} bytes.
     * The buffer has no resize strategy and no registries unless the builder sets them.
     *
     * @param size the initial capacity, which must not be negative
     * @return the new builder
     */
    static Builder builder(long size) {
        return new NetworkBufferImpl.Builder(size);
    }

    /**
     * Creates a buffer with a capacity of {@code size} bytes and no resize strategy,
     * so writes past the capacity throw {@link IndexOutOfBoundsException}.
     *
     * @param size       the capacity
     * @param registries the registries for types that need them
     * @return the new buffer
     */
    static NetworkBuffer staticBuffer(long size, Registries registries) {
        return builder(size).registry(registries).build();
    }

    /**
     * Creates a buffer with a capacity of {@code size} bytes, no resize strategy, and no registries.
     *
     * @param size the capacity
     * @return the new buffer
     * @see #staticBuffer(long, Registries)
     */
    static NetworkBuffer staticBuffer(long size) {
        return builder(size).build();
    }

    /**
     * Creates a buffer with an initial capacity of {@code initialSize} bytes that grows with {@link AutoResize#DOUBLE}.
     *
     * @param initialSize the initial capacity
     * @param registries  the registries for types that need them
     * @return the new buffer
     */
    static NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        return builder(initialSize)
                .autoResize(AutoResize.DOUBLE)
                .registry(registries)
                .build();
    }

    /**
     * Creates a buffer with an initial capacity of {@code initialSize} bytes that grows with {@link AutoResize#DOUBLE},
     * and has no registries.
     *
     * @param initialSize the initial capacity
     * @return the new buffer
     */
    static NetworkBuffer resizableBuffer(long initialSize) {
        return builder(initialSize)
                .autoResize(AutoResize.DOUBLE)
                .build();
    }

    /**
     * Creates a buffer with an initial capacity of 256 bytes that grows with {@link AutoResize#DOUBLE}.
     *
     * @param registries the registries for types that need them
     * @return the new buffer
     */
    static NetworkBuffer resizableBuffer(Registries registries) {
        return resizableBuffer(256, registries);
    }

    /**
     * Creates a buffer with an initial capacity of 256 bytes that grows with {@link AutoResize#DOUBLE},
     * and has no registries.
     *
     * @return the new buffer
     */
    static NetworkBuffer resizableBuffer() {
        return resizableBuffer(256);
    }

    /**
     * Creates a buffer that reads and writes {@code segment} directly, with no resize strategy.
     * Useful when you already have a memory segment.
     *
     * @param segment    the memory to use
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @param registries the {@link #registries()}, or {@code null}
     * @return the new buffer
     */
    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        return NetworkBufferImpl.wrap(segment, readIndex, writeIndex, registries);
    }

    /**
     * Creates a buffer that reads and writes {@code segment} directly, with no resize strategy and no registries.
     *
     * @param segment    the memory to use
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @return the new buffer
     * @see #wrap(MemorySegment, long, long, Registries)
     */
    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex) {
        return wrap(segment, readIndex, writeIndex, null);
    }

    /**
     * Creates a buffer that reads and writes {@code bytes} directly, with no resize strategy.
     * Useful when you already have a {@code byte[]}.
     *
     * @param bytes      the array to use
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @param registries the {@link #registries()}, or {@code null}
     * @return the new buffer
     */
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex, @Nullable Registries registries) {
        return wrap(MemorySegment.ofArray(bytes), readIndex, writeIndex, registries);
    }

    /**
     * Creates a buffer that reads and writes {@code bytes} directly, with no resize strategy and no registries.
     *
     * @param bytes      the array to use
     * @param readIndex  the {@link #readIndex()}
     * @param writeIndex the {@link #writeIndex()}
     * @return the new buffer
     * @see #wrap(byte[], int, int, Registries)
     */
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return wrap(bytes, readIndex, writeIndex, null);
    }

    /**
     * Configures and creates buffers, obtained from {@link #builder(long)}.
     * A builder can be reused, and each call to {@link #build()} creates a separate buffer.
     */
    sealed interface Builder permits NetworkBufferImpl.Builder {
        /**
         * Sets the resize strategy used when a write needs more room than the capacity.
         *
         * @param autoResize the resize strategy, or {@code null} for a buffer that does not grow
         * @return this builder
         */
        Builder autoResize(@Nullable AutoResize autoResize);

        /**
         * Sets the registries for types that need them.
         *
         * @param registries the registries, or {@code null} for none
         * @return this builder
         */
        Builder registry(@Nullable Registries registries);

        /**
         * Creates a buffer with the configured capacity, resize strategy, and registries,
         * with both indexes at 0. The buffer allocates native memory, which is freed once it is no longer reachable.
         *
         * @return the new buffer
         * @throws IllegalArgumentException if the capacity is negative
         */
        NetworkBuffer build();
    }

    /**
     * Resize strategy for a {@link NetworkBuffer}, used when a write needs more room than the capacity.
     */
    @FunctionalInterface
    interface AutoResize {
        AutoResize DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        /**
         * Returns the new capacity for a buffer that needs to hold {@code targetSize} bytes.
         *
         * @param capacity   the current capacity
         * @param targetSize the write index plus the number of bytes being written
         * @return the new capacity, which must be at least {@code targetSize}
         */
        long resize(long capacity, long targetSize);
    }

    /**
     * Runs {@code writing} on a new resizable buffer and returns the bytes it wrote, in an array of exactly that length.
     *
     * @param writing    the writes to capture
     * @param registries the registries for types that need them, or {@code null}
     * @return the written bytes
     */
    static byte[] makeArray(Consumer<? super NetworkBuffer> writing, @Nullable Registries registries) {
        NetworkBuffer buffer = resizableBuffer(256, registries);
        writing.accept(buffer);
        return buffer.read(RAW_BYTES);
    }

    /**
     * Runs {@code writing} on a new resizable buffer without registries and returns the bytes it wrote.
     *
     * @param writing the writes to capture
     * @return the written bytes
     * @see #makeArray(Consumer, Registries)
     */
    static byte[] makeArray(Consumer<? super NetworkBuffer> writing) {
        return makeArray(writing, null);
    }

    /**
     * Encodes {@code value} with {@code type} and returns the bytes, in an array of exactly that length.
     *
     * @param type       the type of the value
     * @param value      the value to encode
     * @param registries the registries for types that need them, or {@code null}
     * @param <T>        the value type
     * @return the encoded bytes
     */
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value, @Nullable Registries registries) {
        return makeArray(buffer -> buffer.write(type, value), registries);
    }

    /**
     * Encodes {@code value} with {@code type} without registries and returns the bytes.
     *
     * @param type  the type of the value
     * @param value the value to encode
     * @param <T>   the value type
     * @return the encoded bytes
     * @see #makeArray(Type, Object, Registries)
     */
    static <T extends @UnknownNullability Object> byte[] makeArray(Type<T> type, T value) {
        return makeArray(type, value, null);
    }

    /**
     * Copies {@code length} bytes from {@code srcBuffer} at {@code srcOffset} to {@code dstBuffer} at {@code dstOffset},
     * ignoring the read and write indexes of both buffers. {@code dstBuffer} is not resized.
     *
     * @param srcBuffer the buffer to copy from
     * @param srcOffset the index of the first byte to copy
     * @param dstBuffer the buffer to copy to
     * @param dstOffset the index in {@code dstBuffer} to copy to
     * @param length    the number of bytes to copy
     * @throws IndexOutOfBoundsException if either range is outside the capacity of its buffer
     * @throws IllegalArgumentException  if either buffer is a dummy, or {@code dstBuffer} is read-only
     */
    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        NetworkBufferImpl.copy(srcBuffer, srcOffset, dstBuffer, dstOffset, length);
    }

    /**
     * Checks whether two buffers have the same capacity and the same bytes across it,
     * ignoring their read and write indexes.
     *
     * @param buffer1 the first buffer
     * @param buffer2 the second buffer
     * @return true if the contents are equal
     * @throws IllegalArgumentException if either buffer is a dummy
     */
    static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        return NetworkBufferImpl.equals(buffer1, buffer2);
    }
}
