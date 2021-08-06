package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Class used to read from a byte array.
 * <p>
 * WARNING: not thread-safe.
 */
public class BinaryReader extends InputStream {
    private final ByteBuffer buffer;
    private final NBTReader nbtReader = new NBTReader(this, false);

    public BinaryReader(@NotNull ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public BinaryReader(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public int readVarInt() {
        return Utils.readVarInt(buffer);
    }

    public long readVarLong() {
        return Utils.readVarLong(buffer);
    }

    public boolean readBoolean() {
        return buffer.get() == 1;
    }

    public byte readByte() {
        return buffer.get();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public char readChar() {
        return buffer.getChar();
    }

    public int readUnsignedShort() {
        return buffer.getShort() & 0xFFFF;
    }

    /**
     * Same as readInt
     */
    public int readInteger() {
        return buffer.getInt();
    }

    /**
     * Same as readInteger, created for parity with BinaryWriter
     */
    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
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
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        final String str = new String(bytes, StandardCharsets.UTF_8);
        Check.stateCondition(str.length() > maxLength,
                "String length ({0}) was higher than the max length of {1}", length, maxLength);
        return str;
    }

    public String readSizedString() {
        return readSizedString(Integer.MAX_VALUE);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
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
        return SerializerUtils.longToBlockPosition(buffer.getLong());
    }

    public UUID readUuid() {
        return new UUID(readLong(), readLong());
    }

    public Component readComponent(int maxLength) {
        final String jsonObject = readSizedString(maxLength);
        return GsonComponentSerializer.gson().deserialize(jsonObject);
    }

    public Component readComponent() {
        return readComponent(Integer.MAX_VALUE);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int read() {
        return readByte() & 0xFF;
    }

    @Override
    public int available() {
        return buffer.remaining();
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
        int startingPosition = buffer.position();
        extractor.run();
        int endingPosition = getBuffer().position();
        byte[] output = new byte[endingPosition - startingPosition];
        buffer.get(output, 0, output.length);
        //buffer.get(startingPosition, output);
        return output;
    }
}
