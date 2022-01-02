package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class used to write to a byte array.
 * WARNING: not thread-safe.
 */
public class BinaryWriter extends OutputStream {
    private ByteBuffer buffer;
    private NBTWriter nbtWriter; // Lazily initialized

    private final boolean resizable;

    private BinaryWriter(ByteBuffer buffer, boolean resizable) {
        this.buffer = buffer;
        this.resizable = resizable;
    }

    public BinaryWriter(@NotNull ByteBuffer buffer) {
        this.buffer = buffer;
        this.resizable = true;
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

    protected void ensureSize(int length) {
        if (!resizable) return;
        final int position = buffer.position();
        if (position + length >= buffer.limit()) {
            final int newLength = (position + length) * 4;
            var copy = buffer.isDirect() ?
                    ByteBuffer.allocateDirect(newLength) : ByteBuffer.allocate(newLength);
            copy.put(buffer.flip());
            this.buffer = copy;
        }
    }

    /**
     * Writes a component to the buffer as a sized string.
     *
     * @param component the component
     */
    public void writeComponent(@NotNull Component component) {
        this.writeSizedString(GsonComponentSerializer.gson().serialize(component));
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param b the byte to write
     */
    public void writeByte(byte b) {
        ensureSize(Byte.BYTES);
        buffer.put(b);
    }

    /**
     * Writes a single boolean to the buffer.
     *
     * @param b the boolean to write
     */
    public void writeBoolean(boolean b) {
        writeByte((byte) (b ? 1 : 0));
    }

    /**
     * Writes a single char to the buffer.
     *
     * @param c the char to write
     */
    public void writeChar(char c) {
        ensureSize(Character.BYTES);
        buffer.putChar(c);
    }

    /**
     * Writes a single short to the buffer.
     *
     * @param s the short to write
     */
    public void writeShort(short s) {
        ensureSize(Short.BYTES);
        buffer.putShort(s);
    }

    /**
     * Writes a single int to the buffer.
     *
     * @param i the int to write
     */
    public void writeInt(int i) {
        ensureSize(Integer.BYTES);
        buffer.putInt(i);
    }

    /**
     * Writes a single long to the buffer.
     *
     * @param l the long to write
     */
    public void writeLong(long l) {
        ensureSize(Long.BYTES);
        buffer.putLong(l);
    }

    /**
     * Writes a single float to the buffer.
     *
     * @param f the float to write
     */
    public void writeFloat(float f) {
        ensureSize(Float.BYTES);
        buffer.putFloat(f);
    }

    /**
     * Writes a single double to the buffer.
     *
     * @param d the double to write
     */
    public void writeDouble(double d) {
        ensureSize(Double.BYTES);
        buffer.putDouble(d);
    }

    /**
     * Writes a single var-int to the buffer.
     *
     * @param i the int to write
     */
    public void writeVarInt(int i) {
        ensureSize(5);
        Utils.writeVarInt(buffer, i);
    }

    /**
     * Writes a single var-long to the buffer.
     *
     * @param l the long to write
     */
    public void writeVarLong(long l) {
        ensureSize(10);
        Utils.writeVarLong(buffer, l);
    }

    /**
     * Writes a string to the buffer.
     * <p>
     * The size is a var-int type.
     *
     * @param string the string to write
     */
    public void writeSizedString(@NotNull String string) {
        final var bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }

    /**
     * Writes a null terminated string to the buffer. This method adds the null character
     * to the end of the string before writing.
     *
     * @param string  the string to write
     * @param charset the charset to encode in
     */
    public void writeNullTerminatedString(@NotNull String string, @NotNull Charset charset) {
        final var bytes = (string + '\0').getBytes(charset);
        writeBytes(bytes);
    }

    /**
     * Writes a var-int array to the buffer.
     * <p>
     * It is sized by another var-int at the beginning.
     *
     * @param array the integers to write
     */
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

    /**
     * Writes a byte array.
     * <p>
     * WARNING: it doesn't write the length of {@code bytes}.
     *
     * @param bytes the byte array to write
     */
    public void writeBytes(byte @NotNull [] bytes) {
        if (bytes.length == 0) return;
        ensureSize(bytes.length);
        buffer.put(bytes);
    }

    /**
     * Writes a string to the buffer.
     * <p>
     * The array is sized by a var-int and all strings are wrote using {@link #writeSizedString(String)}.
     *
     * @param array the string array to write
     */
    public void writeStringArray(@NotNull String[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (String element : array) {
            writeSizedString(element);
        }
    }

    /**
     * Writes an {@link UUID}.
     * It is done by writing both long, the most and least significant bits.
     *
     * @param uuid the {@link UUID} to write
     */
    public void writeUuid(@NotNull UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeBlockPosition(@NotNull Point point) {
        writeBlockPosition(point.blockX(), point.blockY(), point.blockZ());
    }

    public void writeBlockPosition(int x, int y, int z) {
        writeLong(SerializerUtils.positionToLong(x, y, z));
    }

    public void writeItemStack(@NotNull ItemStack itemStack) {
        if (itemStack.isAir()) {
            writeBoolean(false);
        } else {
            writeBoolean(true);
            writeVarInt(itemStack.getMaterial().id());
            writeByte((byte) itemStack.getAmount());
            write(itemStack.getMeta());
        }
    }

    public void writeNBT(@NotNull String name, @NotNull NBT tag) {
        if (nbtWriter == null) {
            this.nbtWriter = new NBTWriter(this, CompressedProcesser.NONE);
        }
        try {
            nbtWriter.writeNamed(name, tag);
        } catch (IOException e) {
            // should not throw, as nbtWriter points to this PacketWriter
            MinecraftServer.getExceptionManager().handleException(e);
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
        ensureSize(buffer.remaining());
        this.buffer.put(buffer);
    }

    public void write(@NotNull BinaryWriter writer) {
        write(writer.buffer);
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
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Adds a {@link BinaryWriter}'s {@link ByteBuffer} at the beginning of this writer.
     *
     * @param headerWriter the {@link BinaryWriter} to add at the beginning
     */
    public void writeAtStart(@NotNull BinaryWriter headerWriter) {
        // Get the buffer of the header
        final var headerBuf = headerWriter.getBuffer();
        // Merge both the headerBuf and this buffer
        final var finalBuffer = concat(headerBuf, buffer);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    /**
     * Adds a {@link BinaryWriter}'s {@link ByteBuffer} at the end of this writer.
     *
     * @param footerWriter the {@link BinaryWriter} to add at the end
     */
    public void writeAtEnd(@NotNull BinaryWriter footerWriter) {
        // Get the buffer of the footer
        final var footerBuf = footerWriter.getBuffer();
        // Merge both this buffer and the footerBuf
        final var finalBuffer = concat(buffer, footerBuf);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    public static ByteBuffer concat(final ByteBuffer... buffers) {
        final ByteBuffer combined = ByteBuffer.allocate(Arrays.stream(buffers).mapToInt(Buffer::remaining).sum());
        Arrays.stream(buffers).forEach(b -> combined.put(b.duplicate()));
        return combined;
    }

    /**
     * Gets the raw buffer used by this binary writer.
     *
     * @return the raw buffer
     */
    public @NotNull ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Changes the buffer used by this binary writer.
     *
     * @param buffer the new buffer used by this binary writer
     */
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) {
        writeByte((byte) b);
    }

    public void writeUnsignedShort(int yourShort) {
        // FIXME unsigned
        ensureSize(Short.BYTES);
        buffer.putShort((short) (yourShort & 0xFFFF));
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
