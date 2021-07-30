package net.minestom.server.utils.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Class used to read from a byte array.
 * <p>
 * WARNING: not thread-safe.
 */
public class BinaryReader extends InputStream {

    private final ByteBuf buffer;
    private final NBTReader nbtReader = new NBTReader(this, false);

    public BinaryReader(@NotNull ByteBuf buffer) {
        this.buffer = buffer;
    }

    public BinaryReader(byte[] bytes) {
        this(Unpooled.wrappedBuffer(bytes));
    }

    public int readVarInt() {
        return Utils.readVarInt(buffer);
    }

    public long readVarLong() {
        return Utils.readVarLong(buffer);
    }

    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    public byte readByte() {
        return buffer.readByte();
    }

    public short readShort() {
        return buffer.readShort();
    }

    public char readChar() {
        return buffer.readChar();
    }

    public int readUnsignedShort() {
        return buffer.readUnsignedShort();
    }

    /**
     * Same as readInt
     */
    public int readInteger() {
        return buffer.readInt();
    }

    /**
     * Same as readInteger, created for parity with BinaryWriter
     */
    public int readInt() {
        return buffer.readInt();
    }

    public long readLong() {
        return buffer.readLong();
    }

    public float readFloat() {
        return buffer.readFloat();
    }

    public double readDouble() {
        return buffer.readDouble();
    }

    /**
     * Reads a string size by a var-int.
     * <p>
     * If the string length is higher than {@code maxLength},
     * the code throws an exception and the string bytes are not read.
     *
     * @param maxLength the max length of the string
     * @return the string
     * @throws IllegalStateException if the string length is invalid or higher than {@code maxLength}
     */
    public String readSizedString(int maxLength) {
        final int length = readVarInt();
        Check.stateCondition(!buffer.isReadable(length),
                "Trying to read a string that is too long (wanted {0}, only have {1})",
                length,
                buffer.readableBytes());
        final String str = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.skipBytes(length);
        Check.stateCondition(str.length() > maxLength,
                "String length ({0}) was higher than the max length of {1}", length, maxLength);
        return str;
    }

    public String readSizedString() {
        return readSizedString(Integer.MAX_VALUE);
    }

    public byte[] readBytes(int length) {
        ByteBuf buf = buffer.readBytes(length);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return bytes;
    }

    public String[] readSizedStringArray(int maxLength) {
        final int size = readVarInt();
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = readSizedString(maxLength);
        }
        return strings;
    }

    public String[] readSizedStringArray() {
        return readSizedStringArray(Integer.MAX_VALUE);
    }

    public int[] readVarIntArray() {
        final int size = readVarInt();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = readVarInt();
        }
        return array;
    }

    public long[] readLongArray() {
        final int size = readVarInt();
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            array[i] = readLong();
        }
        return array;
    }

    public byte[] readRemainingBytes() {
        return readBytes(available());
    }

    public Point readBlockPosition() {
        final long value = buffer.readLong();
        return SerializerUtils.longToBlockPosition(value);
    }

    public UUID readUuid() {
        final long most = readLong();
        final long least = readLong();
        return new UUID(most, least);
    }

    /**
     * Tries to read an {@link ItemStack}.
     *
     * @return the read item
     * @throws NullPointerException if the item could not get read
     */
    public ItemStack readItemStack() {
        final ItemStack itemStack = NBTUtils.readItemStack(this);
        Check.notNull(itemStack, "#readSlot returned null, probably because the buffer was corrupted");
        return itemStack;
    }

    public Component readComponent(int maxLength) {
        final String jsonObject = readSizedString(maxLength);
        return GsonComponentSerializer.gson().deserialize(jsonObject);
    }

    public Component readComponent() {
        return readComponent(Integer.MAX_VALUE);
    }

    /**
     * Creates a new object from the given supplier and calls its {@link Readable#read(BinaryReader)} method with this reader.
     *
     * @param supplier supplier to create new instances of your object
     * @param <T>      the readable object type
     * @return the read object
     */
    public <T extends Readable> T read(@NotNull Supplier<@NotNull T> supplier) {
        T result = supplier.get();
        result.read(this);
        return result;
    }

    /**
     * Reads the length of the array to read as a varint, creates the array to contain the readable objects and call
     * their respective {@link Readable#read(BinaryReader)} methods.
     *
     * @param supplier supplier to create new instances of your object
     * @param <T>      the readable object type
     * @return the read objects
     */
    public <T extends Readable> @NotNull T[] readArray(@NotNull Supplier<@NotNull T> supplier) {
        Readable[] result = new Readable[readVarInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = supplier.get();
            result[i].read(this);
        }
        return (T[]) result;
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    @Override
    public int read() {
        return readByte() & 0xFF;
    }

    @Override
    public int available() {
        return buffer.readableBytes();
    }

    public NBT readTag() throws IOException, NBTException {
        return nbtReader.read();
    }

    /**
     * Records the current position, runs the given Runnable, and then returns the bytes between the position before
     * running the runnable and the position after.
     * Can be used to extract a subsection of this reader's buffer with complex data
     *
     * @param extractor the extraction code, simply call the reader's read* methods here.
     */
    public byte[] extractBytes(Runnable extractor) {
        int startingPosition = getBuffer().readerIndex();
        extractor.run();
        int endingPosition = getBuffer().readerIndex();
        byte[] output = new byte[endingPosition - startingPosition];
        getBuffer().getBytes(startingPosition, output);
        return output;
    }
}
