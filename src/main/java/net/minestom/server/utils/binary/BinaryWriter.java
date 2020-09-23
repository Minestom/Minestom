package net.minestom.server.utils.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class used to write to a byte array
 * WARNING: not thread-safe
 */
public class BinaryWriter extends OutputStream {

    private ByteBuf buffer;
    private final NBTWriter nbtWriter = new NBTWriter(this, false);

    /**
     * Create a {@link BinaryWriter} with a custom initial capacity
     *
     * @param initialCapacity the initial capacity of the binary writer
     */
    public BinaryWriter(int initialCapacity) {
        this.buffer = Unpooled.buffer(initialCapacity);
    }

    /**
     * Create a {@link BinaryWriter} with a "reasonably small initial capacity"
     */
    public BinaryWriter() {
        this.buffer = Unpooled.buffer();
    }

    /**
     * Write a single boolean to the buffer
     *
     * @param b the boolean to write
     */
    public void writeBoolean(boolean b) {
        buffer.writeBoolean(b);
    }

    /**
     * Write a single byte to the buffer
     *
     * @param b the byte to write
     */
    public void writeByte(byte b) {
        buffer.writeByte(b);
    }

    /**
     * Write a single char to the buffer
     *
     * @param c the char to write
     */
    public void writeChar(char c) {
        buffer.writeChar(c);
    }

    /**
     * Write a single short to the buffer
     *
     * @param s the short to write
     */
    public void writeShort(short s) {
        buffer.writeShort(s);
    }

    /**
     * Write a single int to the buffer
     *
     * @param i the int to write
     */
    public void writeInt(int i) {
        buffer.writeInt(i);
    }

    /**
     * Write a single long to the buffer
     *
     * @param l the long to write
     */
    public void writeLong(long l) {
        buffer.writeLong(l);
    }

    /**
     * Write a single float to the buffer
     *
     * @param f the float to write
     */
    public void writeFloat(float f) {
        buffer.writeFloat(f);
    }

    /**
     * Write a single double to the buffer
     *
     * @param d the double to write
     */
    public void writeDouble(double d) {
        buffer.writeDouble(d);
    }

    /**
     * Write a single var-int to the buffer
     *
     * @param i the int to write
     */
    public void writeVarInt(int i) {
        Utils.writeVarInt(this, i);
    }

    /**
     * Write a single var-long to the buffer
     *
     * @param l the long to write
     */
    public void writeVarLong(long l) {
        Utils.writeVarLong(this, l);
    }

    /**
     * Write a string to the buffer
     * <p>
     * The size is a var-int type
     *
     * @param string the string to write
     */
    public void writeSizedString(String string) {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }

    /**
     * Write a string to the buffer
     * <p>
     * The size is a short type
     *
     * @param string the string to write
     */
    public void writeShortSizedString(String string) {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeShort((short) bytes.length);
        writeBytes(bytes);
    }

    /**
     * Write a var-int array to the buffer
     * <p>
     * It is sized by another var-int at the beginning
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

    /**
     * Write a byte array
     *
     * @param bytes the byte array to write
     */
    public void writeBytes(byte[] bytes) {
        buffer.writeBytes(bytes);
    }

    /**
     * Write a string to the buffer
     * <p>
     * The array is sized by a var-int and all strings are wrote using {@link #writeSizedString(String)}
     *
     * @param array the string array to write
     */
    public void writeStringArray(String[] array) {
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
     * Consumer this object to write at a different time
     *
     * @param consumer the writer consumer
     */
    public void write(Consumer<BinaryWriter> consumer) {
        if (consumer != null)
            consumer.accept(this);
    }

    /**
     * Write an {@link UUID}
     * It is done by writing both long, the most and least significant bits
     *
     * @param uuid the {@link UUID} to write
     */
    public void writeUuid(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeBlockPosition(BlockPosition blockPosition) {
        writeBlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public void writeBlockPosition(int x, int y, int z) {
        writeLong(SerializerUtils.positionToLong(x, y, z));
    }

    public void writeItemStack(ItemStack itemStack) {
        NBTUtils.writeItemStack(this, itemStack);
    }

    public void writeNBT(String name, NBT tag) {
        try {
            nbtWriter.writeNamed(name, tag);
        } catch (IOException e) {
            // should not throw, as nbtWriter points to this PacketWriter
            e.printStackTrace();
        }
    }

    /**
     * Convert the internal buffer to a byte array
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
     * Add a {@link BinaryWriter}'s {@link ByteBuf} at the beginning of this writer
     *
     * @param headerWriter the {@link BinaryWriter} to add at the beginning
     */
    public void writeAtStart(BinaryWriter headerWriter) {
        // Get the buffer of the header
        final ByteBuf headerBuf = headerWriter.getBuffer();
        // Merge both the headerBuf and this buffer
        final ByteBuf finalBuffer = Unpooled.wrappedBuffer(headerBuf, buffer);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    /**
     * Add a {@link BinaryWriter}'s {@link ByteBuf} at the end of this writer
     *
     * @param footerWriter the {@link BinaryWriter} to add at the end
     */
    public void writeAtEnd(BinaryWriter footerWriter) {
        // Get the buffer of the footer
        final ByteBuf footerBuf = footerWriter.getBuffer();
        // Merge both this buffer and the footerBuf
        final ByteBuf finalBuffer = Unpooled.wrappedBuffer(buffer, footerBuf);
        // Change the buffer used by this writer
        setBuffer(finalBuffer);
    }

    /**
     * Get the raw buffer used by this binary writer
     *
     * @return the raw buffer
     */
    public ByteBuf getBuffer() {
        return buffer;
    }

    /**
     * Change the buffer used by this binary writer
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
}
