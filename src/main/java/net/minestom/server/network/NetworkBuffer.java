package net.minestom.server.network;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.UUID;

public final class NetworkBuffer {
    public static final Type<Boolean> BOOLEAN = NetworkBufferTypes.BOOLEAN;
    public static final Type<Byte> BYTE = NetworkBufferTypes.BYTE;
    public static final Type<Short> SHORT = NetworkBufferTypes.SHORT;
    public static final Type<Integer> INT = NetworkBufferTypes.INT;
    public static final Type<Long> LONG = NetworkBufferTypes.LONG;
    public static final Type<Float> FLOAT = NetworkBufferTypes.FLOAT;
    public static final Type<Double> DOUBLE = NetworkBufferTypes.DOUBLE;
    public static final Type<Integer> VAR_INT = NetworkBufferTypes.VAR_INT;
    public static final Type<Long> VAR_LONG = NetworkBufferTypes.VAR_LONG;
    public static final Type<byte[]> RAW_BYTES = NetworkBufferTypes.RAW_BYTES;
    public static final Type<String> STRING = NetworkBufferTypes.STRING;
    public static final Type<NBT> NBT = NetworkBufferTypes.NBT;
    public static final Type<Component> COMPONENT = NetworkBufferTypes.COMPONENT;
    public static final Type<UUID> UUID = NetworkBufferTypes.UUID;
    public static final Type<ItemStack> ITEM = NetworkBufferTypes.ITEM;

    final ByteBuffer nioBuffer;
    int writeIndex;
    int readIndex;

    public NetworkBuffer(int initialCapacity) {
        this.nioBuffer = ByteBuffer.allocateDirect(initialCapacity).order(ByteOrder.BIG_ENDIAN);
    }

    public NetworkBuffer() {
        this(1024);
    }

    public <T> void write(@NotNull Type<T> type, @NotNull T value) {
        var impl = (NetworkBufferTypes.TypeImpl<T>) type;
        final long length = impl.writer().write(this, value);
        if (length != -1) this.writeIndex += length;
        assert nioBuffer.position() == 0 : "NIO buffer position is not 0: " + nioBuffer.position();
    }

    public <T> @NotNull T read(@NotNull Type<T> type) {
        var impl = (NetworkBufferTypes.TypeImpl<T>) type;
        return impl.reader().read(this);
    }

    public <T> void writeOptional(@NotNull Type<T> type, @Nullable T value) {
        write(BOOLEAN, value != null);
        if (value != null) write(type, value);
    }

    public <T> @Nullable T readOptional(@NotNull Type<T> type) {
        return read(BOOLEAN) ? read(type) : null;
    }

    public <T> void writeCollection(@NotNull Type<T> type, @NotNull Collection<@NotNull T> values) {
        write(VAR_INT, values.size());
        for (T value : values) {
            write(type, value);
        }
    }

    public <T> @NotNull Collection<@NotNull T> readCollection(@NotNull Type<T> type) {
        final int size = read(VAR_INT);
        final Collection<T> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(read(type));
        }
        return values;
    }

    public void copyTo(int srcOffset, byte @NotNull [] dest, int destOffset, int length) {
        this.nioBuffer.get(srcOffset, dest, destOffset, length);
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

    int readableBytes() {
        return writeIndex - readIndex;
    }

    public sealed interface Type<T> permits NetworkBufferTypes.TypeImpl {
    }
}
