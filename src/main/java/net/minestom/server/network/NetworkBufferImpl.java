package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.nio.ByteOrder.BIG_ENDIAN;

final class NetworkBufferImpl implements NetworkBuffer {
    private static final ValueLayout.OfShort SHORT_LAYOUT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(BIG_ENDIAN);
    private static final ValueLayout.OfInt INT_LAYOUT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(BIG_ENDIAN);
    private static final ValueLayout.OfLong LONG_LAYOUT = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(BIG_ENDIAN);
    private static final ValueLayout.OfFloat FLOAT_LAYOUT = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(BIG_ENDIAN);
    private static final ValueLayout.OfDouble DOUBLE_LAYOUT = ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(BIG_ENDIAN);

    private @UnknownNullability MemorySegment segment; // null for dummy buffers
    private long readIndex, writeIndex;

    private @Nullable BinaryTagWriter nbtWriter;
    private @Nullable BinaryTagReader nbtReader;

    final @Nullable AutoResize autoResize;
    @Nullable Registries registries;

    NetworkBufferImpl(@UnknownNullability MemorySegment segment,
                      long readIndex, long writeIndex,
                      @Nullable AutoResize autoResize,
                      @Nullable Registries registries) {
        this.segment = segment;
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.autoResize = autoResize;
        this.registries = registries;
    }

    @Override
    public <T> void write(Type<T> type, @UnknownNullability T value) {
        assertReadOnly();
        type.write(this, value);
    }

    @Override
    public <T> @UnknownNullability T read(Type<T> type) {
        assertDummy();
        return type.read(this);
    }

    @Override
    public <T> void writeAt(long index, Type<T> type, @UnknownNullability T value) {
        assertReadOnly();
        final long oldWriteIndex = writeIndex;
        writeIndex = index;
        try {
            write(type, value);
        } finally {
            writeIndex = oldWriteIndex;
        }
    }

    @Override
    public <T> @UnknownNullability T readAt(long index, Type<T> type) {
        assertDummy();
        final long oldReadIndex = readIndex;
        readIndex = index;
        try {
            return read(type);
        } finally {
            readIndex = oldReadIndex;
        }
    }

    @Override
    public void copyTo(long srcOffset, byte[] dest, long destOffset, long length) {
        assertDummy();
        assertOverflow(srcOffset + length);
        assertOverflow(destOffset + length);
        if (length == 0) return;
        if (dest.length < destOffset + length) {
            throw new IndexOutOfBoundsException("Destination array is too small: " + dest.length + " < " + (destOffset + length));
        }
        MemorySegment.copy(segment, srcOffset, MemorySegment.ofArray(dest), destOffset, length);
    }

    public byte[] extractBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        assertOverflow(length);
        byte[] output = new byte[(int) length];
        copyTo(startingPosition, output, 0, output.length);
        return output;
    }

    public NetworkBuffer clear() {
        return index(0, 0);
    }

    public long writeIndex() {
        return writeIndex;
    }

    public long readIndex() {
        return readIndex;
    }

    public NetworkBuffer writeIndex(long writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    public NetworkBuffer readIndex(long readIndex) {
        this.readIndex = readIndex;
        return this;
    }

    @Override
    public NetworkBuffer index(long readIndex, long writeIndex) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        return this;
    }

    public long advanceWrite(long length) {
        final long oldWriteIndex = writeIndex;
        writeIndex = oldWriteIndex + length;
        return oldWriteIndex;
    }

    @Override
    public long advanceRead(long length) {
        final long oldReadIndex = readIndex;
        readIndex = oldReadIndex + length;
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
        final MemorySegment segment = this.segment;
        return segment != null ? segment.byteSize() : Long.MAX_VALUE;
    }

    @Override
    public void readOnly() {
        final MemorySegment segment = this.segment;
        if (segment != null) this.segment = segment.asReadOnly();
    }

    @Override
    public boolean isReadOnly() {
        final MemorySegment segment = this.segment;
        return segment != null && segment.isReadOnly();
    }

    @Override
    public void resize(long newSize) {
        assertDummy();
        assertReadOnly();
        final long capacity = capacity();
        if (newSize < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (newSize == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final MemorySegment newSegment = Arena.ofAuto().allocate(newSize);
        MemorySegment.copy(segment, 0, newSegment, 0, capacity);
        this.segment = newSegment;
    }

    @Override
    public void ensureWritable(long length) {
        assertReadOnly();
        if (writableBytes() >= length) return;
        final long newCapacity = newCapacity(length, capacity());
        resize(newCapacity);
    }

    private long newCapacity(long length, long capacity) {
        final long targetSize = writeIndex + length;
        final AutoResize strategy = this.autoResize;
        if (strategy == null)
            throw new IndexOutOfBoundsException("Buffer is full and cannot be resized: " + capacity + " -> " + targetSize);
        final long newCapacity = strategy.resize(capacity, targetSize);
        if (newCapacity == capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);
        return newCapacity;
    }

    @Override
    public void compact() {
        assertDummy();
        assertReadOnly();
        final long readIndex = readIndex();
        if (readIndex == 0) return;
        final MemorySegment segment = this.segment;
        MemorySegment.copy(segment, readIndex, segment, 0, readableBytes());
        this.writeIndex -= readIndex;
        this.readIndex = 0;
    }

    @Override
    public NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        Objects.checkFromIndexSize(index, length, capacity());
        final MemorySegment newSegment = Arena.ofAuto().allocate(length);
        MemorySegment.copy(segment, index, newSegment, 0, length);
        return new NetworkBufferImpl(newSegment, readIndex, writeIndex, autoResize, registries);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertDummy();
        assertReadOnly();
        assertOverflow(writeIndex + writableBytes());
        var buffer = bufferSlice(writeIndex, writableBytes());
        final int count = channel.read(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceWrite(count);
        return count;
    }

    @Override
    public boolean writeChannel(SocketChannel channel) throws IOException {
        assertDummy();
        final long readableBytes = readableBytes();
        if (readableBytes == 0) return true; // Nothing to write
        assertOverflow(readIndex + readableBytes);
        var buffer = bufferSlice(readIndex, readableBytes);
        if (!buffer.hasRemaining())
            return true; // Nothing to write
        final int count = channel.write(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceRead(count);
        return !buffer.hasRemaining();
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        assertDummy();
        assertOverflow(start + length);
        ByteBuffer input = bufferSlice(start, length);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    // Use the JVM lazy loading to ignore these until compression is required.
    static class CompressionHolder {
        private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(Deflater::new);
        private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(Inflater::new);
    }

    @Override
    public long compress(long start, long length, NetworkBuffer output) {
        assertDummy();
        impl(output).assertReadOnly();
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice(start, length);
        ByteBuffer outputBuffer = impl(output).bufferSlice(output.writeIndex(), output.writableBytes());

        Deflater deflater = CompressionHolder.DEFLATER_POOL.get();
        try {
            deflater.setInput(input);
            deflater.finish();
            final int bytes = deflater.deflate(outputBuffer);
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            deflater.reset();
            CompressionHolder.DEFLATER_POOL.add(deflater);
        }
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) throws DataFormatException {
        assertDummy();
        impl(output).assertReadOnly();
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice(start, length);
        ByteBuffer outputBuffer = impl(output).bufferSlice(output.writeIndex(), output.writableBytes());

        Inflater inflater = CompressionHolder.INFLATER_POOL.get();
        try {
            inflater.setInput(input);
            final int bytes = inflater.inflate(outputBuffer);
            if (!inflater.finished()) {
                throw new DataFormatException("Decompressed payload exceeds output capacity");
            }
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            inflater.reset();
            CompressionHolder.INFLATER_POOL.add(inflater);
        }
    }

    @Override
    public @Nullable Registries registries() {
        return registries;
    }

    @Override
    public void registries(@Nullable Registries registries) {
        this.registries = registries;
    }

    private ByteBuffer bufferSlice(long position, long length) {
        return segment.asSlice(position, length).asByteBuffer().order(BIG_ENDIAN);
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity(), registries != null, autoResize != null, isReadOnly());
    }

    private boolean isDummy() {
        return segment == null;
    }

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        if (isDummy()) return;
        assertReadOnly();
        MemorySegment.copy(MemorySegment.ofArray(value), 0, segment, index, value.length);
    }

    void _getBytes(long index, byte[] value) {
        assertDummy();
        MemorySegment.copy(segment, index, MemorySegment.ofArray(value), 0, value.length);
    }

    void _putByte(long index, byte value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(ValueLayout.JAVA_BYTE, index, value);
    }

    byte _getByte(long index) {
        assertDummy();
        return segment.get(ValueLayout.JAVA_BYTE, index);
    }

    void _putShort(long index, short value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(SHORT_LAYOUT, index, value);
    }

    short _getShort(long index) {
        assertDummy();
        return segment.get(SHORT_LAYOUT, index);
    }

    void _putInt(long index, int value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(INT_LAYOUT, index, value);
    }

    int _getInt(long index) {
        assertDummy();
        return segment.get(INT_LAYOUT, index);
    }

    void _putLong(long index, long value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(LONG_LAYOUT, index, value);
    }

    long _getLong(long index) {
        assertDummy();
        return segment.get(LONG_LAYOUT, index);
    }

    void _putFloat(long index, float value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(FLOAT_LAYOUT, index, value);
    }

    float _getFloat(long index) {
        assertDummy();
        return segment.get(FLOAT_LAYOUT, index);
    }

    void _putDouble(long index, double value) {
        if (isDummy()) return;
        assertReadOnly();
        segment.set(DOUBLE_LAYOUT, index, value);
    }

    double _getDouble(long index) {
        assertDummy();
        return segment.get(DOUBLE_LAYOUT, index);
    }

    static NetworkBuffer wrap(byte[] bytes, long readIndex, long writeIndex, @Nullable Registries registries) {
        var buffer = new Builder(bytes.length).registry(registries).build();
        buffer.writeAt(0, NetworkBuffer.RAW_BYTES, bytes);
        buffer.index(readIndex, writeIndex);
        return buffer;
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        var src = impl(srcBuffer);
        var dst = impl(dstBuffer);
        dst.assertReadOnly();
        MemorySegment.copy(src.segment, srcOffset, dst.segment, dstOffset, length);
    }

    public static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        var impl1 = impl(buffer1);
        var impl2 = impl(buffer2);
        if (impl1.capacity() != impl2.capacity()) return false;
        return impl1.segment.mismatch(impl2.segment) == -1;
    }

    void assertReadOnly() {
        if (isReadOnly()) throw new UnsupportedOperationException("Buffer is read-only");
    }

    void assertDummy() {
        if (isDummy()) throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    static final class Builder implements NetworkBuffer.Builder {
        private final long initialSize;
        private @Nullable AutoResize autoResize;
        private @Nullable Registries registries;

        public Builder(long initialSize) {
            this.initialSize = initialSize;
        }

        @Override
        public NetworkBuffer.Builder autoResize(@Nullable AutoResize autoResize) {
            this.autoResize = autoResize;
            return this;
        }

        @Override
        public NetworkBuffer.Builder registry(@Nullable Registries registries) {
            this.registries = registries;
            return this;
        }

        @Override
        public NetworkBuffer build() {
            final MemorySegment segment = Arena.ofAuto().allocate(initialSize);
            return new NetworkBufferImpl(segment, 0, 0, autoResize, registries);
        }
    }

    static NetworkBufferImpl dummy(Registries registries) {
        // Dummy buffer with no memory allocated
        // Useful for size calculations
        return new NetworkBufferImpl(null, 0, 0, null, registries);
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }

    BinaryTagWriter nbtWriter() {
        if (this.nbtWriter == null) {
            this.nbtWriter = new BinaryTagWriter(new DataOutputStream(new OutputStream() {
                @Override
                public void write(int b) {
                    NetworkBufferImpl.this.write(BYTE, (byte) b);
                }
            }));
        }
        return this.nbtWriter;
    }

    BinaryTagReader nbtReader() {
        if (nbtReader == null) {
            this.nbtReader = new BinaryTagReader(new DataInputStream(new InputStream() {
                @Override
                public int read() {
                    return NetworkBufferImpl.this.read(BYTE) & 0xFF;
                }

                @Override
                public int available() {
                    return (int) NetworkBufferImpl.this.readableBytes();
                }
            }));
        }
        return nbtReader;
    }

    private static void assertOverflow(long value) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Math.toIntExact(value); // Check if long is within the bounds of an int
        } catch (ArithmeticException e) {
            throw new RuntimeException("Method does not support long values: " + value);
        }
    }
}
