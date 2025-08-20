package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class NetworkBufferImpl implements NetworkBuffer, NetworkBufferLayouts {
    // Dummy constants
    private static final long DUMMY_CAPACITY = Long.MAX_VALUE;

    // Nullable for dummy buffers.
    private final @Nullable Arena arena;
    private MemorySegment segment; // final when autoResize is null

    private final @Nullable AutoResize autoResize;
    private final @Nullable Registries registries;

    private long readIndex, writeIndex;

    NetworkBufferImpl(@Nullable Arena arena, MemorySegment segment, long readIndex, long writeIndex, @Nullable AutoResize autoResize, @Nullable Registries registries) {
        this.arena = arena;
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
    public void copyTo(long srcOffset, byte [] dest, long destOffset, long length) {
        assertDummy();
        if (length == 0) return;
        MemorySegment.copy(this.segment, srcOffset, MemorySegment.ofArray(dest), destOffset, length);
    }

    @Override
    public byte [] extractBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        if (length > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Buffer is too large to be extracted: " + length);
        }
        byte[] output = new byte[(int) length];
        copyTo(startingPosition, output, 0, output.length);
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
        return writeIndex - readIndex;
    }

    @Override
    public long writableBytes() {
        return capacity() - writeIndex;
    }

    @Override
    public long capacity() {
        if (isDummy()) return DUMMY_CAPACITY;
        return this.segment.byteSize();
    }

    @Override
    public void readOnly() {
        assertDummy();
        this.segment = this.segment.asReadOnly();
    }

    @Override
    public boolean isReadOnly() {
        if (isDummy()) return false; // Returns false for null segment regardless.
        return segment.isReadOnly();
    }

    @Override
    public void ensureWritable(long length) {
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
        if (newCapacity <= capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);
        return newCapacity;
    }

    @Override
    public void resize(long newSize) {
        assertDummy();
        assertReadOnly();
        if (arena == null) throw new IllegalStateException("Buffer cannot be resized without an arena");
        final long capacity = capacity();
        if (newSize < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (newSize == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final var oldSegment = this.segment;
        this.segment = arena.allocate(newSize);
        MemorySegment.copy(oldSegment, 0, this.segment, 0, capacity);
    }

    @Override
    public void compact() {
        assertDummy();
        assertReadOnly();
        if (readIndex == 0) return;
        MemorySegment.copy(this.segment, readIndex, this.segment, 0, readableBytes());

        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public void trim() {
        assertDummy();
        assertReadOnly();
        final var readableBytes = readableBytes();
        if (readableBytes == capacity()) return;
        this.segment = this.segment.asSlice(readIndex, readableBytes);

        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer copy(Arena arena, long index, long length, long readIndex, long writeIndex) {
        assertDummy();

        final var newReadIndex = Math.max(readIndex - index, 0);
        final var newWriteIndex = Math.max(writeIndex - index, 0);
        final var newBuffer = new NetworkBufferImpl(arena, arena.allocate(length), newReadIndex, newWriteIndex, autoResize, registries);
        assert !newBuffer.isDummy() : "Dummy active for a newly created buffer";

        MemorySegment.copy(this.segment, index, newBuffer.segment, 0, length);

        return newBuffer;
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        assertDummy();

        // Shift read and write indexes over.
        final var newReadIndex = Math.max(readIndex() - readIndex, 0);
        final var newWriteIndex = Math.max(writeIndex() - writeIndex, 0);
        final var sliceSegment = this.segment.asSlice(index, length);

        // This region will live as long as the backing segment is alive, no reason to create another arena.
        return new NetworkBufferImpl(arena, sliceSegment, newReadIndex, newWriteIndex, autoResize, registries);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertDummy();
        final var buffer = segment.asSlice(writeIndex, writableBytes()).asByteBuffer().order(BYTE_ORDER);
        final int count = channel.read(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceWrite(count);
        return count;
    }

    @Override
    public boolean writeChannel(WritableByteChannel channel) throws IOException {
        assertDummy();
        final long readableBytes = readableBytes();
        if (readableBytes == 0) return true; // Nothing to write
        final var buffer = segment.asSlice(readIndex, readableBytes).asByteBuffer().order(BYTE_ORDER);
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
        final ByteBuffer input = segment.asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
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

        final var outImpl = impl(output);
        outImpl.assertDummy();

        final ByteBuffer input = segment.asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment.asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        Deflater deflater = CompressionHolder.DEFLATER_POOL.get();
        try {
            deflater.setInput(input);
            deflater.finish();
            final int bytes = deflater.deflate(outputBuffer);
            deflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            CompressionHolder.DEFLATER_POOL.add(deflater);
        }
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) throws DataFormatException {
        assertDummy();

        final var outImpl = impl(output);
        outImpl.assertDummy();

        final ByteBuffer input = segment.asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment.asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        Inflater inflater = CompressionHolder.INFLATER_POOL.get();
        try {
            inflater.setInput(input);
            final int bytes = inflater.inflate(outputBuffer);
            inflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            CompressionHolder.INFLATER_POOL.add(inflater);
        }
    }

    @Override
    public @Nullable Registries registries() {
        return registries;
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity(), registries != null, autoResize != null, isReadOnly());
    }

    private boolean isDummy() {
        return segment == MemorySegment.NULL;
    }

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        if (isDummy()) return;
        MemorySegment.copy(MemorySegment.ofArray(value), 0, this.segment, index, value.length);
    }

    void _getBytes(long index, byte[] value) {
        assertDummy();
        MemorySegment.copy(this.segment, index, MemorySegment.ofArray(value), 0, value.length);
    }

    void _putByte(long index, byte value) {
        if (isDummy()) return;
        segment.set(JAVA_BYTE, index, value);
    }

    byte _getByte(long index) {
        assertDummy();
        return segment.get(JAVA_BYTE, index);
    }

    void _putShort(long index, short value) {
        if (isDummy()) return;
        segment.set(JAVA_SHORT, index, value);
    }

    short _getShort(long index) {
        assertDummy();
        return segment.get(JAVA_SHORT, index);
    }

    void _putInt(long index, int value) {
        if (isDummy()) return;
        segment.set(JAVA_INT, index, value);
    }

    int _getInt(long index) {
        assertDummy();
        return segment.get(JAVA_INT, index);
    }

    void _putLong(long index, long value) {
        if (isDummy()) return;
        segment.set(JAVA_LONG, index, value);
    }

    long _getLong(long index) {
        assertDummy();
        return segment.get(JAVA_LONG, index);
    }

    void _putFloat(long index, float value) {
        if (isDummy()) return;
        segment.set(JAVA_FLOAT, index, value);
    }

    float _getFloat(long index) {
        assertDummy();
        return segment.get(JAVA_FLOAT, index);
    }

    void _putDouble(long index, double value) {
        if (isDummy()) return;
        segment.set(JAVA_DOUBLE, index, value);
    }

    double _getDouble(long index) {
        assertDummy();
        return segment.get(JAVA_DOUBLE, index);
    }

    // Warning this is writing a null terminated string
    void _putString(long index, String value) {
        if (isDummy()) return;
        segment.setString(index, value);
    }

    // Warning this is reading a null terminated string
    String _getString(long index) {
        assertDummy();
        return segment.getString(index);
    }

    static NetworkBuffer wrap(MemorySegment segment, long readIndex, long writeIndex, @Nullable Registries registries) {
        return new NetworkBufferImpl(
                null, segment,
                readIndex, writeIndex, null, registries
        );
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        var src = impl(srcBuffer);
        var dst = impl(dstBuffer);
        src.assertDummy();
        dst.assertDummy();
        MemorySegment.copy(src.segment, srcOffset, dst.segment, dstOffset, length);
    }

    public static boolean contentEquals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        var impl1 = impl(buffer1);
        var impl2 = impl(buffer2);
        if (impl1 == impl2) return true;
        if (impl1.capacity() != impl2.capacity()) return false;
        if (impl1.isDummy() || impl2.isDummy()) return false;

        return impl1.segment.mismatch(impl2.segment) == -1;
    }

    void assertReadOnly() {
        if (isReadOnly()) throw new UnsupportedOperationException("Buffer is read-only");
    }

    @Contract("-> fail")
    void assertDummy() {
        if (isDummy()) throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    static final class Builder implements NetworkBuffer.Builder {
        private final long initialSize;
        private @Nullable Arena arena;
        private @Nullable AutoResize autoResize;
        private @Nullable Registries registries;
        public Builder(long initialSize) {
            this.initialSize = initialSize;
        }

        @Override
        public NetworkBuffer.Builder arena(Arena arena) {
            Check.notNull(arena, "Arena cannot be null, use NetworkBuffer#sizeOf instead.");
            this.arena = arena;
            return this;
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
            if (this.arena == null) this.arena = Arena.ofAuto();
            return new NetworkBufferImpl(
                    arena, arena.allocate(initialSize),
                    0, 0,
                    autoResize, registries);
        }

    }

    static NetworkBufferImpl dummy(@Nullable Registries registries) {
        // Dummy buffer with no memory allocated
        // Useful for size calculations
        return new NetworkBufferImpl(
                null, MemorySegment.NULL,
                0, 0,
                null, registries);
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }
}
