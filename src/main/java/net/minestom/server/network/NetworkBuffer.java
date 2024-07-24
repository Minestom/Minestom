package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public static final Type<byte[]> RAW_BYTES = new NetworkBufferTypeImpl.RawBytesType();
    public static final Type<String> STRING = new NetworkBufferTypeImpl.StringType();
    public static final Type<BinaryTag> NBT = new NetworkBufferTypeImpl.NbtType();
    public static final Type<Point> BLOCK_POSITION = new NetworkBufferTypeImpl.BlockPositionType();
    public static final Type<Component> COMPONENT = new ComponentNetworkBufferTypeImpl();
    public static final Type<Component> JSON_COMPONENT = new NetworkBufferTypeImpl.JsonComponentType();
    public static final Type<UUID> UUID = new NetworkBufferTypeImpl.UUIDType();
    public static final Type<Pos> POS = new NetworkBufferTypeImpl.PosType();

    public static final Type<byte[]> BYTE_ARRAY = new NetworkBufferTypeImpl.ByteArrayType();
    public static final Type<long[]> LONG_ARRAY = new NetworkBufferTypeImpl.LongArrayType();
    public static final Type<int[]> VAR_INT_ARRAY = new NetworkBufferTypeImpl.VarIntArrayType();
    public static final Type<long[]> VAR_LONG_ARRAY = new NetworkBufferTypeImpl.VarLongArrayType();

    public static <T extends ProtocolObject> @NotNull Type<DynamicRegistry.Key<T>> RegistryKey(@NotNull Function<Registries, DynamicRegistry<T>> selector) {
        return new NetworkBufferTypeImpl.RegistryTypeType<>(selector);
    }

    // METADATA
    public static final Type<int[]> VILLAGER_DATA = new NetworkBufferTypeImpl.VillagerDataType();
    public static final Type<Point> VECTOR3 = new NetworkBufferTypeImpl.Vector3Type();
    public static final Type<Point> VECTOR3D = new NetworkBufferTypeImpl.Vector3DType();
    public static final Type<float[]> QUATERNION = new NetworkBufferTypeImpl.QuaternionType();

    public static final Type<@Nullable Component> OPT_CHAT = Optional(COMPONENT);
    public static final Type<@Nullable Point> OPT_BLOCK_POSITION = Optional(BLOCK_POSITION);
    public static final Type<@Nullable UUID> OPT_UUID = Optional(UUID);

    public static final Type<Direction> DIRECTION = new NetworkBufferTypeImpl.EnumType<>(Direction.class);
    public static final Type<EntityPose> POSE = new NetworkBufferTypeImpl.EnumType<>(EntityPose.class);

    // Combinators

    public static <T> @NotNull Type<@Nullable T> Optional(@NotNull Type<T> type) {
        return new NetworkBufferTypeImpl.OptionalType<>(type);
    }

    public static <E extends Enum<E>> @NotNull Type<E> Enum(@NotNull Class<E> enumClass) {
        return new NetworkBufferTypeImpl.EnumType<>(enumClass);
    }

    public static <T> @NotNull Type<T> Lazy(@NotNull Supplier<NetworkBuffer.@NotNull Type<T>> supplier) {
        return new NetworkBufferTypeImpl.LazyType<>(supplier);
    }


    ByteBuffer nioBuffer;
    final boolean resizable;
    int writeIndex;
    int readIndex;

    BinaryTagWriter nbtWriter;
    BinaryTagReader nbtReader;

    // In the future, this should be passed as a parameter.
    final Registries registries = MinecraftServer.process();

    public NetworkBuffer(@NotNull ByteBuffer buffer, boolean resizable) {
        this.nioBuffer = buffer.order(ByteOrder.BIG_ENDIAN);
        this.resizable = resizable;

        this.writeIndex = buffer.position();
        this.readIndex = buffer.position();
    }

    public NetworkBuffer(@NotNull ByteBuffer buffer) {
        this(buffer, true);
    }

    public NetworkBuffer(int initialCapacity) {
        this(ByteBuffer.allocateDirect(initialCapacity), true);
    }

    public NetworkBuffer() {
        this(1024);
    }

    public <T> void write(@NotNull Type<T> type, @NotNull T value) {
        type.write(this, value);
    }

    public <T> void write(@NotNull Writer writer) {
        writer.write(this);
    }

    public <T> @NotNull T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    public <T> void writeOptional(@NotNull Type<T> type, @Nullable T value) {
        write(BOOLEAN, value != null);
        if (value != null) write(type, value);
    }

    public void writeOptional(@Nullable Writer writer) {
        write(BOOLEAN, writer != null);
        if (writer != null) write(writer);
    }

    public <T> @Nullable T readOptional(@NotNull Type<T> type) {
        return read(BOOLEAN) ? read(type) : null;
    }

    public <T> @Nullable T readOptional(@NotNull Function<@NotNull NetworkBuffer, @NotNull T> function) {
        return read(BOOLEAN) ? function.apply(this) : null;
    }

    public <T> void writeCollection(@NotNull Type<T> type, @Nullable Collection<@NotNull T> values) {
        if (values == null) {
            write(BYTE, (byte) 0);
            return;
        }
        write(VAR_INT, values.size());
        for (T value : values) write(type, value);
    }

    @SafeVarargs
    public final <T> void writeCollection(@NotNull Type<T> type, @NotNull T @Nullable ... values) {
        writeCollection(type, values == null ? null : List.of(values));
    }

    public <T extends Writer> void writeCollection(@Nullable Collection<@NotNull T> values) {
        if (values == null) {
            write(BYTE, (byte) 0);
            return;
        }
        write(VAR_INT, values.size());
        for (T value : values) write(value);
    }

    public <T> void writeCollection(@Nullable Collection<@NotNull T> values,
                                    @NotNull BiConsumer<@NotNull NetworkBuffer, @NotNull T> consumer) {
        if (values == null) {
            write(BYTE, (byte) 0);
            return;
        }
        write(VAR_INT, values.size());
        for (T value : values) consumer.accept(this, value);
    }

    public <T> @NotNull List<@NotNull T> readCollection(@NotNull Type<T> type, int maxSize) {
        final int size = read(VAR_INT);
        Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
        final List<T> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) values.add(read(type));
        return values;
    }

    public <T> @NotNull List<@NotNull T> readCollection(@NotNull Function<@NotNull NetworkBuffer, @NotNull T> function, int maxSize) {
        final int size = read(VAR_INT);
        Check.argCondition(size > maxSize, "Collection size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
        final List<T> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) values.add(function.apply(this));
        return values;
    }

    public <K, V> @NotNull Map<K, V> writeMap(@NotNull NetworkBuffer.Type<K> keyType, @NotNull NetworkBuffer.Type<V> valueType, @NotNull Map<K, V> map) {
        write(VAR_INT, map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            write(keyType, entry.getKey());
            write(valueType, entry.getValue());
        }
        return map;
    }

    public <K, V> @NotNull Map<K, V> readMap(@NotNull NetworkBuffer.Type<K> keyType, @NotNull NetworkBuffer.Type<V> valueType, int maxSize) {
        final int size = read(VAR_INT);
        Check.argCondition(size > maxSize, "Map size ({0}) is higher than the maximum allowed size ({1})", size, maxSize);
        final Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(read(keyType), read(valueType));
        }
        return map;
    }

    public <E extends Enum<?>> void writeEnum(@NotNull Class<E> enumClass, @NotNull E value) {
        write(VAR_INT, value.ordinal());
    }

    public <E extends Enum<?>> @NotNull E readEnum(@NotNull Class<@NotNull E> enumClass) {
        return enumClass.getEnumConstants()[read(VAR_INT)];
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> enumType) {
        final E[] values = enumType.getEnumConstants();
        BitSet bitSet = new BitSet(values.length);
        for (int i = 0; i < values.length; ++i) {
            bitSet.set(i, enumSet.contains(values[i]));
        }
        writeFixedBitSet(bitSet, values.length);
    }

    public <E extends Enum<E>> @NotNull EnumSet<E> readEnumSet(Class<E> enumType) {
        final E[] values = enumType.getEnumConstants();
        BitSet bitSet = readFixedBitSet(values.length);
        EnumSet<E> enumSet = EnumSet.noneOf(enumType);
        for (int i = 0; i < values.length; ++i) {
            if (bitSet.get(i)) {
                enumSet.add(values[i]);
            }
        }
        return enumSet;
    }

    public void writeFixedBitSet(BitSet set, int length) {
        final int setLength = set.length();
        if (setLength > length) {
            throw new IllegalArgumentException("BitSet is larger than expected size (" + setLength + ">" + length + ")");
        } else {
            final byte[] array = set.toByteArray();
            write(RAW_BYTES, array);
        }
    }

    @NotNull
    public BitSet readFixedBitSet(int length) {
        final byte[] array = readBytes((length + 7) / 8);
        return BitSet.valueOf(array);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        nioBuffer.get(readIndex, bytes, 0, length);
        readIndex += length;
        return bytes;
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
        if (!resizable) return;
        if (nioBuffer.capacity() < writeIndex + length) {
            final int newCapacity = Math.max(nioBuffer.capacity() * 2, writeIndex + length);
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);
            nioBuffer.position(0);
            newBuffer.put(nioBuffer);
            nioBuffer = newBuffer.clear();
        }
    }


    public interface Type<T> {
        void write(@NotNull NetworkBuffer buffer, T value);

        T read(@NotNull NetworkBuffer buffer);

        default <S> @NotNull Type<S> map(@NotNull Function<T, S> to, @NotNull Function<S, T> from) {
            return new NetworkBufferTypeImpl.MappedType<>(this, to, from);
        }

        default @NotNull Type<List<T>> list(int maxSize) {
            return new NetworkBufferTypeImpl.ListType<>(this, maxSize);
        }

        default @NotNull Type<T> optional() {
            return new NetworkBufferTypeImpl.OptionalTypeImpl<>(this);
        }
    }

    @FunctionalInterface
    public interface Writer {
        void write(@NotNull NetworkBuffer writer);
    }

    @FunctionalInterface
    public interface Reader<T> {
        @NotNull T read(@NotNull NetworkBuffer reader);
    }

    public static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing) {
        NetworkBuffer writer = new NetworkBuffer();
        writing.accept(writer);
        byte[] bytes = new byte[writer.writeIndex];
        writer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
    }
}
