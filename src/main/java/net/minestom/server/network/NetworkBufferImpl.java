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
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static net.minestom.server.network.NetworkBufferUnsafe.*;

final class NetworkBufferImpl implements NetworkBuffer {
    private static final Cleaner CLEANER = Cleaner.create();

    private final NetworkBufferImpl parent; // Used for slices so we can control GC over the parent buffer
    private final BufferCleaner state;
    private long address, capacity;
    private long readIndex, writeIndex;
    boolean readOnly;

    BinaryTagWriter nbtWriter;
    BinaryTagReader nbtReader;

    final @Nullable ResizeStrategy resizeStrategy;
    final @Nullable Registries registries;

    NetworkBufferImpl(NetworkBufferImpl parent,
                      long address, long capacity,
                      long readIndex, long writeIndex,
                      @Nullable ResizeStrategy resizeStrategy,
                      @Nullable Registries registries) {
        this.parent = parent;
        this.address = address;
        this.capacity = capacity;
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.resizeStrategy = resizeStrategy;
        this.registries = registries;

        this.state = new BufferCleaner(new AtomicLong(address));
        if (this.parent == null) CLEANER.register(this, state);
    }

    private record BufferCleaner(AtomicLong address) implements Runnable {
        @Override
        public void run() {
            UNSAFE.freeMemory(address.get());
        }
    }

    @Override
    public <T> void write(@NotNull Type<T> type, @UnknownNullability T value) {
        assertReadOnly();
        type.write(this, value);
    }

    @Override
    public <T> @UnknownNullability T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    @Override
    public <T> void writeAt(long index, @NotNull Type<T> type, @UnknownNullability T value) {
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
        if (length == 0) return;
        if (dest.length < destOffset + length) {
            throw new IndexOutOfBoundsException("Destination array is too small: " + dest.length + " < " + (destOffset + length));
        }
        UNSAFE.copyMemory(null, address + srcOffset, dest, BYTE_ARRAY_OFFSET + destOffset, length);
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
        return index(0, 0);
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
        return capacity;
    }

    @Override
    public void readOnly() {
        this.readOnly = true;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void resize(long newSize) {
        assertReadOnly();
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
        if (writableBytes() >= length) return;
        final long newCapacity = newCapacity(length, capacity());
        resize(newCapacity);
    }

    private long newCapacity(long length, long capacity) {
        final long targetSize = writeIndex + length;
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
        assertReadOnly();
        ByteBuffer nioBuffer = bufferSlice((int) readIndex, (int) readableBytes());
        nioBuffer.compact();
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        Objects.checkFromIndexSize(index, length, capacity);
        NetworkBufferImpl slice = new NetworkBufferImpl(this,
                address + index, length,
                readIndex, writeIndex,
                resizeStrategy, registries);
        slice.readOnly = readOnly;
        return slice;
    }

    @Override
    public NetworkBuffer copy(long index, long length, long readIndex, long writeIndex) {
        Objects.checkFromIndexSize(index, length, capacity);
        final long newAddress = UNSAFE.allocateMemory(length);
        if (newAddress == 0) {
            throw new OutOfMemoryError("Failed to allocate memory");
        }
        UNSAFE.copyMemory(address + index, newAddress, length);
        return new NetworkBufferImpl(null,
                newAddress, length,
                readIndex, writeIndex,
                resizeStrategy, registries);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertReadOnly();
        assertOverflow(writeIndex + writableBytes());
        var buffer = bufferSlice((int) writeIndex, (int) writableBytes());
        final int count = channel.read(buffer);
        if (count == -1) throw new IOException("Disconnected"); // EOS
        advanceWrite(count);
        return count;
    }

    @Override
    public boolean writeChannel(SocketChannel channel) throws IOException {
        final long readableBytes = readableBytes();
        if (readableBytes == 0) return true; // Nothing to write
        assertOverflow(readIndex + readableBytes);
        var buffer = bufferSlice((int) readIndex, (int) readableBytes);
        if (!buffer.hasRemaining())
            return true; // Nothing to write
        final int count = channel.write(buffer);
        if (count == -1) throw new IOException("Disconnected"); // EOS
        advanceRead(count);
        return !buffer.hasRemaining();
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        assertOverflow(start + length);
        ByteBuffer input = bufferSlice((int) start, (int) length);
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
        impl(output).assertReadOnly();
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice((int) start, (int) length);
        ByteBuffer outputBuffer = impl(output).bufferSlice((int) output.writeIndex(), (int) output.writableBytes());

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
        impl(output).assertReadOnly();
        assertOverflow(start + length);

        ByteBuffer input = bufferSlice((int) start, (int) length);
        ByteBuffer outputBuffer = impl(output).bufferSlice((int) output.writeIndex(), (int) output.writableBytes());

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

    private ByteBuffer bufferSlice(int position, int length) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(0).order(ByteOrder.BIG_ENDIAN);
        updateAddress(buffer, address);
        updateCapacity(buffer, (int) capacity);
        buffer.limit(position + length).position(position);
        return buffer;
    }

    @Override
    public String toString() {
        return String.format("NetworkBuffer{r%d|w%d->%d, registries=%s, resize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity, registries != null, resizeStrategy != null, readOnly);
    }

    private static final boolean ENDIAN_CONVERSION = ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN;

    // Internal writing methods
    void _putBytes(long index, byte[] value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, value.length, capacity);
        UNSAFE.copyMemory(value, BYTE_ARRAY_OFFSET, null, address + index, value.length);
    }

    void _getBytes(long index, byte[] value) {
        Objects.checkFromIndexSize(index, value.length, capacity);
        UNSAFE.copyMemory(null, address + index, value, BYTE_ARRAY_OFFSET, value.length);
    }

    void _putByte(long index, byte value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Byte.BYTES, capacity);
        UNSAFE.putByte(address + index, value);
    }

    byte _getByte(long index) {
        Objects.checkFromIndexSize(index, Byte.BYTES, capacity);
        return UNSAFE.getByte(address + index);
    }

    void _putShort(long index, short value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Short.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Short.reverseBytes(value);
        UNSAFE.putShort(address + index, value);
    }

    short _getShort(long index) {
        Objects.checkFromIndexSize(index, Short.BYTES, capacity);
        final short value = UNSAFE.getShort(address + index);
        return ENDIAN_CONVERSION ? Short.reverseBytes(value) : value;
    }

    void _putInt(long index, int value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Integer.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Integer.reverseBytes(value);
        UNSAFE.putInt(address + index, value);
    }

    int _getInt(long index) {
        Objects.checkFromIndexSize(index, Integer.BYTES, capacity);
        final int value = UNSAFE.getInt(address + index);
        return ENDIAN_CONVERSION ? Integer.reverseBytes(value) : value;
    }

    void _putLong(long index, long value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Long.BYTES, capacity);
        if (ENDIAN_CONVERSION) value = Long.reverseBytes(value);
        UNSAFE.putLong(address + index, value);
    }

    long _getLong(long index) {
        Objects.checkFromIndexSize(index, Long.BYTES, capacity);
        final long value = UNSAFE.getLong(address + index);
        return ENDIAN_CONVERSION ? Long.reverseBytes(value) : value;
    }

    void _putFloat(long index, float value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Float.BYTES, capacity);
        int intValue = Float.floatToIntBits(value);
        if (ENDIAN_CONVERSION) intValue = Integer.reverseBytes(intValue);
        UNSAFE.putInt(address + index, intValue);
    }

    float _getFloat(long index) {
        Objects.checkFromIndexSize(index, Float.BYTES, capacity);
        int intValue = UNSAFE.getInt(address + index);
        if (ENDIAN_CONVERSION) intValue = Integer.reverseBytes(intValue);
        return Float.intBitsToFloat(intValue);
    }

    void _putDouble(long index, double value) {
        assertReadOnly();
        Objects.checkFromIndexSize(index, Double.BYTES, capacity);
        long longValue = Double.doubleToLongBits(value);
        if (ENDIAN_CONVERSION) longValue = Long.reverseBytes(longValue);
        UNSAFE.putLong(address + index, longValue);
    }

    double _getDouble(long index) {
        Objects.checkFromIndexSize(index, Double.BYTES, capacity);
        long longValue = UNSAFE.getLong(address + index);
        if (ENDIAN_CONVERSION) longValue = Long.reverseBytes(longValue);
        return Double.longBitsToDouble(longValue);
    }

    static NetworkBuffer wrap(byte @NotNull [] bytes, long readIndex, long writeIndex, @Nullable Registries registries) {
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
        Objects.checkFromIndexSize(srcOffset, length, src.capacity);
        Objects.checkFromIndexSize(dstOffset, length, dst.capacity);
        final long srcAddress = src.address + srcOffset;
        final long dstAddress = dst.address + dstOffset;
        UNSAFE.copyMemory(srcAddress, dstAddress, length);
    }

    public static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        var impl1 = impl(buffer1);
        var impl2 = impl(buffer2);
        final int capacity = (int) impl1.capacity;
        if (capacity != impl2.capacity) return false;
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
            final long address = UNSAFE.allocateMemory(initialSize);
            return new NetworkBufferImpl(null,
                    address, initialSize,
                    0, 0,
                    resizeStrategy, registries);
        }
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }

    private static void assertOverflow(long value) {
        try {
            Math.toIntExact(value); // Check if long is within the bounds of an int
        } catch (ArithmeticException e) {
            throw new RuntimeException("Method does not support long values: " + value);
        }
    }
}
