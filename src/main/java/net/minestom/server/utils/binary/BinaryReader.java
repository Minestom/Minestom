package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Class used to read from a byte array.
 * <p>
 * WARNING: not thread-safe.
 */
public class BinaryReader extends InputStream {
    private final NetworkBuffer buffer;

    public BinaryReader(@NotNull NetworkBuffer buffer) {
        this.buffer = buffer;
    }

    public BinaryReader(@NotNull ByteBuffer buffer) {
        this.buffer = new NetworkBuffer(buffer);
    }

    public BinaryReader(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    public int readVarInt() {
        return buffer.read(VAR_INT);
    }

    public long readVarLong() {
        return buffer.read(VAR_LONG);
    }

    public boolean readBoolean() {
        return buffer.read(BOOLEAN);
    }

    public byte readByte() {
        return buffer.read(BYTE);
    }

    public short readShort() {
        return buffer.read(SHORT);
    }

    public int readUnsignedShort() {
        return buffer.read(SHORT) & 0xFFFF;
    }

    /**
     * Same as readInt
     */
    public int readInteger() {
        return buffer.read(INT);
    }

    /**
     * Same as readInteger, created for parity with BinaryWriter
     */
    public int readInt() {
        return buffer.read(INT);
    }

    public long readLong() {
        return buffer.read(LONG);
    }

    public float readFloat() {
        return buffer.read(FLOAT);
    }

    public double readDouble() {
        return buffer.read(DOUBLE);
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
            for (int i = 0; i < length; i++) {
                bytes[i] = readByte();
            }
        } catch (BufferUnderflowException e) {
            throw new RuntimeException("Could not read " + length + ", " + buffer.readableBytes() + " remaining.");
        }
        final String str = new String(bytes, StandardCharsets.UTF_8);
        Check.stateCondition(str.length() > maxLength,
                "String length ({0}) was higher than the max length of {1}", length, maxLength);
        return str;
    }

    public String readSizedString() {
        return buffer.read(STRING);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = readByte();
        }
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
        return buffer.read(RAW_BYTES);
    }

    public Point readBlockPosition() {
        return buffer.read(BLOCK_POSITION);
    }

    public UUID readUuid() {
        return buffer.read(UUID);
    }

    public ItemStack readItemStack() {
        return buffer.read(ITEM);
    }

    public Component readComponent(int maxLength) {
        final String jsonObject = readSizedString(maxLength);
        return GsonComponentSerializer.gson().deserialize(jsonObject);
    }

    public Component readComponent() {
        return buffer.read(COMPONENT);
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

    public <L, R> Either<L, R> readEither(Function<BinaryReader, L> leftReader, Function<BinaryReader, R> rightReader) {
        if (readBoolean()) {
            return Either.left(leftReader.apply(this));
        } else {
            return Either.right(rightReader.apply(this));
        }
    }

    private <T> List<T> readList(int length, @NotNull Function<BinaryReader, T> supplier) {
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(supplier.apply(this));
        }
        return list;
    }

    @Override
    public int read() {
        return readByte() & 0xFF;
    }

    @Override
    public int available() {
        return buffer.readableBytes();
    }

    public NBT readTag() {
        return buffer.read(NBT);
    }

    /**
     * Records the current position, runs the given Runnable, and then returns the bytes between the position before
     * running the runnable and the position after.
     * Can be used to extract a subsection of this reader's buffer with complex data
     *
     * @param extractor the extraction code, simply call the reader's read* methods here.
     */
    public byte[] extractBytes(Runnable extractor) {
        int startingPosition = buffer.readIndex();
        extractor.run();
        int endingPosition = buffer.readIndex();
        byte[] output = new byte[endingPosition - startingPosition];
        buffer.copyTo(buffer.readIndex(), output, 0, output.length);
        //buffer.get(startingPosition, output);
        return output;
    }
}
