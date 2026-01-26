package net.minestom.server.network.foreign;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.collection.ObjectPool;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.EOFException;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

sealed abstract class NetworkBufferSegmentImpl implements NetworkBuffer, NetworkBuffer.Direct permits NetworkBufferStaticSegmentImpl, NetworkBufferResizeableSegmentImpl {
    // Writing order
    private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    private static final ValueLayout.OfByte JAVA_BYTE = ValueLayout.JAVA_BYTE.withOrder(BYTE_ORDER);
    private static final ValueLayout.OfShort JAVA_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(BYTE_ORDER);
    private static final ValueLayout.OfInt JAVA_INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(BYTE_ORDER);
    private static final ValueLayout.OfLong JAVA_LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(BYTE_ORDER);
    private static final ValueLayout.OfFloat JAVA_FLOAT = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(BYTE_ORDER);
    private static final ValueLayout.OfDouble JAVA_DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(BYTE_ORDER);

    private final @Nullable Registries registries;

    private long readIndex, writeIndex;

    protected NetworkBufferSegmentImpl(long readIndex, long writeIndex, @Nullable Registries registries) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.registries = registries;
        super();
    }

    @Contract(pure = true)
    static NetworkBufferSegmentImpl impl(NetworkBuffer buffer) {
        Objects.requireNonNull(buffer, "buffer");
        if (!(buffer instanceof NetworkBufferSegmentImpl impl))
            throw new IllegalStateException("Expected NetworkBufferSegment found: %s".formatted(buffer));
        return impl;
    }

    protected abstract MemorySegment segment();

    protected abstract @Nullable Arena arena();

    @Override
    public final <T> void write(Type<T> type, @UnknownNullability T value) {
        assertReadOnly();
        type.write(this, value);
    }

    @Override
    public final void copyTo(long srcOffset, byte[] dest, int destOffset, int length) {
        MemorySegment.copy(this.segment(), JAVA_BYTE, srcOffset, dest, destOffset, length);
    }

    @Override
    public void copyTo(long srcOffset, NetworkBuffer destBuffer, long destOffset, long length) {
        final var dst = impl(destBuffer);
        dst.assertReadOnly();
        MemorySegment.copy(segment(), srcOffset, dst.segment(), destOffset, length);
    }

    @Override
    public final void fill(long srcOffset, long length, byte value) {
        assertReadOnly();
        segment().asSlice(srcOffset, length).fill(value);
    }

    @Override
    public final byte[] extractReadBytes(Consumer<? super NetworkBuffer> extractor) {
        final long startingPosition = readIndex();
        extractor.accept(this);
        final long endingPosition = readIndex();
        final long length = endingPosition - startingPosition;
        return extractBytes(startingPosition, length);
    }

    @Override
    public final byte[] extractWrittenBytes(Consumer<? super NetworkBuffer> extractor) {
        assertReadOnly();
        final long startingPosition = writeIndex();
        extractor.accept(this);
        final long endingPosition = writeIndex();
        final long length = endingPosition - startingPosition;
        return extractBytes(startingPosition, length);
    }

    @Contract("_, _ -> new")
    private byte[] extractBytes(long index, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Buffer is too large to be extracted: " + length);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Buffer is too small to be extracted: " + length);
        }
        byte[] output = new byte[(int) length];
        MemorySegment.copy(this.segment(), JAVA_BYTE, index, output, 0, output.length);
        return output;
    }

    @Override
    public final NetworkBuffer clear() {
        return index(0, 0);
    }

    @Override
    public final long writeIndex() {
        return writeIndex;
    }

    @Override
    public final long readIndex() {
        return readIndex;
    }

    @Override
    public final NetworkBuffer writeIndex(long writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    @Override
    public final NetworkBuffer readIndex(long readIndex) {
        this.readIndex = readIndex;
        return this;
    }

    @Override
    public final NetworkBuffer index(long readIndex, long writeIndex) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        return this;
    }

    @Override
    public final long advanceWrite(long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        final long oldWriteIndex = writeIndex;
        writeIndex = oldWriteIndex + length;
        return oldWriteIndex;
    }

    @Override
    public final long advanceRead(long length) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative");
        final long oldReadIndex = readIndex;
        readIndex = oldReadIndex + length;
        return oldReadIndex;
    }

    @Override
    public final long readableBytes() {
        return writeIndex - readIndex;
    }

    @Override
    public final long writableBytes() {
        return capacity() - writeIndex;
    }

    @Override
    public final long capacity() {
        return this.segment().byteSize();
    }

    @Override
    public final NetworkBuffer readOnly() {
        if (isReadOnly()) return this; // Should we warn? (also here cause asReadOnly does not check for this)
        return new NetworkBufferStaticSegmentImpl(arena(), segment().asReadOnly(), readIndex, writeIndex, registries);
    }

    @Override
    public final void compact() {
        assertReadOnly();
        if (readIndex == 0) return;
        final MemorySegment segment = this.segment();
        MemorySegment.copy(segment, readIndex, segment, 0, readableBytes());
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public final NetworkBuffer copy(NetworkBufferFactory factory, long index, long length, long readIndex, long writeIndex) {
        final NetworkBufferSegmentImpl newBuffer = (NetworkBufferSegmentImpl) factory.allocate(length);
        MemorySegment.copy(this.segment(), index, newBuffer.segment(), 0, length);
        return newBuffer.index(readIndex, writeIndex);
    }

    @Override
    public final NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        final MemorySegment sliceSegment = this.segment().asSlice(index, length);
        // This region will live as long as the backing segment is alive, no reason to create another arena.
        return new NetworkBufferStaticSegmentImpl(arena(), sliceSegment, readIndex, writeIndex, registries);
    }

    @Override
    public final int readChannel(ReadableByteChannel channel) throws IOException {
        final ByteBuffer buffer = segment().asSlice(writeIndex, writableBytes()).asByteBuffer().order(BYTE_ORDER);
        final int count = channel.read(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceWrite(count);
        return count;
    }

    @Override
    public final boolean writeChannel(WritableByteChannel channel) throws IOException {
        final long readableBytes = readableBytes();
        if (readableBytes == 0) return true; // Nothing to write
        final ByteBuffer buffer = segment().asSlice(readIndex, readableBytes).asByteBuffer().order(BYTE_ORDER);
        if (!buffer.hasRemaining())
            return true; // Nothing to write
        final int count = channel.write(buffer);
        if (count == -1) throw new EOFException("Disconnected");
        advanceRead(count);
        return !buffer.hasRemaining();
    }

    @Override
    public final void cipher(Cipher cipher, long start, long length) {
        final ByteBuffer input = segment().asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final long compress(long start, long length, NetworkBuffer output) {
        final var outImpl = impl(output);
        outImpl.assertReadOnly();

        final ByteBuffer input = segment().asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment().asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        // Use the JVM lazy loading to ignore these until compression is required.
        final class DeflaterPoolHolder {
            private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(ServerFlag.COMPRESS_POOL_SIZE, Deflater::new);
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
    public final long decompress(long start, long length, NetworkBuffer output) throws DataFormatException {

        final var outImpl = impl(output);
        outImpl.assertReadOnly();

        final ByteBuffer input = segment().asSlice(start, length).asByteBuffer().order(BYTE_ORDER);
        final ByteBuffer outputBuffer = outImpl.segment().asSlice(output.writeIndex(), output.writableBytes()).asByteBuffer().order(BYTE_ORDER);

        // Use the JVM lazy loading to ignore these until decompression is required.
        final class InflaterPoolHolder {
            private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(ServerFlag.DECOMPRESS_POOL_SIZE, Inflater::new);
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
    public final @Nullable Registries registries() {
        return this.registries;
    }

    @Override
    public final String toString() {
        return String.format("NetworkBufferSegment{r%d|w%d->%d, registries=%b, autoResize=%b, readOnly=%b, segment=%s, arena=%s}",
                readIndex(), writeIndex(), capacity(), registries() != null, isResizable(), isReadOnly(), segment(), arena());
    }

    // Internal writing methods
    @Override
    public final void putBytes(long index, byte[] value) {
        MemorySegment.copy(value, 0, this.segment(), JAVA_BYTE, index, value.length);
    }

    @Override
    public final void getBytes(long index, byte[] value) {
        MemorySegment.copy(this.segment(), JAVA_BYTE, index, value, 0, value.length);
    }

    @Override
    public final void putByte(long index, byte value) {
        segment().set(JAVA_BYTE, index, value);
    }

    @Override
    public final byte getByte(long index) {
        return segment().get(JAVA_BYTE, index);
    }

    @Override
    public final void putShort(long index, short value) {
        segment().set(JAVA_SHORT, index, value);
    }

    @Override
    public final short getShort(long index) {
        return segment().get(JAVA_SHORT, index);
    }

    @Override
    public final void putInt(long index, int value) {
        segment().set(JAVA_INT, index, value);
    }

    @Override
    public final int getInt(long index) {
        return segment().get(JAVA_INT, index);
    }

    @Override
    public final void putLong(long index, long value) {
        segment().set(JAVA_LONG, index, value);
    }

    @Override
    public final long getLong(long index) {
        return segment().get(JAVA_LONG, index);
    }

    @Override
    public final void putFloat(long index, float value) {
        segment().set(JAVA_FLOAT, index, value);
    }

    @Override
    public final float getFloat(long index) {
        return segment().get(JAVA_FLOAT, index);
    }

    @Override
    public final void putDouble(long index, double value) {
        segment().set(JAVA_DOUBLE, index, value);
    }

    @Override
    public final double getDouble(long index) {
        return segment().get(JAVA_DOUBLE, index);
    }

    // Warning this is writing a null terminated string
    @Override
    public final void putString(long index, String value) {
        segment().setString(index, value);
    }

    // Warning this is reading a null terminated string
    @Override
    public final String getString(long index) {
        return segment().getString(index);
    }

    public final String getString(long index, int byteLength) {
        byte[] bytes = new byte[byteLength];
        getBytes(index, bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public final String getString(long index, long byteLength) {
        if (NetworkBufferSegmentMethods.STRING_SUPPORTED) {
            // We can use the better no String copy method!
            return NetworkBufferSegmentMethods.getString(segment(), index, StandardCharsets.UTF_8, byteLength);
        }
        // Only supports int for small.
        return getString(index, Math.toIntExact(byteLength));
    }

    final void assertReadOnly() { // These are already handled; but we can do these sooner before going into the types.
        if (isReadOnly()) throw new UnsupportedOperationException("Buffer is read-only");
    }

    @Override
    public Direct direct() {
        return this;
    }

    @Override
    public final boolean contentEquals(NetworkBuffer buffer) {
        var impl2 = impl(buffer);
        if (this == impl2) return true;
        if (this.capacity() != impl2.capacity()) return false;

        return this.segment().mismatch(impl2.segment()) == -1;
    }

    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }
}
