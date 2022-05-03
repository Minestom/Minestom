package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class used to read from a byte array.
 * <p>
 * WARNING: not thread-safe.
 */
public class BinaryReader extends InputStream {
    private final ByteBuffer buffer;
    private NBTReader nbtReader = null;

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
        try {
            this.buffer.get(bytes);
        } catch (BufferUnderflowException e) {
            throw new RuntimeException("Could not read " + length + ", " + buffer.remaining() + " remaining.");
        }
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

    public byte[] readByteArray() {
        return readBytes(readVarInt());
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

    public long[] readVarLongArray() {
        final int size = readVarInt();
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            array[i] = readVarLong();
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

    public <T> List<T> readVarIntList(@NotNull Function<BinaryReader, T> supplier) {
        return readList(readVarInt(), supplier);
    }

    public <T> List<T> readByteList(@NotNull Function<BinaryReader, T> supplier) {
        return readList(readByte(), supplier);
    }

    private <T> List<T> readList(int length, @NotNull Function<BinaryReader, T> supplier) {
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(supplier.apply(this));
        }
        return list;
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

    public NBT readTag() {
        NBTReader reader = this.nbtReader;
        if (reader == null) {
            reader = new NBTReader(this, CompressedProcesser.NONE);
            this.nbtReader = reader;
        }
        try {
            return reader.read();
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new RuntimeException();
        }
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
