package net.minestom.server.utils.binary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Manages off-heap memory.
 * Not thread-safe.
 */
public final class BinaryBuffer {
    private ByteBuffer nioBuffer; // To become a `MemorySegment` once released
    private NBTReader nbtReader = new NBTReader(new InputStream() {
        @Override
        public int read() {
            return readByte();
        }
    }, false);
    private NBTWriter nbtWriter = new NBTWriter(new OutputStream() {
        @Override
        public void write(int b) {
            // TODO write byte
        }
    }, false);

    private final int capacity;
    private int readerOffset, writerOffset;

    private BinaryBuffer(ByteBuffer buffer) {
        this.nioBuffer = buffer;
        this.capacity = buffer.capacity();
    }

    @ApiStatus.Internal
    public static BinaryBuffer ofSize(int size) {
        return new BinaryBuffer(ByteBuffer.allocateDirect(size));
    }

    @ApiStatus.Internal
    public static BinaryBuffer ofArray(byte[] bytes) {
        return new BinaryBuffer(ByteBuffer.wrap(bytes));
    }

    public static BinaryBuffer copy(BinaryBuffer buffer) {
        final int size = buffer.readableBytes();
        final var temp = ByteBuffer.allocateDirect(size)
                .put(buffer.asByteBuffer(0, size));
        return new BinaryBuffer(temp);
    }

    public void write(ByteBuffer buffer) {
        final int size = buffer.remaining();
        // TODO jdk 13 put with index
        this.nioBuffer.position(writerOffset).put(buffer);
        this.writerOffset += size;
    }

    public void write(BinaryBuffer buffer) {
        write(buffer.asByteBuffer(buffer.readerOffset, buffer.writerOffset));
    }

    public int readVarInt() {
        int value = 0;
        for (int i = 0; i < 5; i++) {
            final int offset = readerOffset + i;
            final byte k = nioBuffer.get(offset);
            value |= (k & 0x7F) << i * 7;
            if ((k & 0x80) != 128) {
                this.readerOffset = offset + 1;
                return value;
            }
        }
        throw new RuntimeException("VarInt is too big");
    }

    public long readVarLong() {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = readByte();
            final long value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);
        return result;
    }

    public byte readByte() {
        return nioBuffer.get(readerOffset++);
    }

    public boolean readBoolean() {
        return readByte() == 1;
    }

    public short readShort() {
        final short value = nioBuffer.getShort(readerOffset);
        this.readerOffset += Short.BYTES;
        return value;
    }

    public char readChar() {
        final char value = nioBuffer.getChar(readerOffset);
        this.readerOffset += Character.BYTES;
        return value;
    }

    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    public int readInt() {
        final int value = nioBuffer.getInt(readerOffset);
        this.readerOffset += Integer.BYTES;
        return value;
    }

    public long readLong() {
        final long value = nioBuffer.getLong(readerOffset);
        this.readerOffset += Long.BYTES;
        return value;
    }

    public float readFloat() {
        final float value = nioBuffer.getFloat(readerOffset);
        this.readerOffset += Float.BYTES;
        return value;
    }

    public double readDouble() {
        final double value = nioBuffer.getDouble(readerOffset);
        this.readerOffset += Double.BYTES;
        return value;
    }

    public String readSizedString(int maxLength) {
        final int length = readVarInt();
        final byte[] bytes = readBytes(length);
        final String str = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("read " + str + " " + length);
        Check.stateCondition(str.length() > maxLength,
                "String length ({0}) was higher than the max length of {1}", length, maxLength);
        return str;
    }

    public String readSizedString() {
        return readSizedString(Integer.MAX_VALUE);
    }

    public Component readComponent(int maxLength) {
        final String jsonObject = readSizedString(maxLength);
        return GsonComponentSerializer.gson().deserialize(jsonObject);
    }

    public Component readComponent() {
        return readComponent(Integer.MAX_VALUE);
    }

    public Point readBlockPosition() {
        return SerializerUtils.longToBlockPosition(readLong());
    }

    public UUID readUuid() {
        return new UUID(readLong(), readLong());
    }

    public NBT readTag() throws IOException, NBTException {
        return nbtReader.read();
    }

    public ItemStack readItemStack() {
        final ItemStack itemStack = NBTUtils.readItemStack(this);
        Check.notNull(itemStack, "#readSlot returned null, probably because the buffer was corrupted");
        return itemStack;
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        this.nioBuffer.position(readerOffset).get(bytes, 0, length);
        this.readerOffset += length;
        return bytes;
    }

    public byte[] readRemainingBytes() {
        return readBytes(readableBytes());
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

    public <T extends Readable> T read(@NotNull Supplier<@NotNull T> supplier) {
        T result = supplier.get();
        result.read(this);
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Readable> @NotNull T[] readArray(@NotNull Supplier<@NotNull T> supplier) {
        Readable[] result = new Readable[readVarInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = supplier.get();
            result[i].read(this);
        }
        return (T[]) result;
    }

    /**
     * Records the current position, runs the given Runnable, and then returns the bytes between the position before
     * running the runnable and the position after.
     * Can be used to extract a subsection of this reader's buffer with complex data
     *
     * @param extractor the extraction code, simply call the reader's read* methods here.
     */
    public byte[] extractBytes(Runnable extractor) {
        final int startingPosition = readerOffset();
        extractor.run();
        final int endingPosition = readerOffset();
        this.readerOffset += endingPosition - startingPosition;
        byte[] output = new byte[endingPosition - startingPosition];
        this.nioBuffer.position(startingPosition).get(output, 0, output.length);
        return output;
    }

    @Contract(pure = true)
    public BinaryBuffer slice(int start, int length) {
        final int end = start + length;
        var slice = new BinaryBuffer(asByteBuffer(start, end));
        slice.readerOffset = start;
        slice.writerOffset = end;
        return slice;
    }

    public @NotNull Marker mark() {
        return new Marker(readerOffset, writerOffset);
    }

    public void reset(int readerOffset, int writerOffset) {
        this.readerOffset = readerOffset;
        this.writerOffset = writerOffset;
    }

    public void reset(@NotNull Marker marker) {
        reset(marker.readerOffset(), marker.writerOffset());
    }

    public boolean canWrite(int size) {
        return writerOffset + size <= capacity;
    }

    public int capacity() {
        return capacity;
    }

    public int readerOffset() {
        return readerOffset;
    }

    public int writerOffset() {
        return writerOffset;
    }

    public int readableBytes() {
        return writerOffset - readerOffset;
    }

    public void clear() {
        this.readerOffset = 0;
        this.writerOffset = 0;
    }

    public ByteBuffer asByteBuffer(int reader, int writer) {
        return nioBuffer.duplicate().position(reader).limit(writer);
    }

    public void writeChannel(WritableByteChannel channel) throws IOException {
        final int count = channel.write(asByteBuffer(readerOffset, writerOffset));
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        this.readerOffset += count;
    }

    public void readChannel(ReadableByteChannel channel) throws IOException {
        final int count = channel.read(asByteBuffer(readerOffset, capacity));
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        this.writerOffset += count;
    }

    @Override
    public String toString() {
        return "BinaryBuffer{" +
                "readerOffset=" + readerOffset +
                ", writerOffset=" + writerOffset +
                ", capacity=" + capacity +
                '}';
    }

    public static final class Marker {
        private final int readerOffset, writerOffset;

        private Marker(int readerOffset, int writerOffset) {
            this.readerOffset = readerOffset;
            this.writerOffset = writerOffset;
        }

        public int readerOffset() {
            return readerOffset;
        }

        public int writerOffset() {
            return writerOffset;
        }

        @Override
        public String toString() {
            return "Marker{" +
                    "readerOffset=" + readerOffset +
                    ", writerOffset=" + writerOffset +
                    '}';
        }
    }
}
