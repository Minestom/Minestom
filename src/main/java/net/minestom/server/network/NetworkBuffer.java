package net.minestom.server.network;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A network buffer backed by a Netty {@link ByteBuf} instead of raw UNSAFE memory.
 * <p>
 * All previous usages of {@code java.nio.channels.ReadableByteChannel},
 * {@code java.nio.channels.SocketChannel}, and sun.misc.Unsafe have been removed.
 * Compression is delegated to Netty's built-in zlib utilities.
 */
public sealed interface NetworkBuffer permits NetworkBufferImpl {

    Type<Unit>    UNIT           = new NetworkBufferTypeImpl.UnitType();
    Type<Boolean> BOOLEAN        = new NetworkBufferTypeImpl.BooleanType();
    Type<Byte>    BYTE           = new NetworkBufferTypeImpl.ByteType();
    Type<Short>   UNSIGNED_BYTE  = new NetworkBufferTypeImpl.UnsignedByteType();
    Type<Short>   SHORT          = new NetworkBufferTypeImpl.ShortType();
    Type<Integer> UNSIGNED_SHORT = new NetworkBufferTypeImpl.UnsignedShortType();
    Type<Integer> INT            = new NetworkBufferTypeImpl.IntType();
    Type<Long>    UNSIGNED_INT   = new NetworkBufferTypeImpl.UnsignedIntType();
    Type<Long>    LONG           = new NetworkBufferTypeImpl.LongType();
    Type<Float>   FLOAT          = new NetworkBufferTypeImpl.FloatType();
    Type<Double>  DOUBLE         = new NetworkBufferTypeImpl.DoubleType();

    Type<Integer>           VAR_INT          = new NetworkBufferTypeImpl.VarIntType();
    Type<@Nullable Integer> OPTIONAL_VAR_INT = new NetworkBufferTypeImpl.OptionalVarIntType();
    Type<Integer>           VAR_INT_3        = new NetworkBufferTypeImpl.VarInt3Type();
    Type<Long>              VAR_LONG         = new NetworkBufferTypeImpl.VarLongType();

    Type<byte[]>  RAW_BYTES         = new NetworkBufferTypeImpl.RawBytesType(-1);
    Type<String>  STRING            = new NetworkBufferTypeImpl.StringType();
    Type<Key>     KEY               = STRING.transform(Key::key, Key::asString);
    Type<String>  STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    Type<String>  STRING_IO_UTF8    = new NetworkBufferTypeImpl.IOUTF8StringType();

    Type<BinaryTag>         NBT          = new NetworkBufferTypeImpl.NbtType();
    @SuppressWarnings({"unchecked", "rawtypes"})
    Type<CompoundBinaryTag> NBT_COMPOUND = (Type) new NetworkBufferTypeImpl.NbtType();

    Type<Point>     BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    Type<Component> COMPONENT      = new ComponentNetworkBufferTypeImpl();
    Type<Component> JSON_COMPONENT = new NetworkBufferTypeImpl.JsonComponentType();
    Type<UUID>      UUID           = new NetworkBufferTypeImpl.UUIDType();
    Type<Pos>       POS            = new NetworkBufferTypeImpl.PosType();

    Type<byte[]>  BYTE_ARRAY      = new NetworkBufferTypeImpl.ByteArrayType();
    Type<long[]>  LONG_ARRAY      = new NetworkBufferTypeImpl.LongArrayType();
    Type<int[]>   VAR_INT_ARRAY   = new NetworkBufferTypeImpl.VarIntArrayType();
    Type<long[]>  VAR_LONG_ARRAY  = new NetworkBufferTypeImpl.VarLongArrayType();

    Type<BitSet>      BITSET     = LONG_ARRAY.transform(BitSet::valueOf, BitSet::toLongArray);
    Type<Instant>     INSTANT_MS = LONG.transform(Instant::ofEpochMilli, Instant::toEpochMilli);
    Type<PublicKey>   PUBLIC_KEY = BYTE_ARRAY.transform(KeyUtils::publicRSAKeyFrom, PublicKey::getEncoded);

    Type<Point>   VECTOR3   = new NetworkBufferTypeImpl.Vector3Type();
    Type<Point>   VECTOR3D  = new NetworkBufferTypeImpl.Vector3DType();
    Type<Point>   VECTOR3I  = new NetworkBufferTypeImpl.Vector3IType();
    Type<Point>   VECTOR3B  = new NetworkBufferTypeImpl.Vector3BType();
    Type<Vec>     LP_VECTOR3 = new NetworkBufferTypeImpl.LpVector3Type();
    Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();

    Type<@Nullable Component> OPT_CHAT           = COMPONENT.optional();
    Type<@Nullable Point>     OPT_BLOCK_POSITION = BLOCK_POSITION.optional();

    Type<Direction>  DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE      = Enum(EntityPose.class);

    static <E extends Enum<E>> Type<E> Enum(Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(i -> values[i], Enum::ordinal);
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

    static <L, R> Type<Either<L, R>> Either(Type<L> left, Type<R> right) {
        return new NetworkBufferTypeImpl.EitherType<>(left, right);
    }

    <T> void write(Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T read(Type<T> type) throws IndexOutOfBoundsException;

    <T> void writeAt(long index, Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T readAt(long index, Type<T> type) throws IndexOutOfBoundsException;

    void copyTo(long srcOffset, byte[] dest, long destOffset, long length);

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
    void compact();

    NetworkBuffer copy(long index, long length, long readIndex, long writeIndex);

    default NetworkBuffer copy(long index, long length) {
        return copy(index, length, readIndex(), writeIndex());
    }

    /**
     * Reads available bytes from a Netty {@link ByteBuf} into this buffer,
     * advancing the write index accordingly.
     *
     * @param in the source Netty buffer (typically from a channel read)
     * @return number of bytes transferred
     */
    int readFromByteBuf(ByteBuf in);

    /**
     * Writes all readable bytes of this buffer into the supplied Netty
     * {@link ByteBuf} (typically the channel's outbound buffer).
     *
     * @param out the destination Netty buffer
     * @return {@code true} if all bytes were written, {@code false} if partial
     */
    boolean writeToByteBuf(ByteBuf out);

    /**
     * In-place cipher transformation over a region of this buffer.
     * Used for Mojang's AES-CFB8 encryption/decryption applied directly to
     * the raw bytes before they are handed to Netty.
     */
    void cipher(Cipher cipher, long start, long length);

    /**
     * Compresses {@code length} bytes starting at {@code start} into {@code output}.
     *
     * @return number of compressed bytes written to {@code output}
     * @throws IOException on compression error
     */
    long compress(long start, long length, NetworkBuffer output) throws IOException;

    /**
     * Decompresses {@code length} bytes starting at {@code start} into {@code output}.
     *
     * @return number of decompressed bytes written to {@code output}
     * @throws IOException on decompression error
     */
    long decompress(long start, long length, NetworkBuffer output) throws IOException;

    @Nullable Registries registries();

    interface Type<T> {
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

        default <R, TR extends R> Type<R> unionType(
                Function<T, NetworkBuffer.Type<TR>> serializers,
                Function<R, ? extends T> keyFunc) {
            return new NetworkBufferTypeImpl.UnionType<>(this, keyFunc, serializers);
        }

        default Type<T> lengthPrefixed(int maxLength) {
            return new NetworkBufferTypeImpl.LengthPrefixedType<>(this, maxLength);
        }
    }

    static Builder builder(long size) {
        return new NetworkBufferImpl.Builder(size);
    }

    static NetworkBuffer staticBuffer(long size, Registries registries) {
        return builder(size).registry(registries).build();
    }

    static NetworkBuffer staticBuffer(long size) {
        return staticBuffer(size, null);
    }

    static NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        return builder(initialSize)
                .autoResize(AutoResize.DOUBLE)
                .registry(registries)
                .build();
    }

    static NetworkBuffer resizableBuffer(int initialSize) {
        return resizableBuffer(initialSize, null);
    }

    static NetworkBuffer resizableBuffer(Registries registries) {
        return resizableBuffer(256, registries);
    }

    static NetworkBuffer resizableBuffer() {
        return resizableBuffer((Registries) null);
    }

    /**
     * Wraps an existing byte array. Copies the data into a new buffer so the
     * array lifetime is not tied to the buffer.
     */
    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex,
                              @Nullable Registries registries) {
        return NetworkBufferImpl.wrap(bytes, readIndex, writeIndex, registries);
    }

    static NetworkBuffer wrap(byte[] bytes, int readIndex, int writeIndex) {
        return wrap(bytes, readIndex, writeIndex, null);
    }

    /**
     * Wraps a Netty {@link ByteBuf} directly. The caller retains ownership; the
     * returned {@link NetworkBuffer} does <em>not</em> release the buf.
     */
    static NetworkBuffer fromByteBuf(ByteBuf buf, @Nullable Registries registries) {
        return NetworkBufferImpl.fromByteBuf(buf, registries);
    }

    static NetworkBuffer fromByteBuf(ByteBuf buf) {
        return fromByteBuf(buf, null);
    }

    sealed interface Builder permits NetworkBufferImpl.Builder {
        Builder autoResize(@Nullable AutoResize autoResize);
        Builder registry(@Nullable Registries registries);
        NetworkBuffer build();
    }

    @FunctionalInterface
    interface AutoResize {
        AutoResize DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);
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

    static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        return NetworkBufferImpl.equals(buffer1, buffer2);
    }
}