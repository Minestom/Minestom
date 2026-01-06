package net.minestom.server.network.unsafe;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.ObjectPool;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.*;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static net.minestom.server.network.unsafe.NetworkBufferUnsafe.*;

@SuppressWarnings("removal")
final class NetworkBufferUnsafeImpl implements NetworkBuffer, NetworkBuffer.Direct {
    private static final Cleaner CLEANER = Cleaner.create();
    static final long DUMMY_ADDRESS = -1;

    private final BufferCleaner state;
    // Address may be -1 if the buffer is a dummy buffer
    // Dummy buffers are used for size calculations and do not have memory allocated
    private long address, capacity;
    private long readIndex, writeIndex;
    boolean readOnly;

    final @Nullable AutoResize autoResize;
    final @Nullable Registries registries;
    final @Nullable NetworkBufferUnsafeImpl parent;

    @Nullable ByteBuffer nioBuffer = null;

    NetworkBufferUnsafeImpl(long address, long capacity,
                            long readIndex, long writeIndex,
                            @Nullable AutoResize autoResize,
                            @Nullable Registries registries) {
        this(address, capacity, readIndex, writeIndex, autoResize, registries, null);
    }

    NetworkBufferUnsafeImpl(long address, long capacity,
                            long readIndex, long writeIndex,
                            @Nullable AutoResize autoResize,
                            @Nullable Registries registries,
                            @Nullable NetworkBufferUnsafeImpl parent) {
        this.address = address;
        this.capacity = capacity;
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.autoResize = autoResize;
        this.registries = registries;
        this.parent = parent;

        this.state = new BufferCleaner(new AtomicLong(address));
        if (parent == null && address != DUMMY_ADDRESS) CLEANER.register(this, state);
    }

    private record BufferCleaner(AtomicLong address) implements Runnable {
        @Override
        public void run() {
            UNSAFE.freeMemory(address.get());
        }
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
        Objects.checkFromIndexSize(srcOffset, length, capacity);
        if (dest.length < destOffset + length) {
            throw new IndexOutOfBoundsException("Destination array is too small: " + dest.length + " < " + (destOffset + length));
        }
        assertOverflow(srcOffset + length);
        assertOverflow(destOffset + length);
        if (length == 0) return;
        UNSAFE.copyMemory(null, address + srcOffset, dest, BYTE_ARRAY_OFFSET + destOffset, length);
    }

    @Override
    public void copyTo(long srcOffset, NetworkBuffer destBuffer, long destOffset, long length) {
        assertDummy();
        impl(destBuffer).assertReadOnly();
        Objects.checkFromIndexSize(srcOffset, length, capacity);
        Objects.checkFromIndexSize(destOffset, length, impl(destBuffer).capacity);
        if (length == 0) return;
        final long srcAddress = address + srcOffset;
        final long dstAddress = impl(destBuffer).address + destOffset;
        UNSAFE.copyMemory(srcAddress, dstAddress, length);
    }

    @Override
    public void fill(long srcOffset, long length, byte value) {
        assertDummy();
        assertReadOnly();
        Objects.checkFromIndexSize(srcOffset, length, capacity);
        assertOverflow(srcOffset + length);
        if (length == 0) return;
        UNSAFE.setMemory(null, address + srcOffset, length, value);
    }

    @Override
    public byte[] extractReadBytes(Consumer<NetworkBuffer> extractor) {
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

    @Override
    public byte[] extractWrittenBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        final long startingPosition = writeIndex();
        extractor.accept(this);
        final long endingPosition = writeIndex();
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
        return capacity;
    }

    @Override
    public NetworkBuffer readOnly() {
        this.readOnly = true;
        return this;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isResizable() {
        return autoResize != null;
    }

    @Override
    public void resize(long newSize) {
        assertDummy();
        assertReadOnly();
        if (newSize < capacity) throw new IllegalArgumentException("New size is smaller than the current size: " + newSize + " < " + capacity);
        if (newSize == capacity) throw new IllegalArgumentException("New size is the same as the current size: " + newSize);
        final long newAddress = UNSAFE.reallocateMemory(address, newSize);
        if (newAddress == 0) {
            throw new OutOfMemoryError("Failed to reallocate memory");
        }
        this.address = newAddress;
        this.capacity = newSize;
        this.state.address.set(newAddress);
    }

    @Override
    public void ensureWritable(long length) {
        assertReadOnly();
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative: " + length);
        if (writableBytes() >= length) return;
        final long newCapacity = newCapacity(length, capacity());
        resize(newCapacity);
    }

    @Override
    public void ensureReadable(@Range(from = 0, to = Long.MAX_VALUE) long length) {
        assertDummy();
        if (readableBytes() >= length) return;
        if (this.autoResize == null) throw new IndexOutOfBoundsException("Buffer is too small: " + readableBytes() + " < " + length);
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
        ByteBuffer nioBuffer = bufferSlice((int) readIndex, (int) readableBytes());
        nioBuffer.compact();
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public void trim() {
        assertDummy();
        assertReadOnly();
    }

    @Override
    public NetworkBuffer trimmed(NetworkBufferFactory factory) {
        assertDummy();
        assertReadOnly();
        final long readableBytes = readableBytes();
        if (readableBytes == capacity()) return this;
        return copy(factory, readIndex, readableBytes, 0, readableBytes);
    }

    @Override
    public NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        Objects.checkFromIndexSize(index, length, capacity);
        if (length == 0) {
            return new NetworkBufferUnsafeImpl(DUMMY_ADDRESS, 0, 0, 0, autoResize, registries);
        }
        final long newAddress = UNSAFE.allocateMemory(length);
        if (newAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory");
        }
        UNSAFE.copyMemory(address + index, newAddress, length);
        return new NetworkBufferUnsafeImpl(
                newAddress, length,
                readIndex, writeIndex,
                autoResize, registries);
    }

    @Override
    public NetworkBuffer copy(NetworkBufferFactory factory, long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        Objects.checkFromIndexSize(index, length, capacity);
        final NetworkBuffer newBuffer = factory.allocate(length);
        if (length > 0) {
            copyTo(index, newBuffer, 0, length);
        }
        return newBuffer.index(readIndex, writeIndex);
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        Objects.checkFromIndexSize(index, length, capacity);
        final long sliceAddress = address + index;
        return new NetworkBufferUnsafeImpl(
                sliceAddress, length,
                readIndex, writeIndex,
                null, registries, this);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertDummy();
        assertReadOnly();
        assertOverflow(writeIndex + writableBytes());
        var buffer = bufferSlice((int) writeIndex, (int) writableBytes());
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
        assertOverflow(readIndex + readableBytes);
        var buffer = bufferSlice((int) readIndex, (int) readableBytes);
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
        Objects.checkFromIndexSize(start, length, capacity);
        assertOverflow(start + length);
        if (length == 0) return;
        ByteBuffer input = bufferSlice((int) start, (int) length);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    // Use the JVM lazy loading to ignore these until compression is required.
    static class CompressionHolder {
        private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(ServerFlag.COMPRESS_POOL_SIZE, Deflater::new);
        private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(ServerFlag.DECOMPRESS_POOL_SIZE, Inflater::new);
    }

    @Override
    public long compress(long start, long length, NetworkBuffer output) {
        assertDummy();
        impl(output).assertReadOnly();
        Objects.checkFromIndexSize(start, length, capacity);
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice((int) start, (int) length);
        ByteBuffer outputBuffer = impl(output).bufferSlice((int) output.writeIndex(), (int) output.writableBytes());

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
        impl(output).assertReadOnly();
        Objects.checkFromIndexSize(start, length, capacity);
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice((int) start, (int) length);
        ByteBuffer outputBuffer = impl(output).bufferSlice((int) output.writeIndex(), (int) output.writableBytes());

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
    public Direct direct() {
        return this;
    }

    @Override
    public boolean contentEquals(NetworkBuffer buffer) {
        Objects.requireNonNull(buffer, "buffer");
        var impl2 = impl(buffer);
        if (this == impl2) return true;
        if (this.capacity != impl2.capacity) return false;
        if (this.isDummy() || impl2.isDummy()) return false;

        final long address1 = this.address;
        final long address2 = impl2.address;
        for (long i = 0; i < capacity; i++) {
            if (UNSAFE.getByte(address1 + i) != UNSAFE.getByte(address2 + i)) {
                return false;
            }
        }
        return true;
    }

    private ByteBuffer bufferSlice(int position, int length) {
        ByteBuffer nioBuffer = this.nioBuffer;
        if (nioBuffer == null) {
            this.nioBuffer = nioBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.BIG_ENDIAN);
        }
        updateAddress(nioBuffer, address);
        updateCapacity(nioBuffer, (int) capacity);
        nioBuffer.limit(position + length).position(position);
        return nioBuffer;
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity, registries != null, autoResize != null, readOnly);
    }

    private static final boolean ENDIAN_CONVERSION = ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN;

    private boolean isDummy() {
        return address == DUMMY_ADDRESS;
    }

    // Internal writing methods
    @Override
    public void putBytes(long index, byte[] value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, value.length, capacity);
        UNSAFE.copyMemory(value, BYTE_ARRAY_OFFSET, null, address + index, value.length);
    }

    @Override
    public void getBytes(long index, byte[] value) {
        assertDummy();
        Objects.checkFromIndexSize(index, value.length, capacity);
        UNSAFE.copyMemory(null, address + index, value, BYTE_ARRAY_OFFSET, value.length);
    }

    @Override
    public void putByte(long index, byte value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Byte.BYTES, capacity);
        UNSAFE.putByte(address + index, value);
    }

    @Override
    public byte getByte(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Byte.BYTES, capacity);
        return UNSAFE.getByte(address + index);
    }

    @Override
    public void putShort(long index, short value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Short.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Short.reverseBytes(value);
        UNSAFE.putShort(address + index, value);
    }

    @Override
    public short getShort(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Short.BYTES, capacity);
        final short value = UNSAFE.getShort(address + index);
        return ENDIAN_CONVERSION ? Short.reverseBytes(value) : value;
    }

    @Override
    public void putInt(long index, int value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Integer.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Integer.reverseBytes(value);
        UNSAFE.putInt(address + index, value);
    }

    @Override
    public int getInt(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Integer.BYTES, capacity);
        final int value = UNSAFE.getInt(address + index);
        return ENDIAN_CONVERSION ? Integer.reverseBytes(value) : value;
    }

    @Override
    public void putLong(long index, long value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Long.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Long.reverseBytes(value);
        UNSAFE.putLong(address + index, value);
    }

    @Override
    public long getLong(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Long.BYTES, capacity);
        final long value = UNSAFE.getLong(address + index);
        return ENDIAN_CONVERSION ? Long.reverseBytes(value) : value;
    }

    @Override
    public void putFloat(long index, float value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Float.BYTES, capacity);
        int intValue = Float.floatToIntBits(value);
        if (ENDIAN_CONVERSION) intValue = Integer.reverseBytes(intValue);
        UNSAFE.putInt(address + index, intValue);
    }

    @Override
    public float getFloat(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Float.BYTES, capacity);
        int intValue = UNSAFE.getInt(address + index);
        if (ENDIAN_CONVERSION) intValue = Integer.reverseBytes(intValue);
        return Float.intBitsToFloat(intValue);
    }

    @Override
    public void putDouble(long index, double value) {
        if (isDummy()) return;
        assertReadOnly();
        Objects.checkFromIndexSize(index, Double.BYTES, capacity);
        long longValue = Double.doubleToLongBits(value);
        if (ENDIAN_CONVERSION) longValue = Long.reverseBytes(longValue);
        UNSAFE.putLong(address + index, longValue);
    }

    @Override
    public double getDouble(long index) {
        assertDummy();
        Objects.checkFromIndexSize(index, Double.BYTES, capacity);
        long longValue = UNSAFE.getLong(address + index);
        if (ENDIAN_CONVERSION) longValue = Long.reverseBytes(longValue);
        return Double.longBitsToDouble(longValue);
    }

    @Override
    public void putString(long index, String value) {
        if (isDummy()) return;
        assertReadOnly();
        byte[] bytes = (value + '\0').getBytes(StandardCharsets.UTF_8);
        Objects.checkFromIndexSize(index, bytes.length, capacity);
        putBytes(index, bytes);
    }

    @Override
    public String getString(long index) {
        assertDummy();
        if (index >= capacity) throw new IndexOutOfBoundsException("Index out of bounds: " + index + " >= " + capacity);
        long endIndex = index;
        while (endIndex < capacity && getByte(endIndex) != 0) {
            endIndex++;
        }
        if (endIndex >= capacity) {
            throw new IndexOutOfBoundsException("Null terminator not found within buffer bounds");
        }
        long length = endIndex - index;
        byte[] bytes = new byte[(int) length];
        getBytes(index, bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public String getString(long index, long byteLength) {
        return getString(index, Math.toIntExact(byteLength));
    }

    @Override
    public String getString(long index, int byteLength) {
        assertDummy();
        Objects.checkFromIndexSize(index, byteLength, capacity);
        byte[] bytes = new byte[byteLength];
        getBytes(index, bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static NetworkBuffer wrap(byte [] bytes, long readIndex, long writeIndex, @Nullable Registries registries) {
        Objects.requireNonNull(bytes, "bytes");
        Objects.checkFromIndexSize(readIndex, writeIndex - readIndex, bytes.length);
        final NetworkBufferFactory factory = NetworkBufferFactory.staticFactory();
        final NetworkBuffer buffer = (registries != null ? factory.registry(registries) : factory).allocate(bytes.length);
        if (bytes.length > 0) {
            buffer.writeAt(0, NetworkBuffer.RAW_BYTES, bytes);
        }
        buffer.index(readIndex, writeIndex);
        return buffer;
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        Objects.requireNonNull(srcBuffer, "srcBuffer");
        Objects.requireNonNull(dstBuffer, "dstBuffer");
        var src = impl(srcBuffer);
        var dst = impl(dstBuffer);
        dst.assertReadOnly();
        Objects.checkFromIndexSize(srcOffset, length, src.capacity);
        Objects.checkFromIndexSize(dstOffset, length, dst.capacity);
        if (length == 0) return;
        final long srcAddress = src.address + srcOffset;
        final long dstAddress = dst.address + dstOffset;
        UNSAFE.copyMemory(srcAddress, dstAddress, length);
    }

    public static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        Objects.requireNonNull(buffer1, "buffer1");
        Objects.requireNonNull(buffer2, "buffer2");
        var impl1 = impl(buffer1);
        var impl2 = impl(buffer2);
        final int capacity = (int) impl1.capacity;
        if (capacity != impl2.capacity) return false;
        if (impl1.isDummy() || impl2.isDummy()) return false;
        final long address1 = impl1.address;
        final long address2 = impl2.address;
        for (long i = 0; i < capacity; i++) {
            if (UNSAFE.getByte(address1 + i) != UNSAFE.getByte(address2 + i)) {
                return false;
            }
        }
        return true;
    }

    void assertReadOnly() {
        if (readOnly) throw new UnsupportedOperationException("Buffer is read-only");
    }

    void assertDummy() {
        if (isDummy()) throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    static NetworkBufferUnsafeImpl dummy(@Nullable Registries registries) {
        // Dummy buffer with no memory allocated
        // Useful for size calculations
        return new NetworkBufferUnsafeImpl(
                DUMMY_ADDRESS, Long.MAX_VALUE,
                0, 0,
                null, registries);
    }

    static NetworkBufferUnsafeImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferUnsafeImpl) buffer;
    }

    private static void assertOverflow(long value) {
        if (value < 0) throw new RuntimeException("Value cannot be negative: " + value);
        if (value > Integer.MAX_VALUE) {
            throw new RuntimeException("Method does not support long values: " + value);
        }
    }
}