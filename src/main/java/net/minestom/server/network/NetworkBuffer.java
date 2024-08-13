package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
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

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    Type<Long> VAR_LONG = new NetworkBufferTypeImpl.VarLongType();
    Type<byte[]> RAW_BYTES = new NetworkBufferTypeImpl.RawBytesType(-1);
    Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    Type<String> STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    Type<BinaryTag> NBT = new NetworkBufferTypeImpl.NbtType();
    Type<Point> BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    Type<Component> COMPONENT = new NetworkBufferTypeImpl.ComponentType();
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

    <T> void write(@NotNull Type<T> type, @UnknownNullability T value);

    <T> @UnknownNullability T read(@NotNull Type<T> type);

    void copyTo(int srcOffset, byte @NotNull [] dest, int destOffset, int length);

    byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor);

    void clear();

    int writeIndex();

    int readIndex();

    void writeIndex(int writeIndex);

    void readIndex(int readIndex);

    int advanceWrite(int length);

    int advanceRead(int length);

    int readableBytes();

    int size();

    void resize(int newSize);

    void ensureSize(int length);

    interface Type<T> {
        void write(@NotNull NetworkBuffer buffer, T value);

        T read(@NotNull NetworkBuffer buffer);

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

    static @NotNull Builder builder(int size) {
        return new NetworkBufferImpl.Builder(size);
    }

    static @NotNull NetworkBuffer staticBuffer(int size, Registries registries) {
        return builder(size).registry(registries).build();
    }

    static @NotNull NetworkBuffer staticBuffer(int size) {
        return staticBuffer(size, null);
    }

    static @NotNull NetworkBuffer resizableBuffer(int initialSize, Registries registries) {
        return builder(initialSize)
                .resizeStrategy(ResizeStrategy.DOUBLE)
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

    static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes, @Nullable Registries registries) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new NetworkBufferImpl(buffer, null, registries);
    }

    static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes) {
        return wrap(bytes, null);
    }

    static NetworkBuffer wrap(@NotNull ByteBuffer buffer, @Nullable Registries registries) {
        return new NetworkBufferImpl(buffer, null, registries);
    }

    static NetworkBuffer wrap(@NotNull ByteBuffer buffer) {
        return wrap(buffer, null);
    }

    sealed interface Builder permits NetworkBufferImpl.Builder {
        @NotNull Builder resizeStrategy(@Nullable ResizeStrategy resizeStrategy);

        @NotNull Builder registry(@Nullable Registries registries);

        @NotNull NetworkBuffer build();
    }

    @FunctionalInterface
    interface ResizeStrategy {
        ResizeStrategy DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        long resize(long capacity, long targetSize);
    }

    static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing, @Nullable Registries registries) {
        NetworkBuffer writer = resizableBuffer(256, registries);
        writing.accept(writer);
        byte[] bytes = new byte[writer.writeIndex()];
        writer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
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
}
