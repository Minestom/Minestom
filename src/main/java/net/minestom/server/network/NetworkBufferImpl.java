package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.io.EOFException;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class NetworkBufferImpl implements NetworkBuffer {
    // Writing order
    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final ValueLayout.OfByte JAVA_BYTE = ValueLayout.JAVA_BYTE.withOrder(BYTE_ORDER);
    public static final ValueLayout.OfShort JAVA_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(BYTE_ORDER);
    public static final ValueLayout.OfInt JAVA_INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(BYTE_ORDER);
    public static final ValueLayout.OfLong JAVA_LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(BYTE_ORDER);
    public static final ValueLayout.OfFloat JAVA_FLOAT = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(BYTE_ORDER);
    public static final ValueLayout.OfDouble JAVA_DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(BYTE_ORDER);

    // Dummy constants
    private static final long DUMMY_CAPACITY = Long.MAX_VALUE;

    // Required to keep a strong reference to the used arena. Regardless if we allocate on it or not
    // Nullable for dummy buffers or wrapped buffers.
    private @Nullable Arena arena; // final when autoResize is null
    private MemorySegment segment; // final when autoResize is null

    private final @Nullable AutoResize autoResize;
    private final @Nullable Registries registries;
    private final @Nullable Supplier<Arena> arenaSupplier; // This is awful, but auto arenas won't deallocate on resize without this.

    private long readIndex, writeIndex;

    NetworkBufferImpl(@Nullable Arena arena, MemorySegment segment, long readIndex, long writeIndex, @Nullable AutoResize autoResize, @Nullable Registries registries, @Nullable Supplier<Arena> arenaSupplier) {
        this.arena = arena;
        this.segment = segment;
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.autoResize = autoResize;
        this.registries = registries;
        this.arenaSupplier = arenaSupplier;
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
    public void copyTo(long srcOffset, byte[] dest, int destOffset, int length) {
        assertDummy();
        MemorySegment.copy(this.segment, JAVA_BYTE, srcOffset, dest, destOffset, length);
    }

    @Override
    public byte[] extractReadBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        return extractBytes(startingPosition, length);
    }

    @Override
    public byte[] extractWrittenBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        assertReadOnly();
        final long startingPosition = writeIndex();
        extractor.accept(this);
        final long endingPosition = writeIndex();
        final long length = endingPosition - startingPosition;
        return extractBytes(startingPosition, length);
    }

    private byte[] extractBytes(long index, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Buffer is too large to be extracted: " + length);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Buffer is too small to be extracted: " + length);
        }
        byte[] output = new byte[(int) length];
        MemorySegment.copy(this.segment, JAVA_BYTE, index, output, 0, output.length);
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
        if (isReadOnly()) return; // Should we warn? (also here cause asReadOnly does not check for this)
        this.segment = this.segment.asReadOnly();
    }

    @Override
    public boolean isReadOnly() {
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
        if (targetSize > newCapacity) {
            throw new IndexOutOfBoundsException("Buffer is full below the target size: " + newCapacity + " -> " + targetSize);
        }
        return newCapacity;
    }

    @Override
    public void resize(long length) {
        assertDummy();
        assertReadOnly();
        final Supplier<Arena> arenaSupplier = this.arenaSupplier;
        if (arenaSupplier == null) throw new IllegalStateException("Buffer cannot be resized without an arena");
        final long capacity = capacity();
        if (length < capacity) throw new IllegalArgumentException("New size is smaller than the current size");
        if (length == capacity) throw new IllegalArgumentException("New size is the same as the current size");
        final Arena arena = arenaSupplier.get(); // We need to use a new arena to allow the old one to deallocate.
        final MemorySegment newSegment = arena.allocate(length);
        MemorySegment.copy(this.segment, 0, newSegment, 0, capacity);
        this.segment = newSegment;
        this.arena = arena;
    }

    @Override
    public void compact() {
        assertDummy();
        assertReadOnly();
        if (readIndex == 0) return;
        final MemorySegment segment = this.segment;
        MemorySegment.copy(segment, readIndex, segment, 0, readableBytes());
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer trim(NetworkBuffer.Settings settings) {
        assertDummy();
        assertReadOnly();
        final long readableBytes = readableBytes();
        if (readableBytes == capacity()) return this;
        return copy(settings, readIndex, readableBytes, 0, readableBytes);
    }

    @Override
    public NetworkBuffer trim(Arena arena) {
        assertDummy();
        assertReadOnly();
        final long readableBytes = readableBytes();
        if (readableBytes == capacity()) return this;
        return copy(arena, readIndex, readableBytes, 0, readableBytes);
    }

    @Override
    public NetworkBuffer copy(NetworkBuffer.Settings settings, long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        final Settings settingsImpl = (Settings) settings;
        final Arena arena = settingsImpl.arena(); // Get a new arena to copy into.
        final MemorySegment copySegment = arena.allocate(length); // implicit null check of arena.
        MemorySegment.copy(this.segment, index, copySegment, 0, length);
        return new NetworkBufferImpl(arena, copySegment, readIndex, writeIndex, settingsImpl.autoResize(), settingsImpl.registries(), settingsImpl.arenaSupplier());
    }

    @Override
    public NetworkBuffer copy(Arena arena, long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        final MemorySegment copySegment = arena.allocate(length); // implicit null check of arena.
        MemorySegment.copy(this.segment, index, copySegment, 0, length);
        return new NetworkBufferImpl(arena, copySegment, readIndex, writeIndex, autoResize, registries, arenaSupplier);
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        final MemorySegment sliceSegment = this.segment.asSlice(index, length);
        // This region will live as long as the backing segment is alive, no reason to create another arena.
        return new NetworkBufferImpl(this.arena, sliceSegment, readIndex, writeIndex, null, registries, null);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertDummy();
        final ByteBuffer buffer = segment.asSlice(writeIndex, writableBytes()).asByteBuffer().order(BYTE_ORDER);
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
        final ByteBuffer buffer = segment.asSlice(readIndex, readableBytes).asByteBuffer().order(BYTE_ORDER);
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

    @Override
    public long compress(long start, long length, NetworkBuffer output) {
        assertDummy();

        final var outImpl = impl(output);
        outImpl.assertDummy();
        outImpl.assertReadOnly();

        final ByteBuffer input = segment.asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment.asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        // Use the JVM lazy loading to ignore these until compression is required.
        final class DeflaterPoolHolder {
            private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(Deflater::new);
        }

        Deflater deflater = DeflaterPoolHolder.DEFLATER_POOL.get();
        try {
            deflater.setInput(input);
            deflater.finish();
            final int bytes = deflater.deflate(outputBuffer);
            deflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            DeflaterPoolHolder.DEFLATER_POOL.add(deflater);
        }
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) throws DataFormatException {
        assertDummy();

        final var outImpl = impl(output);
        outImpl.assertDummy();
        outImpl.assertReadOnly();

        final ByteBuffer input = segment.asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment.asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        // Use the JVM lazy loading to ignore these until decompression is required.
        final class InflaterPoolHolder {
            private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(Inflater::new);
        }

        Inflater inflater = InflaterPoolHolder.INFLATER_POOL.get();
        try {
            inflater.setInput(input);
            final int bytes = inflater.inflate(outputBuffer);
            inflater.reset();
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            InflaterPoolHolder.INFLATER_POOL.add(inflater);
        }
    }

    @Override
    public @Nullable Registries registries() {
        return this.registries;
    }

    @Override
    public IOView ioView() {
        return new NetworkBufferIOViewImpl(this);
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity(), registries() != null, this.autoResize != null, isReadOnly());
    }

    private boolean isDummy() {
        return segment == MemorySegment.NULL; // Cant address check for zero (wrapping is zeroed)
    }

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        if (isDummy()) return;
        MemorySegment.copy(value, 0, this.segment, JAVA_BYTE, index, value.length);
    }

    void _getBytes(long index, byte[] value) {
        assertDummy();
        MemorySegment.copy(this.segment, JAVA_BYTE, index, value, 0, value.length);
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
        Objects.requireNonNull(segment, "segment"); // Doesn't make sense with a null segment.
        return new NetworkBufferImpl(
                null, segment,
                readIndex, writeIndex,
                null, registries, null
        );
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        var src = impl(srcBuffer);
        var dst = impl(dstBuffer);
        src.assertDummy();
        dst.assertDummy();
        dst.assertReadOnly();
        MemorySegment.copy(src.segment, srcOffset, dst.segment, dstOffset, length);
    }

    public static void fill(NetworkBuffer srcBuffer, long srcOffset, byte value, long length) {
        var src = impl(srcBuffer);
        src.assertDummy();
        src.assertReadOnly();
        src.segment.asSlice(srcOffset, length).fill(value);
    }

    public static boolean contentEquals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        var impl1 = impl(buffer1);
        var impl2 = impl(buffer2);
        if (impl1 == impl2) return true;
        if (impl1.capacity() != impl2.capacity()) return false;
        if (impl1.isDummy() || impl2.isDummy()) return false;

        return impl1.segment.mismatch(impl2.segment) == -1;
    }

    void assertReadOnly() { // These are already handled; but we can do these sooner before going into the types.
        if (isReadOnly()) throw new UnsupportedOperationException("Buffer is read-only");
    }

    void assertDummy() {
        if (isDummy()) throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    record Settings(Supplier<Arena> arenaSupplier, @Nullable AutoResize autoResize, @Nullable Registries registries) implements NetworkBuffer.Settings {
        static final Settings STATIC = new Settings(Arena::ofAuto, null, null);
        static final Settings RESIZEABLE = STATIC.autoResize(AutoResize.DOUBLE);

        public Settings {
            Objects.requireNonNull(arenaSupplier, "arenaSupplier");
        }

        @Override
        public Settings arena(Arena arena) {
            Objects.requireNonNull(arena, "arena");
            return new Settings(() -> arena, autoResize, registries);
        }

        @Override
        public Settings arena(Supplier<Arena> arenaSupplier) {
            Objects.requireNonNull(arenaSupplier, "arenaSupplier");
            return new Settings(arenaSupplier, autoResize, registries);
        }

        @Override
        public Settings autoResize(AutoResize autoResize) {
            Objects.requireNonNull(autoResize, "autoResize");
            return new Settings(arenaSupplier, autoResize, registries);
        }

        @Override
        public Settings registry(Registries registries) {
            Objects.requireNonNull(registries, "registries");
            return new Settings(arenaSupplier, autoResize, registries);
        }

        @Contract("->new")
        public Arena arena() {
            return Objects.requireNonNull(arenaSupplier.get(), "arena");
        }

        @Override
        public NetworkBuffer allocate(long length) {
            final Arena arena = arena();
            return new NetworkBufferImpl(
                    arena, arena.allocate(length),
                    0, 0,
                    autoResize, registries, arenaSupplier
            );
        }

    }

    @Contract(pure = true)
    static NetworkBufferImpl dummy(@Nullable Registries registries) {
        // Dummy buffer with no memory allocated
        // Useful for size calculations
        return new NetworkBufferImpl(
                null, MemorySegment.NULL,
                0, 0,
                null, registries, null);
    }

    @Contract(pure = true)
    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }
}
