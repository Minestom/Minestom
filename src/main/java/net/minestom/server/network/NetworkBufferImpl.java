package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class NetworkBufferImpl implements NetworkBuffer {
    private ByteBuffer nioBuffer;
    long readIndex, writeIndex;

    BinaryTagWriter nbtWriter;
    BinaryTagReader nbtReader;

    final @Nullable ResizeStrategy resizeStrategy;
    final @Nullable Registries registries;
    boolean readOnly;

    NetworkBufferImpl(@NotNull ByteBuffer buffer,
                      @Nullable ResizeStrategy resizeStrategy,
                      @Nullable Registries registries) {
        this.nioBuffer = buffer.order(ByteOrder.BIG_ENDIAN);
        this.resizeStrategy = resizeStrategy;
        this.registries = registries;

        buffer.limit(buffer.capacity());
    }

    @Override
    public <T> void write(@NotNull Type<T> type, @UnknownNullability T value) {
        assertReadOnly(this);
        type.write(this, value);
    }

    @Override
    public <T> @UnknownNullability T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    @Override
    public <T> void writeAt(long index, @NotNull Type<T> type, @UnknownNullability T value) {
        assertReadOnly(this);
        final long oldWriteIndex = writeIndex;
        writeIndex = index;
        try {
            write(type, value);
        } finally {
            writeIndex = oldWriteIndex;
        }
    }

    @Override
    public <T> @UnknownNullability T readAt(long index, @NotNull Type<T> type) {
        final long oldReadIndex = readIndex;
        readIndex = index;
        try {
            return read(type);
        } finally {
            readIndex = oldReadIndex;
        }
    }

    @Override
    public void copyTo(long srcOffset, byte @NotNull [] dest, long destOffset, long length) {
        assertOverflow(srcOffset + length);
        assertOverflow(destOffset + length);
        this.nioBuffer.get((int) srcOffset, dest, (int) destOffset, (int) length);
    }

    @Override
    public void copyTo(long srcOffset, @NotNull ByteBuffer dest, long destOffset, long length) {
        assertOverflow(srcOffset + length);
        assertOverflow(destOffset + length);
        dest.put((int) destOffset, nioBuffer, (int) srcOffset, (int) length);
    }

    public byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor) {
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        assertOverflow(length);
        byte[] output = new byte[(int) length];
        copyTo(startingPosition, output, 0, output.length);
        return output;
    }

    public @NotNull NetworkBuffer clear() {
        this.writeIndex = 0;
        this.readIndex = 0;
        return this;
    }

    public long writeIndex() {
        return writeIndex;
    }

    public long readIndex() {
        return readIndex;
    }

    public @NotNull NetworkBuffer writeIndex(long writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    public @NotNull NetworkBuffer readIndex(long readIndex) {
        this.readIndex = readIndex;
        return this;
    }

    @Override
    public @NotNull NetworkBuffer index(long readIndex, long writeIndex) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        return this;
    }

    public long advanceWrite(long length) {
        final long oldWriteIndex = writeIndex;
        writeIndex += length;
        return oldWriteIndex;
    }

    @Override
    public long advanceRead(long length) {
        final long oldReadIndex = readIndex;
        readIndex += length;
        return oldReadIndex;
    }

    @Override
    public long readableBytes() {
        return writeIndex - readIndex;
    }

    @Override
    public long writableBytes() {
        return capacity() - writeIndex;
    }

    @Override
    public long capacity() {
        return nioBuffer.capacity();
    }

    @Override
    public void readOnly() {
        this.readOnly = true;
        this.nioBuffer = nioBuffer.asReadOnlyBuffer();
    }

    @Override
    public void resize(long newSize) {
        assertOverflow(newSize);
        ByteBuffer oldBuffer = nioBuffer;
        ByteBuffer newBuffer = ByteBuffer.allocateDirect((int) newSize);
        oldBuffer.position(0);
        newBuffer.put(nioBuffer);
        nioBuffer = newBuffer.clear();
    }

    @Override
    public void ensureWritable(long length) {
        if (writableBytes() >= length) return;
        final long newCapacity = newCapacity(length, capacity());
        assertOverflow(newCapacity);
        resize((int) newCapacity);
    }

    private long newCapacity(long length, long capacity) {
        final long targetSize = writeIndex + length;
        assertOverflow(targetSize);
        final ResizeStrategy strategy = this.resizeStrategy;
        if (strategy == null)
            throw new IndexOutOfBoundsException("Buffer is full and cannot be resized: " + capacity + " -> " + targetSize);
        final long newCapacity = strategy.resize(capacity, targetSize);
        if (newCapacity == capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);
        return newCapacity;
    }

    @Override
    public void compact() {
        assertReadOnly(this);
        nioBuffer.position((int) readIndex);
        nioBuffer.limit((int) writeIndex);
        nioBuffer.compact();
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        NetworkBufferImpl slice = new NetworkBufferImpl(nioBuffer.slice((int) index, (int) length), resizeStrategy, registries);
        slice.readIndex = readIndex;
        slice.writeIndex = writeIndex;
        slice.readOnly = readOnly;
        return slice;
    }

    @Override
    public NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        assertOverflow(length);
        assertOverflow(index + length);
        ByteBuffer payload = ByteBuffer.allocateDirect((int) length);
        payload.put(nioBuffer.slice((int) index, (int) length).duplicate());
        NetworkBufferImpl copy = new NetworkBufferImpl(payload, resizeStrategy, registries);
        copy.readIndex = readIndex;
        copy.writeIndex = writeIndex;
        return copy;
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertReadOnly(this);
        assertOverflow(writeIndex + writableBytes());
        var buffer = nioBuffer.slice((int) writeIndex, (int) writableBytes());
        final int count = channel.read(buffer);
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        advanceWrite(count);
        return count;
    }

    @Override
    public boolean writeChannel(SocketChannel channel) throws IOException {
        assertOverflow(readIndex + readableBytes());
        var buffer = nioBuffer.slice((int) readIndex, (int) readableBytes());
        if (!buffer.hasRemaining())
            return true; // Nothing to write
        final int count = channel.write(buffer);
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        advanceRead(count);
        return !buffer.hasRemaining();
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        assertOverflow(start + length);
        ByteBuffer input = nioBuffer.slice((int) start, (int) length);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(Deflater::new);
    private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(Inflater::new);

    @Override
    public long compress(long start, long length, NetworkBuffer output) {
        assertReadOnly(output);
        assertOverflow(start + length);

        ByteBuffer src = this.nioBuffer;
        ByteBuffer dst = impl(output).nioBuffer;

        ByteBuffer input = src.slice((int) start, (int) length);
        ByteBuffer outputBuffer = dst.slice((int) output.writeIndex(), (int) output.writableBytes());

        Deflater deflater = DEFLATER_POOL.get();
        try {
            deflater.setInput(input);
            deflater.finish();
            final int bytes = deflater.deflate(outputBuffer);
            deflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            DEFLATER_POOL.add(deflater);
        }
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) throws DataFormatException {
        assertReadOnly(output);
        assertOverflow(start + length);

        ByteBuffer src = this.nioBuffer;
        ByteBuffer dst = impl(output).nioBuffer;

        ByteBuffer input = src.slice((int) start, (int) length);
        ByteBuffer outputBuffer = dst.slice((int) output.writeIndex(), (int) output.writableBytes());

        Inflater inflater = INFLATER_POOL.get();
        try {
            inflater.setInput(input);
            final int bytes = inflater.inflate(outputBuffer);
            inflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            INFLATER_POOL.add(inflater);
        }
    }

    @Override
    public String toString() {
        return String.format("NetworkBufferImpl{r%d|w%d->%d, registries=%s, resizeStrategy=%s}",
                readIndex, writeIndex, capacity(), registries != null, resizeStrategy != null);
    }

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        assertOverflow(index + value.length);
        nioBuffer.put((int) index, value, 0, value.length);
    }

    void _getBytes(long index, byte[] value) {
        final int length = value.length;
        assertOverflow(index + length);
        nioBuffer.get((int) index, value, 0, length);
    }

    void _putByte(long index, byte value) {
        assertOverflow(index + Byte.BYTES);
        nioBuffer.put((int) index, value);
    }

    byte _getByte(long index) {
        assertOverflow(index + Byte.BYTES);
        return nioBuffer.get((int) index);
    }

    void _putShort(long index, short value) {
        assertOverflow(index + Short.BYTES);
        nioBuffer.putShort((int) index, value);
    }

    short _getShort(long index) {
        assertOverflow(index + Short.BYTES);
        return nioBuffer.getShort((int) index);
    }

    void _putInt(long index, int value) {
        assertOverflow(index + Integer.BYTES);
        nioBuffer.putInt((int) index, value);
    }

    int _getInt(long index) {
        assertOverflow(index + Integer.BYTES);
        return nioBuffer.getInt((int) index);
    }

    void _putLong(long index, long value) {
        assertOverflow(index + Long.BYTES);
        nioBuffer.putLong((int) index, value);
    }

    long _getLong(long index) {
        assertOverflow(index + Long.BYTES);
        return nioBuffer.getLong((int) index);
    }

    void _putFloat(long index, float value) {
        assertOverflow(index + Float.BYTES);
        nioBuffer.putFloat((int) index, value);
    }

    float _getFloat(long index) {
        assertOverflow(index + Float.BYTES);
        return nioBuffer.getFloat((int) index);
    }

    void _putDouble(long index, double value) {
        assertOverflow(index + Double.BYTES);
        nioBuffer.putDouble((int) index, value);
    }

    double _getDouble(long index) {
        assertOverflow(index + Double.BYTES);
        return nioBuffer.getDouble((int) index);
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        assertReadOnly(dstBuffer);
        assertOverflow(srcOffset + length);
        assertOverflow(dstOffset + length);
        dstBuffer.ensureWritable(dstOffset + length);
        ByteBuffer src = impl(srcBuffer).nioBuffer;
        ByteBuffer dst = impl(dstBuffer).nioBuffer;
        dst.put((int) dstOffset, src, (int) srcOffset, (int) length);
    }

    public static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        ByteBuffer nioBuffer1 = impl(buffer1).nioBuffer.slice(0, (int) buffer1.capacity());
        ByteBuffer nioBuffer2 = impl(buffer2).nioBuffer.slice(0, (int) buffer2.capacity());
        return nioBuffer1.equals(nioBuffer2);
    }

    static void assertReadOnly(NetworkBuffer buffer) {
        if (impl(buffer).readOnly) {
            throw new UnsupportedOperationException("Buffer is read-only");
        }
    }

    static final class Builder implements NetworkBuffer.Builder {
        private final long initialSize;
        private ResizeStrategy resizeStrategy;
        private Registries registries;

        public Builder(long initialSize) {
            this.initialSize = initialSize;
        }

        @Override
        public NetworkBuffer.@NotNull Builder resizeStrategy(@Nullable ResizeStrategy resizeStrategy) {
            this.resizeStrategy = resizeStrategy;
            return this;
        }

        @Override
        public NetworkBuffer.@NotNull Builder registry(Registries registries) {
            this.registries = registries;
            return this;
        }

        @Override
        public @NotNull NetworkBuffer build() {
            assertOverflow(initialSize);
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) initialSize);
            return new NetworkBufferImpl(buffer, resizeStrategy, registries);
        }
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }

    private static void assertOverflow(long value) {
        try {
            Math.toIntExact(value); // Check if long is within the bounds of an int
        } catch (ArithmeticException e) {
            throw new RuntimeException("Buffer size is too large, harass maintainers for `MemorySegment` support");
        }
    }
}
