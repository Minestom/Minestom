package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Class used to write to a byte array.
 * WARNING: not thread-safe.
 */
public class BinaryWriter extends OutputStream {
    private final NetworkBuffer buffer;

    public BinaryWriter(@NotNull NetworkBuffer buffer) {
        this.buffer = buffer;
    }

    private BinaryWriter(ByteBuffer buffer, boolean resizable) {
        this.buffer = new NetworkBuffer(buffer, resizable);
    }

    public BinaryWriter(@NotNull ByteBuffer buffer) {
        this.buffer = new NetworkBuffer(buffer);
    }

    public BinaryWriter(int initialCapacity) {
        this(ByteBuffer.allocate(initialCapacity));
    }

    public BinaryWriter() {
        this(255);
    }

    @ApiStatus.Experimental
    public static BinaryWriter view(ByteBuffer buffer) {
        return new BinaryWriter(buffer, false);
    }

    public void writeComponent(@NotNull Component component) {
        this.buffer.write(COMPONENT, component);
    }

    public void writeByte(byte b) {
        this.buffer.write(BYTE, b);
    }

    public void writeBoolean(boolean b) {
        this.buffer.write(BOOLEAN, b);
    }

    public void writeShort(short s) {
        this.buffer.write(SHORT, s);
    }

    public void writeInt(int i) {
        this.buffer.write(INT, i);
    }

    public void writeLong(long l) {
        this.buffer.write(LONG, l);
    }

    public void writeFloat(float f) {
        this.buffer.write(FLOAT, f);
    }

    public void writeDouble(double d) {
        this.buffer.write(DOUBLE, d);
    }

    public void writeVarInt(int i) {
        this.buffer.write(VAR_INT, i);
    }

    public void writeVarLong(long l) {
        this.buffer.write(VAR_LONG, l);
    }

    public void writeSizedString(@NotNull String string) {
        this.buffer.write(STRING, string);
    }

    public void writeNullTerminatedString(@NotNull String string, @NotNull Charset charset) {
        final var bytes = (string + '\0').getBytes(charset);
        writeBytes(bytes);
    }

    public void writeVarIntArray(int[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (int element : array) {
            writeVarInt(element);
        }
    }

    public void writeVarLongArray(long[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (long element : array) {
            writeVarLong(element);
        }
    }

    public void writeLongArray(long[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (long element : array) {
            writeLong(element);
        }
    }

    public void writeByteArray(byte[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        writeBytes(array);
    }

    public void writeBytes(byte @NotNull [] bytes) {
        this.buffer.write(RAW_BYTES, bytes);
    }

    public void writeStringArray(@NotNull String[] array) {
        this.buffer.writeCollection(STRING, array);
    }

    public void writeUuid(@NotNull UUID uuid) {
        this.buffer.write(UUID, uuid);
    }

    public void writeBlockPosition(@NotNull Point point) {
        writeBlockPosition(point.blockX(), point.blockY(), point.blockZ());
    }

    public void writeBlockPosition(int x, int y, int z) {
        this.buffer.write(BLOCK_POSITION, new Vec(x, y, z));
    }

    public void writeItemStack(@NotNull ItemStack itemStack) {
        this.buffer.write(ITEM, itemStack);
    }

    public void writeNBT(@NotNull String name, @NotNull NBT tag) {
        this.buffer.write(NBT, tag);
    }

    public <L, R> void writeEither(Either<L, R> either, BiConsumer<BinaryWriter, L> leftWriter, BiConsumer<BinaryWriter, R> rightWriter) {
        if (either.isLeft()) {
            writeBoolean(true);
            leftWriter.accept(this, either.left());
        } else {
            writeBoolean(false);
            rightWriter.accept(this, either.right());
        }
    }

    /**
     * Writes the given writeable object into this writer.
     *
     * @param writeable the object to write
     */
    public void write(@NotNull Writeable writeable) {
        writeable.write(this);
    }

    public void write(@NotNull ByteBuffer buffer) {
        byte[] remaining = new byte[buffer.remaining()];
        buffer.get(remaining);
        writeBytes(remaining);
    }

    public void write(@NotNull BinaryWriter writer) {
        writeBytes(writer.toByteArray());
    }

    /**
     * Writes an array of writeable objects to this writer. Will prepend the binary stream with a var int to denote the
     * length of the array.
     *
     * @param writeables the array of writeables to write
     */
    public void writeArray(@NotNull Writeable[] writeables) {
        writeVarInt(writeables.length);
        for (Writeable w : writeables) {
            write(w);
        }
    }

    public <T> void writeVarIntList(Collection<T> list, @NotNull BiConsumer<BinaryWriter, T> consumer) {
        writeVarInt(list.size());
        writeList(list, consumer);
    }

    public <T> void writeByteList(Collection<T> list, @NotNull BiConsumer<BinaryWriter, T> consumer) {
        writeByte((byte) list.size());
        writeList(list, consumer);
    }

    private <T> void writeList(Collection<T> list, @NotNull BiConsumer<BinaryWriter, T> consumer) {
        for (T t : list) consumer.accept(this, t);
    }

    /**
     * Converts the internal buffer to a byte array.
     *
     * @return the byte array containing all the {@link BinaryWriter} data
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[buffer.writeIndex()];
        this.buffer.copyTo(0, bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    public void write(int b) {
        writeByte((byte) b);
    }

    public void writeUnsignedShort(int yourShort) {
        this.buffer.write(SHORT, (short) (yourShort & 0xFFFF));
    }

    /**
     * Returns a byte[] with the contents written via BinaryWriter
     */
    public static byte[] makeArray(@NotNull Consumer<@NotNull BinaryWriter> writing) {
        BinaryWriter writer = new BinaryWriter();
        writing.accept(writer);
        return writer.toByteArray();
    }
}
