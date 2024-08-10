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
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Experimental
public final class NetworkBuffer {
    public static final Type<Unit> UNIT = new NetworkBufferTypeImpl.UnitType();
    public static final Type<Boolean> BOOLEAN = new NetworkBufferTypeImpl.BooleanType();
    public static final Type<Byte> BYTE = new NetworkBufferTypeImpl.ByteType();
    public static final Type<Short> SHORT = new NetworkBufferTypeImpl.ShortType();
    public static final Type<Integer> UNSIGNED_SHORT = new NetworkBufferTypeImpl.UnsignedShortType();
    public static final Type<Integer> INT = new NetworkBufferTypeImpl.IntType();
    public static final Type<Long> LONG = new NetworkBufferTypeImpl.LongType();
    public static final Type<Float> FLOAT = new NetworkBufferTypeImpl.FloatType();
    public static final Type<Double> DOUBLE = new NetworkBufferTypeImpl.DoubleType();
    public static final Type<Integer> VAR_INT = new NetworkBufferTypeImpl.VarIntType();
    public static final Type<Long> VAR_LONG = new NetworkBufferTypeImpl.VarLongType();
    public static final Type<byte[]> RAW_BYTES = new NetworkBufferTypeImpl.RawBytesType(-1);
    public static final Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    public static final Type<String> STRING_TERMINATED = new NetworkBufferTypeImpl.StringTerminatedType();
    public static final Type<BinaryTag> NBT = new NetworkBufferTypeImpl.NbtType();
    public static final Type<Point> BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    public static final Type<Component> COMPONENT = new NetworkBufferTypeImpl.ComponentType();
    public static final Type<Component> JSON_COMPONENT = new NetworkBufferTypeImpl.JsonComponentType();
    public static final Type<UUID> UUID = new NetworkBufferTypeImpl.UUIDType();
    public static final Type<Pos> POS = new NetworkBufferTypeImpl.PosType();

    public static final Type<byte[]> BYTE_ARRAY = new NetworkBufferTypeImpl.ByteArrayType();
    public static final Type<long[]> LONG_ARRAY = new NetworkBufferTypeImpl.LongArrayType();
    public static final Type<int[]> VAR_INT_ARRAY = new NetworkBufferTypeImpl.VarIntArrayType();
    public static final Type<long[]> VAR_LONG_ARRAY = new NetworkBufferTypeImpl.VarLongArrayType();

    public static final Type<BitSet> BITSET = LONG_ARRAY.transform(BitSet::valueOf, BitSet::toLongArray);
    public static final Type<Instant> INSTANT_MS = LONG.transform(Instant::ofEpochMilli, Instant::toEpochMilli);
    public static final Type<PublicKey> PUBLIC_KEY = BYTE_ARRAY.transform(KeyUtils::publicRSAKeyFrom, PublicKey::getEncoded);

    public static <T extends ProtocolObject> @NotNull Type<DynamicRegistry.Key<T>> RegistryKey(@NotNull Function<Registries, DynamicRegistry<T>> selector) {
        return new NetworkBufferTypeImpl.RegistryTypeType<>(selector);
    }

    // METADATA
    public static final Type<int[]> VILLAGER_DATA = new NetworkBufferTypeImpl.VillagerDataType();
    public static final Type<Point> VECTOR3 = new NetworkBufferTypeImpl.Vector3Type();
    public static final Type<Point> VECTOR3D = new NetworkBufferTypeImpl.Vector3DType();
    public static final Type<Point> VECTOR3B = new NetworkBufferTypeImpl.Vector3BType();
    public static final Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();

    public static final Type<@Nullable Component> OPT_CHAT = COMPONENT.optional();
    public static final Type<@Nullable Point> OPT_BLOCK_POSITION = BLOCK_POSITION.optional();
    public static final Type<@Nullable UUID> OPT_UUID = UUID.optional();

    public static final Type<Direction> DIRECTION = Enum(Direction.class);
    public static final Type<EntityPose> POSE = Enum(EntityPose.class);

    // Combinators

    public static <E extends Enum<E>> @NotNull Type<E> Enum(@NotNull Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return VAR_INT.transform(integer -> values[integer], Enum::ordinal);
    }

    public static <E extends Enum<E>> @NotNull Type<EnumSet<E>> EnumSet(@NotNull Class<E> enumClass) {
        return new NetworkBufferTypeImpl.EnumSetType<>(enumClass, enumClass.getEnumConstants());
    }

    public static @NotNull Type<BitSet> FixedBitSet(int length) {
        return new NetworkBufferTypeImpl.FixedBitSetType(length);
    }

    public static @NotNull Type<byte[]> FixedRawBytes(int length) {
        return new NetworkBufferTypeImpl.RawBytesType(length);
    }

    public static <T> @NotNull Type<T> Lazy(@NotNull Supplier<@NotNull Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }

    ByteBuffer nioBuffer;
    int readIndex, writeIndex;

    BinaryTagWriter nbtWriter;
    BinaryTagReader nbtReader;

    final @Nullable ResizeStrategy resizeStrategy;
    final @Nullable Registries registries;

    NetworkBuffer(@NotNull ByteBuffer buffer,
                  @Nullable ResizeStrategy resizeStrategy,
                  @Nullable Registries registries) {
        this.nioBuffer = buffer.order(ByteOrder.BIG_ENDIAN);
        this.resizeStrategy = resizeStrategy;
        this.registries = registries;

        this.writeIndex = buffer.position();
        this.readIndex = buffer.position();
    }

    public <T> void write(@NotNull Type<T> type, @UnknownNullability T value) {
        type.write(this, value);
    }

    public <T> @UnknownNullability T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    public void copyTo(int srcOffset, byte @NotNull [] dest, int destOffset, int length) {
        this.nioBuffer.get(srcOffset, dest, destOffset, length);
    }

    public byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor) {
        final int startingPosition = readIndex();
        extractor.accept(this);
        final int endingPosition = readIndex();
        byte[] output = new byte[endingPosition - startingPosition];
        copyTo(startingPosition, output, 0, output.length);
        return output;
    }

    public void clear() {
        this.writeIndex = 0;
        this.readIndex = 0;
    }

    public int writeIndex() {
        return writeIndex;
    }

    public int readIndex() {
        return readIndex;
    }

    public void writeIndex(int writeIndex) {
        this.writeIndex = writeIndex;
    }

    public void readIndex(int readIndex) {
        this.readIndex = readIndex;
    }

    public int skipWrite(int length) {
        final int oldWriteIndex = writeIndex;
        writeIndex += length;
        return oldWriteIndex;
    }

    public int readableBytes() {
        return writeIndex - readIndex;
    }

    void ensureSize(int length) {
        final ResizeStrategy strategy = this.resizeStrategy;
        if (strategy == null) return;

        final long capacity = nioBuffer.capacity();
        final long targetSize = writeIndex + length;
        if (capacity >= targetSize) return;

        final long newCapacity = strategy.resize(capacity, targetSize);
        // Check if long is within the bounds of an int
        if (newCapacity > Integer.MAX_VALUE) {
            throw new RuntimeException("Buffer size is too large, harass maintainers for `MemorySegment` support");
        }

        ByteBuffer newBuffer = ByteBuffer.allocateDirect((int) newCapacity);
        nioBuffer.position(0);
        newBuffer.put(nioBuffer);
        nioBuffer = newBuffer.clear();
    }

    public interface Type<T> {
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

    public static @NotNull Builder builder(int size) {
        return new NetworkBufferImpl.Builder(size);
    }

    public static @NotNull NetworkBuffer staticBuffer(int size, Registries registries) {
        return builder(size).registry(registries).build();
    }

    public static @NotNull NetworkBuffer staticBuffer(int size) {
        return staticBuffer(size, null);
    }

    public static @NotNull NetworkBuffer resizableBuffer(int initialSize, Registries registries) {
        return builder(initialSize)
                .resizeStrategy(ResizeStrategy.DOUBLE)
                .registry(registries)
                .build();
    }

    public static @NotNull NetworkBuffer resizableBuffer(int initialSize) {
        return resizableBuffer(initialSize, null);
    }

    public static @NotNull NetworkBuffer resizableBuffer() {
        return resizableBuffer(256);
    }

    public static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes, @Nullable Registries registries) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new NetworkBuffer(buffer, null, registries);
    }

    public static @NotNull NetworkBuffer wrap(byte @NotNull [] bytes) {
        return wrap(bytes, null);
    }

    public static NetworkBuffer wrap(@NotNull ByteBuffer buffer, @Nullable Registries registries) {
        return new NetworkBuffer(buffer, null, registries);
    }

    public static NetworkBuffer wrap(@NotNull ByteBuffer buffer) {
        return wrap(buffer, null);
    }

    public sealed interface Builder permits NetworkBufferImpl.Builder {
        @NotNull Builder resizeStrategy(@Nullable ResizeStrategy resizeStrategy);

        @NotNull Builder registry(@Nullable Registries registries);

        @NotNull NetworkBuffer build();
    }

    @FunctionalInterface
    public interface ResizeStrategy {
        ResizeStrategy DOUBLE = (capacity, targetSize) -> Math.max(capacity * 2, targetSize);

        long resize(long capacity, long targetSize);
    }

    public static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing, @Nullable Registries registries) {
        NetworkBuffer writer = resizableBuffer(256, registries);
        writing.accept(writer);
        byte[] bytes = new byte[writer.writeIndex];
        writer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
    }

    public static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing) {
        return makeArray(writing, null);
    }

    public static <T> byte[] makeArray(@NotNull Type<T> type, @NotNull T value, @Nullable Registries registries) {
        return makeArray(buffer -> buffer.write(type, value), registries);
    }

    public static <T> byte[] makeArray(@NotNull Type<T> type, @NotNull T value) {
        return makeArray(type, value, null);
    }
}
