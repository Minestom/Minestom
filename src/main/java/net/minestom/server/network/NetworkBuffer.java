package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.crypto.KeyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;

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
    Type<Integer> VAR_INT_3 = new NetworkBufferTypeImpl.VarInt3Type();
    Type<Long> VAR_LONG = new NetworkBufferTypeImpl.VarLongType();
    Type<byte[]> RAW_BYTES = new NetworkBufferTypeImpl.RawBytesType(-1);
    Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    Type<String> STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    Type<BinaryTag> NBT = new NetworkBufferTypeImpl.NbtType();
    @SuppressWarnings({"unchecked", "rawtypes"})
    Type<CompoundBinaryTag> NBT_COMPOUND = (Type) new NetworkBufferTypeImpl.NbtType();
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

    static <T extends ProtocolObject> @NotNull Type<DynamicRegistry.Key<T>> RegistryKey(@NotNull Function<Registries, DynamicRegistry<T>> selector) {
        return new NetworkBufferTypeImpl.RegistryTypeType<>(selector);
    }

    // METADATA
    Type<int[]> VILLAGER_DATA = new NetworkBufferTypeImpl.VillagerDataType();
    Type<Point> VECTOR3 = new NetworkBufferTypeImpl.Vector3Type();
    Type<Point> VECTOR3D = new NetworkBufferTypeImpl.Vector3DType();
    Type<Point> VECTOR3B = new NetworkBufferTypeImpl.Vector3BType();
    Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();

    Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();
    Type<@Nullable UUID> OPT_UUID = UUID.optional();

    Type<Direction> DIRECTION = Enum(Direction.class);
    Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    static <E extends Enum<E>> @NotNull Type<E> Enum(@NotNull Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    static <E extends Enum<E>> @NotNull Type<EnumSet<E>> EnumSet(@NotNull Class<E> enumClass) {
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, enumClass.getEnumConstants());
    }

    static @NotNull Type<BitSet> FixedBitSet(int length) {
        return new NetworkBufferTypeImpl.FixedBitSetType(length);
    }

    static @NotNull Type<byte[]> FixedRawBytes(int length) {
        return new NetworkBufferTypeImpl.RawBytesType(length);
    }

    static <T> @NotNull Type<T> Lazy(@NotNull Supplier<@NotNull Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }

    <T> void write(@NotNull Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T read(@NotNull Type<T> type) throws IndexOutOfBoundsException;

    <T> void writeAt(long index, @NotNull Type<T> type, @UnknownNullability T value) throws IndexOutOfBoundsException;

    <T> @UnknownNullability T readAt(long index, @NotNull Type<T> type) throws IndexOutOfBoundsException;

    void copyTo(long srcOffset, byte @NotNull [] dest, long destOffset, long length);

    byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor);

    @NotNull NetworkBuffer clear();

    long writeIndex();

    long readIndex();

    @NotNull NetworkBuffer writeIndex(long writeIndex);

    @NotNull NetworkBuffer readIndex(long readIndex);

    @NotNull NetworkBuffer index(long readIndex, long writeIndex);

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

    int readChannel(ReadableByteChannel channel) throws IOException;

    boolean writeChannel(SocketChannel channel) throws IOException;

    void cipher(Cipher cipher, long start, long length);

    long compress(long start, long length, NetworkBuffer output);

    long decompress(long start, long length, NetworkBuffer output) throws DataFormatException;

    @Nullable Registries registries();

    interface Type<T> {
        void write(@NotNull NetworkBuffer buffer, T value);

        T read(@NotNull NetworkBuffer buffer);

        default long sizeOf(@NotNull T value, @Nullable Registries registries) {
            return NetworkBufferTypeImpl.sizeOf(this, value, registries);
        }

        default long sizeOf(@NotNull T value) {
            return sizeOf(value, null);
        }

        default <S> @NotNull Type<S> transform(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
            return new NetworkBufferTypeImpl.TransformType<>(this, to, from);
        }

        default <V> @NotNull Type<Map<T, V>> mapValue(@NotNull Type<V> valueType, int maxSize) {
            return new NetworkBufferTypeImpl.MapType<>(this, valueType, maxSize);
        }

        default <V> @NotNull Type<Map<T, V>> mapValue(@NotNull Type<V> valueType) {
            return mapValue(valueType, Integer.MAX_VALUE);
        }

        default @NotNull Type<List<T>> list(int maxSize) {
            return new NetworkBufferTypeImpl.ListType<>(this, maxSize);
        }

        default @NotNull Type<List<T>> list() {
            return list(Integer.MAX_VALUE);
        }

        default @NotNull Type<T> optional() {
            return new NetworkBufferTypeImpl.OptionalType<>(this);
        }
    }

    static @NotNull Builder builder(long size) {
        return new NetworkBufferImpl.Builder(size);
    }

    static @NotNull NetworkBuffer staticBuffer(long size, Registries registries) {
        return builder(size).registry(registries).build();
    }

    static @NotNull NetworkBuffer staticBuffer(long size) {
        return staticBuffer(size, null);
    }

    static @NotNull NetworkBuffer resizableBuffer(long initialSize, Registries registries) {
        return builder(initialSize)
                .autoResize(AutoResize.DOUBLE)
                .registry(registries)
                .build();
    }

    static @NotNull NetworkBuffer resizableBuffer(int initialSize) {
        return resizableBuffer(initialSize, null);
    }

    static @NotNull NetworkBuffer resizableBuffer(Registries registries) {
        return resizableBuffer(256, registries);
    }

    static @NotNull NetworkBuffer resizableBuffer() {
        return resizableBuffer(null);
    }

    static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes, int readIndex, int writeIndex, @Nullable Registries registries) {
        return NetworkBufferImpl.wrap(bytes, readIndex, writeIndex, registries);
    }

    static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes, int readIndex, int writeIndex) {
        return wrap(bytes, readIndex, writeIndex, null);
    }

    sealed interface Builder permits NetworkBufferImpl.Builder {
        @NotNull Builder autoResize(@Nullable AutoResize autoResize);

        @NotNull Builder registry(@Nullable Registries registries);

        @NotNull NetworkBuffer build();
    }

    @FunctionalInterface
    interface AutoResize {
        AutoResize DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        long resize(long capacity, long targetSize);
    }

    static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing, @Nullable Registries registries) {
        NetworkBuffer buffer = resizableBuffer(256, registries);
        writing.accept(buffer);
        return buffer.read(RAW_BYTES);
    }

    static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing) {
        return makeArray(writing, null);
    }

    static <T> byte[] makeArray(@NotNull Type<T> type, @NotNull T value, @Nullable Registries registries) {
        return makeArray(buffer -> buffer.write(type, value), registries);
    }

    static <T> byte[] makeArray(@NotNull Type<T> type, @NotNull T value) {
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
