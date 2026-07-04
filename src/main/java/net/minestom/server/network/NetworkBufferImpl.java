package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import org.jetbrains.annotations.Contract;
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
import java.nio.channels.WritableByteChannel;
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

    private @Nullable MemorySegment segment; // null for dummy buffers
    private long readIndex, writeIndex;

    private final @Nullable AutoResize autoResize;
    private @Nullable Registries registries;

    NetworkBufferImpl(@Nullable MemorySegment segment,
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
        final MemorySegment segment = this.segment;
        if (segment != null) assertReadOnly(segment);
        type.write(this, value);
    }

    @Override
    public <T> @UnknownNullability T read(Type<T> type) {
        assertDummy(this.segment);
        return type.read(this);
    }

    @Override
    public <T> void writeAt(long index, Type<T> type, @UnknownNullability T value) {
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
        final long oldReadIndex = readIndex;
        readIndex = index;
        try {
            return read(type);
        } finally {
            readIndex = oldReadIndex;
        }
    }

    @Override
    public void copyTo(long srcOffset, byte[] dest, int destOffset, int length) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, srcOffset, dest, destOffset, length);
    }

    @Override
    public void copyTo(long srcOffset, MemorySegment dest, long destOffset, long length) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        MemorySegment.copy(segment, srcOffset, dest, destOffset, length);
    }

    @Override
    public byte[] extractBytes(Consumer<NetworkBuffer> extractor) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        byte[] output = new byte[Math.toIntExact(length)];
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, startingPosition, output, 0, output.length);
        return output;
    }

    @Override
    public NetworkBuffer clear() {
        return index(0, 0);
    }

    @Override
    public long writeIndex() {
        return writeIndex;
    }

    @Override
    public long readIndex() {
        return readIndex;
    }

    @Override
    public NetworkBuffer writeIndex(long writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    @Override
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

    @Override
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
        return readableBytes(writeIndex, readIndex);
    }

    @Override
    public long writableBytes() {
        return writableBytes(capacity(), writeIndex);
    }

    @Override
    public long capacity() {
        final MemorySegment segment = this.segment;
        return segment != null ? segment.byteSize() : Long.MAX_VALUE;
    }

    @Override
    public NetworkBuffer readOnly() {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return new NetworkBufferImpl(segment.asReadOnly(), this.readIndex, this.writeIndex, null, this.registries);
    }

    @Override
    public boolean isReadOnly() {
        final MemorySegment segment = this.segment;
        return segment != null && segment.isReadOnly();
    }

    @Override
    public void resize(long newSize) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        assertReadOnly(segment);
        final long capacity = capacity();
        if (newSize < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (newSize == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final MemorySegment newSegment = Arena.ofAuto().allocate(newSize);
        MemorySegment.copy(segment, 0, newSegment, 0, capacity);
        this.segment = newSegment;
    }

    @Override
    public void ensureWritable(long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return; // dummy's have infinite write length
        assertReadOnly(segment);
        final long capacity = segment.byteSize();
        if (writableBytes(capacity, writeIndex) >= length) return;
        final long newCapacity = newCapacity(length, capacity);
        resize(newCapacity);
    }

    @Override
    public void ensureReadable(long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        long readableBytes = readableBytes();
        if (readableBytes >= length) return;
        throw new IndexOutOfBoundsException("Buffer does not have %d bytes left, only %d are readable".formatted(length, readableBytes));
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
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final long readIndex = readIndex();
        if (readIndex == 0) return;
        MemorySegment.copy(segment, readIndex, segment, 0, readableBytes(writeIndex, readIndex));
        this.writeIndex -= readIndex;
        this.readIndex = 0;
    }

    @Override
    public NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        Objects.checkFromIndexSize(index, length, capacity());
        final MemorySegment newSegment = Arena.ofAuto().allocate(length);
        MemorySegment.copy(segment, index, newSegment, 0, length);
        return new NetworkBufferImpl(newSegment, readIndex, writeIndex, autoResize, registries);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel);
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final long writeIndex = this.writeIndex;
        var buffer = bufferSlice(segment, writeIndex, writableBytes(segment.byteSize(), writeIndex));
        final int count = channel.read(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceWrite(count);
        return count;
    }

    @Override
    public boolean writeChannel(WritableByteChannel channel) throws IOException {
        Objects.requireNonNull(channel);
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final long readIndex = this.readIndex;
        final long readableBytes = readableBytes(writeIndex, readIndex);
        if (readableBytes == 0) return true; // Nothing to write
        var buffer = bufferSlice(segment, readIndex, readableBytes);
        if (!buffer.hasRemaining())
            return true; // Nothing to write
        final int count = channel.write(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceRead(count);
        return !buffer.hasRemaining();
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        Objects.requireNonNull(cipher);
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        ByteBuffer input = bufferSlice(segment, start, length);
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
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final MemorySegment outputSegment = impl(output).segment;
        assertDummy(outputSegment);
        assertReadOnly(outputSegment);

        ByteBuffer input = bufferSlice(segment, start, length);
        ByteBuffer outputBuffer = bufferSlice(outputSegment, output.writeIndex(), output.writableBytes());

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
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        final MemorySegment outputSegment = impl(output).segment;
        assertDummy(outputSegment);
        assertReadOnly(outputSegment);

        ByteBuffer input = bufferSlice(segment, start, length);
        ByteBuffer outputBuffer = bufferSlice(outputSegment, output.writeIndex(), output.writableBytes());

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

    @Override
    public IOView ioView() {
        return new IOView(this);
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity(), registries != null, autoResize != null, isReadOnly());
    }

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_BYTE, index, value.length);
    }

    void _putBytes(long index, byte[] value, int offset, int length) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        MemorySegment.copy(value, offset, segment, ValueLayout.JAVA_BYTE, index, length);
    }

    void _getBytes(long index, byte[] value) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, index, value, 0, value.length);
    }

    void _putByte(long index, byte value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(ValueLayout.JAVA_BYTE, index, value);
    }

    byte _getByte(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(ValueLayout.JAVA_BYTE, index);
    }

    void _putShort(long index, short value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(SHORT_LAYOUT, index, value);
    }

    short _getShort(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(SHORT_LAYOUT, index);
    }

    void _putInt(long index, int value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(INT_LAYOUT, index, value);
    }

    int _getInt(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(INT_LAYOUT, index);
    }

    void _putLong(long index, long value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(LONG_LAYOUT, index, value);
    }

    long _getLong(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(LONG_LAYOUT, index);
    }

    void _putFloat(long index, float value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(FLOAT_LAYOUT, index, value);
    }

    float _getFloat(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(FLOAT_LAYOUT, index);
    }

    void _putDouble(long index, double value) {
        final MemorySegment segment = this.segment;
        if (isDummy(segment)) return;
        segment.set(DOUBLE_LAYOUT, index, value);
    }

    double _getDouble(long index) {
        final MemorySegment segment = this.segment;
        assertDummy(segment);
        return segment.get(DOUBLE_LAYOUT, index);
    }

    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        return new NetworkBufferImpl(segment, readIndex, writeIndex, null, registries);
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        var src = impl(srcBuffer).segment;
        var dst = impl(dstBuffer).segment;
        assertDummy(src);
        assertDummy(dst);
        MemorySegment.copy(src, srcOffset, dst, dstOffset, length);
    }

    static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        var impl1 = impl(buffer1).segment;
        var impl2 = impl(buffer2).segment;
        assertDummy(impl1);
        assertDummy(impl2);
        if (impl1.byteSize() != impl2.byteSize()) return false;
        return impl1.mismatch(impl2) == -1;
    }

    static void throwDummy() {
        throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    static boolean isDummy(@UnknownNullability MemorySegment segment) {
        return segment == null;
    }

    @Contract("null -> fail")
    static void assertDummy(@UnknownNullability MemorySegment segment) {
        if (isDummy(segment)) throwDummy();
    }

    static void throwReadOnly() {
        throw new UnsupportedOperationException("Buffer is read-only");
    }

    static void assertReadOnly(MemorySegment segment) {
        if (segment.isReadOnly()) throwReadOnly();
    }

    static ByteBuffer bufferSlice(MemorySegment segment, long position, long length) {
        return segment.asSlice(position, length).asByteBuffer().order(BIG_ENDIAN);
    }

    static long readableBytes(long writeIndex, long readIndex) {
        return writeIndex - readIndex;
    }

    static long writableBytes(long capacity, long writeIndex) {
        return capacity - writeIndex;
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

    // Use a record for final field trusting, hopefully better scalar replacement, to avoid caching IOView
    record IOView(NetworkBufferImpl buffer) implements NetworkBuffer.IOView {
        @Override
        public void readFully(byte[] bytes) {
            readFully(bytes, 0, bytes.length);
        }

        @Override
        public void readFully(byte[] bytes, int off, int len) {
            Objects.requireNonNull(bytes, "bytes");
            var buffer = buffer();
            buffer.ensureReadable(len);
            buffer.copyTo(buffer.readIndex(), bytes, off, len);
            buffer.advanceRead(len);
        }

        @Override
        public int skipBytes(int n) {
            if (n <= 0) return 0;
            var buffer = buffer();
            n = (int) Math.min(n, buffer.readableBytes());
            buffer.advanceRead(n);
            return n;
        }

        @Override
        public boolean readBoolean() {
            return buffer().read(BOOLEAN);
        }

        @Override
        public byte readByte() {
            return buffer().read(BYTE);
        }

        @Override
        public int readUnsignedByte() {
            return buffer().read(UNSIGNED_BYTE);
        }

        @Override
        public short readShort() {
            return buffer().read(SHORT);
        }

        @Override
        public int readUnsignedShort() {
            return buffer().read(UNSIGNED_SHORT);
        }

        @Override
        public char readChar() {
            return (char) readUnsignedShort();
        }

        @Override
        public int readInt() {
            return buffer().read(INT);
        }

        @Override
        public long readLong() {
            return buffer().read(LONG);
        }

        @Override
        public float readFloat() {
            return buffer().read(FLOAT);
        }

        @Override
        public double readDouble() {
            return buffer().read(DOUBLE);
        }

        @Override
        public String readUTF() {
            return buffer().read(STRING_IO_UTF8);
        }

        @Override
        public void write(int lower) {
            buffer().write(BYTE, (byte) lower);
        }

        @Override
        public void write(byte[] bytes) {
            Objects.requireNonNull(bytes, "bytes");
            buffer().write(RAW_BYTES, bytes);
        }

        @Override
        public void write(byte[] bytes, int off, int len) {
            Objects.requireNonNull(bytes, "bytes");
            if (len == 0) return;
            var buffer = buffer();
            buffer.ensureWritable(len); // Small intrinsic like RAW_BYTES
            buffer._putBytes(buffer.writeIndex(), bytes, off, len);
            buffer.advanceWrite(len);
        }

        @Override
        public void writeBoolean(boolean value) {
            buffer().write(BOOLEAN, value);
        }

        @Override
        public void writeByte(int value) {
            buffer().write(BYTE, (byte) value);
        }

        @Override
        public void writeShort(int value) {
            buffer().write(UNSIGNED_SHORT, value);
        }

        @Override
        public void writeChar(int value) {
            buffer().write(UNSIGNED_SHORT, value);
        }

        @Override
        public void writeInt(int value) {
            buffer().write(INT, value);
        }

        @Override
        public void writeLong(long value) {
            buffer().write(LONG, value);
        }

        @Override
        public void writeFloat(float value) {
            buffer().write(FLOAT, value);
        }

        @Override
        public void writeDouble(double value) {
            buffer().write(DOUBLE, value);
        }

        @Override
        public void writeBytes(String value) {
            Objects.requireNonNull(value, "value");
            var buffer = buffer();
            for (int i = 0; i < value.length(); i++) {
                buffer.write(BYTE, (byte) value.charAt(i)); // Low byte only
            }
        }

        @Override
        public void writeChars(String value) {
            Objects.requireNonNull(value, "value");
            var buffer = buffer();
            for (int i = 0; i < value.length(); i++) {
                buffer.write(UNSIGNED_SHORT, (int) value.charAt(i));
            }
        }

        @Override
        public void writeUTF(String value) {
            Objects.requireNonNull(value, "value");
            buffer().write(STRING_IO_UTF8, value);
        }
    }
}
