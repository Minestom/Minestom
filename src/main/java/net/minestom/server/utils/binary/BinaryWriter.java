package net.minestom.server.utils.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class used to write to a byte array.
 * WARNING: not thread-safe.
 */
public class BinaryWriter extends OutputStream {

    private ByteBuf buffer;
    private NBTWriter nbtWriter; // Lazily initialized

    /**
     * Creates a {@link BinaryWriter} using a heap buffer with a custom initial capacity.
     *
     * @param initialCapacity the initial capacity of the binary writer
     */
    public BinaryWriter(int initialCapacity) {
        this.buffer = Unpooled.buffer(initialCapacity);
    }

    /**
     * Creates a {@link BinaryWriter} from multiple a single buffer.
     *
     * @param buffer the writer buffer
     */
    public BinaryWriter(@NotNull ByteBuf buffer) {
        this.buffer = buffer;
    }

    /**
     * Creates a {@link BinaryWriter} from multiple buffers.
     *
     * @param buffers the buffers making this
     */
    public BinaryWriter(@NotNull ByteBuf... buffers) {
        this.buffer = Unpooled.wrappedBuffer(buffers);
    }

    /**
     * Creates a {@link BinaryWriter} with a "reasonably small initial capacity".
     */
    public BinaryWriter() {
        this.buffer = Unpooled.buffer();
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
     * Writes a single boolean to the buffer.
     *
     * @param b the boolean to write
     */
    public void writeBoolean(boolean b) {
        buffer.writeBoolean(b);
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param b the byte to write
     */
    public void writeByte(byte b) {
        buffer.writeByte(b);
    }

    /**
     * Writes a single char to the buffer.
     *
     * @param c the char to write
     */
    public void writeChar(char c) {
        buffer.writeChar(c);
    }

    /**
     * Writes a single short to the buffer.
     *
     * @param s the short to write
     */
    public void writeShort(short s) {
        buffer.writeShort(s);
    }

    /**
     * Writes a single int to the buffer.
     *
     * @param i the int to write
     */
    public void writeInt(int i) {
        buffer.writeInt(i);
    }

    /**
     * Writes a single long to the buffer.
     *
     * @param l the long to write
     */
    public void writeLong(long l) {
        buffer.writeLong(l);
    }

    /**
     * Writes a single float to the buffer.
     *
     * @param f the float to write
     */
    public void writeFloat(float f) {
        buffer.writeFloat(f);
    }

    /**
     * Writes a single double to the buffer.
     *
     * @param d the double to write
     */
    public void writeDouble(double d) {
        buffer.writeDouble(d);
    }

    /**
     * Writes a single var-int to the buffer.
     *
     * @param i the int to write
     */
    public void writeVarInt(int i) {
        Utils.writeVarInt(buffer, i);
    }

    /**
     * Writes a single var-long to the buffer.
     *
     * @param l the long to write
     */
    public void writeVarLong(long l) {
        Utils.writeVarLong(this, l);
    }

    /**
     * Writes a string to the buffer.
     * <p>
     * The size is a var-int type.
     *
     * @param string the string to write
     */
    public void writeSizedString(@NotNull String string) {
        final int utf8Bytes = ByteBufUtil.utf8Bytes(string);
        writeVarInt(utf8Bytes);
        buffer.writeCharSequence(string, StandardCharsets.UTF_8);
    }

    /**
     * Writes a null terminated string to the buffer. This method adds the null character
     * to the end of the string before writing.
     *
     * @param string  the string to write
     * @param charset the charset to encode in
     */
    public void writeNullTerminatedString(@NotNull String string, @NotNull Charset charset) {
        buffer.writeCharSequence(string + '\0', charset);
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

    /**
     * Writes a byte array.
     * <p>
     * WARNING: it doesn't write the length of {@code bytes}.
     *
     * @param bytes the byte array to write
     */
    public void writeBytes(@NotNull byte[] bytes) {
        buffer.writeBytes(bytes);
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
            this.nbtWriter = new NBTWriter(this, false);
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

    public void write(@NotNull BinaryWriter writer) {
        this.buffer.writeBytes(writer.getBuffer());
    }

    public void write(@NotNull ByteBuf buffer) {
        this.buffer.writeBytes(buffer);
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

    /**
     * Converts the internal buffer to a byte array.
     *
     * @return the byte array containing all the {@link BinaryWriter} data
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[buffer.readableBytes()];
        final int readerIndex = buffer.readerIndex();
        buffer.getBytes(readerIndex, bytes);
        return bytes;
    }

    /**
     * Adds a {@link BinaryWriter}'s {@link ByteBuf} at the beginning of this writer.
     *
     * @param headerWriter the {@link BinaryWriter} to add at the beginning
     */
    public void writeAtStart(@NotNull BinaryWriter headerWriter) {
        // Get the buffer of the header
        final ByteBuf headerBuf = headerWriter.getBuffer();
        // Merge both the headerBuf and this buffer
        final ByteBuf finalBuffer = Unpooled.wrappedBuffer(headerBuf, buffer);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    /**
     * Adds a {@link BinaryWriter}'s {@link ByteBuf} at the end of this writer.
     *
     * @param footerWriter the {@link BinaryWriter} to add at the end
     */
    public void writeAtEnd(@NotNull BinaryWriter footerWriter) {
        // Get the buffer of the footer
        final ByteBuf footerBuf = footerWriter.getBuffer();
        // Merge both this buffer and the footerBuf
        final ByteBuf finalBuffer = Unpooled.wrappedBuffer(buffer, footerBuf);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    /**
     * Gets the raw buffer used by this binary writer.
     *
     * @return the raw buffer
     */
    public @NotNull ByteBuf getBuffer() {
        return buffer;
    }

    /**
     * Changes the buffer used by this binary writer.
     *
     * @param buffer the new buffer used by this binary writer
     */
    public void setBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) {
        writeByte((byte) b);
    }

    public void writeUnsignedShort(int yourShort) {
        buffer.writeShort(yourShort & 0xFFFF);
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
