package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.data.DeathLocation;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTReader;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Experimental
public final class NetworkBuffer {
    public static final Type<Boolean> BOOLEAN = NetworkBufferTypes.BOOLEAN;
    public static final Type<Byte> BYTE = NetworkBufferTypes.BYTE;
    public static final Type<Short> SHORT = NetworkBufferTypes.SHORT;
    public static final Type<Integer> UNSIGNED_SHORT = NetworkBufferTypes.UNSIGNED_SHORT;
    public static final Type<Integer> INT = NetworkBufferTypes.INT;
    public static final Type<Long> LONG = NetworkBufferTypes.LONG;
    public static final Type<Float> FLOAT = NetworkBufferTypes.FLOAT;
    public static final Type<Double> DOUBLE = NetworkBufferTypes.DOUBLE;
    public static final Type<Integer> VAR_INT = NetworkBufferTypes.VAR_INT;
    public static final Type<Long> VAR_LONG = NetworkBufferTypes.VAR_LONG;
    public static final Type<byte[]> RAW_BYTES = NetworkBufferTypes.RAW_BYTES;
    public static final Type<String> STRING = NetworkBufferTypes.STRING;
    public static final Type<NBT> NBT = NetworkBufferTypes.NBT;
    public static final Type<Point> BLOCK_POSITION = NetworkBufferTypes.BLOCK_POSITION;
    public static final Type<Component> COMPONENT = NetworkBufferTypes.COMPONENT;
    public static final Type<UUID> UUID = NetworkBufferTypes.UUID;
    public static final Type<ItemStack> ITEM = NetworkBufferTypes.ITEM;

    public static final Type<byte[]> BYTE_ARRAY = NetworkBufferTypes.BYTE_ARRAY;
    public static final Type<long[]> LONG_ARRAY = NetworkBufferTypes.LONG_ARRAY;
    public static final Type<int[]> VAR_INT_ARRAY = NetworkBufferTypes.VAR_INT_ARRAY;
    public static final Type<long[]> VAR_LONG_ARRAY = NetworkBufferTypes.VAR_LONG_ARRAY;

    // METADATA
    public static final Type<Component> OPT_CHAT = NetworkBufferTypes.OPT_CHAT;
    public static final Type<Point> ROTATION = NetworkBufferTypes.ROTATION;
    public static final Type<Point> OPT_BLOCK_POSITION = NetworkBufferTypes.OPT_BLOCK_POSITION;
    public static final Type<Direction> DIRECTION = NetworkBufferTypes.DIRECTION;
    public static final Type<UUID> OPT_UUID = NetworkBufferTypes.OPT_UUID;
    public static final Type<Integer> BLOCK_STATE = NetworkBufferTypes.BLOCK_STATE;
    public static final Type<Integer> OPT_BLOCK_STATE = NetworkBufferTypes.OPT_BLOCK_STATE;
    public static final Type<int[]> VILLAGER_DATA = NetworkBufferTypes.VILLAGER_DATA;
    public static final Type<Integer> OPT_VAR_INT = NetworkBufferTypes.OPT_VAR_INT;
    public static final Type<Entity.Pose> POSE = NetworkBufferTypes.POSE;
    public static final Type<DeathLocation> DEATH_LOCATION = NetworkBufferTypes.DEATH_LOCATION;
    public static final Type<CatMeta.Variant> CAT_VARIANT = NetworkBufferTypes.CAT_VARIANT;
    public static final Type<FrogMeta.Variant> FROG_VARIANT = NetworkBufferTypes.FROG_VARIANT;
    public static final Type<SnifferMeta.State> SNIFFER_STATE = NetworkBufferTypes.SNIFFER_STATE;
    public static final Type<Point> VECTOR3 = NetworkBufferTypes.VECTOR3;
    public static final Type<float[]> QUATERNION = NetworkBufferTypes.QUATERNION;

    ByteBuffer nioBuffer;
    final boolean resizable;
    int writeIndex;
    int readIndex;

    NBTWriter nbtWriter;
    NBTReader nbtReader;

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
        var impl = (NetworkBufferTypes.TypeImpl<T>) type;
        final long length = impl.writer().write(this, value);
        if (length != -1) this.writeIndex += length;
    }

    public <T> void write(@NotNull Writer writer) {
        writer.write(this);
    }

    public <T> @NotNull T read(@NotNull Type<T> type) {
        var impl = (NetworkBufferTypes.TypeImpl<T>) type;
        return impl.reader().read(this);
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
        for (T value : values) {
            write(type, value);
        }
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
        for (T value : values) {
            write(value);
        }
    }

    public <T> void writeCollection(@Nullable Collection<@NotNull T> values,
                                    @NotNull BiConsumer<@NotNull NetworkBuffer, @NotNull T> consumer) {
        if (values == null) {
            write(BYTE, (byte) 0);
            return;
        }
        write(VAR_INT, values.size());
        for (T value : values) {
            consumer.accept(this, value);
        }
    }

    public <T> @NotNull List<@NotNull T> readCollection(@NotNull Type<T> type) {
        final int size = read(VAR_INT);
        final List<T> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(read(type));
        }
        return values;
    }

    public <T> @NotNull List<@NotNull T> readCollection(@NotNull Function<@NotNull NetworkBuffer, @NotNull T> function) {
        final int size = read(VAR_INT);
        final List<T> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(function.apply(this));
        }
        return values;
    }

    public <L, R> void writeEither(Either<L, R> either, BiConsumer<NetworkBuffer, L> leftWriter, BiConsumer<NetworkBuffer, R> rightWriter) {
        if (either.isLeft()) {
            write(BOOLEAN, true);
            leftWriter.accept(this, either.left());
        } else {
            write(BOOLEAN, false);
            rightWriter.accept(this, either.right());
        }
    }

    public <L, R> @NotNull Either<L, R> readEither(@NotNull Function<NetworkBuffer, L> leftReader, Function<NetworkBuffer, R> rightReader) {
        if (read(BOOLEAN)) {
            return Either.left(leftReader.apply(this));
        } else {
            return Either.right(rightReader.apply(this));
        }
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


    public sealed interface Type<T> permits NetworkBufferTypes.TypeImpl {
    }

    @FunctionalInterface
    public interface Writer {
        void write(@NotNull NetworkBuffer writer);
    }

    public static byte[] makeArray(@NotNull Consumer<@NotNull NetworkBuffer> writing) {
        NetworkBuffer writer = new NetworkBuffer();
        writing.accept(writer);
        byte[] bytes = new byte[writer.writeIndex];
        writer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
    }
}
