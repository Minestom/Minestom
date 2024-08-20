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
    ByteBuffer nioBuffer;
    int readIndex, writeIndex;

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

    public <T> void write(@NotNull Type<T> type, @UnknownNullability T value) {
        assertReadOnly(this);
        type.write(this, value);
    }

    public <T> @UnknownNullability T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    @Override
    public <T> void writeAt(int index, @NotNull Type<T> type, @UnknownNullability T value) {
        assertReadOnly(this);
        final int oldWriteIndex = writeIndex;
        writeIndex = index;
        try {
            write(type, value);
        } finally {
            writeIndex = oldWriteIndex;
        }
    }

    @Override
    public <T> @UnknownNullability T readAt(int index, @NotNull Type<T> type) {
        final int oldReadIndex = readIndex;
        readIndex = index;
        try {
            return read(type);
        } finally {
            readIndex = oldReadIndex;
        }
    }

    public void copyTo(int srcOffset, byte @NotNull [] dest, int destOffset, int length) {
        this.nioBuffer.get(srcOffset, dest, destOffset, length);
    }

    @Override
    public void copyTo(int srcOffset, @NotNull ByteBuffer dest, int destOffset, int length) {
        dest.put(destOffset, nioBuffer, srcOffset, length);
    }

    public byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor) {
        final int startingPosition = readIndex();
        extractor.accept(this);
        final int endingPosition = readIndex();
        byte[] output = new byte[endingPosition - startingPosition];
        copyTo(startingPosition, output, 0, output.length);
        return output;
    }

    public @NotNull NetworkBuffer clear() {
        this.writeIndex = 0;
        this.readIndex = 0;
        return this;
    }

    public int writeIndex() {
        return writeIndex;
    }

    public int readIndex() {
        return readIndex;
    }

    public @NotNull NetworkBuffer writeIndex(int writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    public @NotNull NetworkBuffer readIndex(int readIndex) {
        this.readIndex = readIndex;
        return this;
    }

    @Override
    public @NotNull NetworkBuffer index(int readIndex, int writeIndex) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        return this;
    }

    public int advanceWrite(int length) {
        final int oldWriteIndex = writeIndex;
        writeIndex += length;
        return oldWriteIndex;
    }

    @Override
    public int advanceRead(int length) {
        final int oldReadIndex = readIndex;
        readIndex += length;
        return oldReadIndex;
    }

    @Override
    public int readableBytes() {
        return writeIndex - readIndex;
    }

    @Override
    public int writableBytes() {
        return size() - writeIndex;
    }

    @Override
    public int size() {
        return nioBuffer.capacity();
    }

    @Override
    public void readOnly() {
        this.readOnly = true;
        this.nioBuffer = nioBuffer.asReadOnlyBuffer();
    }

    @Override
    public void resize(int newSize) {
        ByteBuffer oldBuffer = nioBuffer;
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(newSize);
        oldBuffer.position(0);
        newBuffer.put(nioBuffer);
        nioBuffer = newBuffer.clear();
    }

    @Override
    public void ensureSize(int length) {
        final long capacity = nioBuffer.capacity();
        final long targetSize = writeIndex + length;
        if (capacity >= targetSize) return;

        final ResizeStrategy strategy = this.resizeStrategy;
        if (strategy == null)
            throw new IndexOutOfBoundsException("Buffer is full and cannot be resized: " + capacity + " -> " + targetSize);

        final long newCapacity = strategy.resize(capacity, targetSize);
        if (newCapacity == capacity)
            throw new IndexOutOfBoundsException("Buffer is full has been resized to the same capacity: " + capacity + " -> " + targetSize);

        // Check if long is within the bounds of an int
        if (newCapacity > Integer.MAX_VALUE) {
            throw new RuntimeException("Buffer size is too large, harass maintainers for `MemorySegment` support");
        }
        resize((int) newCapacity);
    }

    @Override
    public void compact() {
        assertReadOnly(this);
        nioBuffer.position(readIndex);
        nioBuffer.limit(writeIndex);
        nioBuffer.compact();
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer slice(int index, int length, int readIndex, int writeIndex) {
        NetworkBufferImpl slice = new NetworkBufferImpl(nioBuffer.slice(index, length), resizeStrategy, registries);
        slice.readIndex = readIndex;
        slice.writeIndex = writeIndex;
        slice.readOnly = readOnly;
        return slice;
    }

    @Override
    public NetworkBuffer copy(int index, int length, int readIndex, int writeIndex) {
        ByteBuffer payload = ByteBuffer.allocateDirect(length);
        payload.put(nioBuffer.slice(index, length).duplicate());
        NetworkBufferImpl copy = new NetworkBufferImpl(payload, resizeStrategy, registries);
        copy.readIndex = readIndex;
        copy.writeIndex = writeIndex;
        return copy;
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        assertReadOnly(this);
        var buffer = nioBuffer.slice(writeIndex, size() - writeIndex);
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
        var buffer = nioBuffer.slice(readIndex, writeIndex - readIndex);
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
    public void cipher(Cipher cipher, int start, int length) {
        ByteBuffer input = nioBuffer.slice(start, length);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ObjectPool<Deflater> DEFLATER_POOL = ObjectPool.pool(Deflater::new);
    private static final ObjectPool<Inflater> INFLATER_POOL = ObjectPool.pool(Inflater::new);

    @Override
    public int compress(int start, int length, NetworkBuffer output) {
        assertReadOnly(output);
        ByteBuffer src = this.nioBuffer;
        ByteBuffer dst = impl(output).nioBuffer;

        ByteBuffer input = src.slice(start, length);
        ByteBuffer outputBuffer = dst.slice(
                output.writeIndex(),
                output.size() - output.writeIndex());

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
    public int decompress(int start, int length, NetworkBuffer output) throws DataFormatException {
        assertReadOnly(output);
        Inflater inflater = INFLATER_POOL.get();
        try {
            inflater.setInput(nioBuffer.slice(start, length));
            ByteBuffer outputBuffer = impl(output).nioBuffer.slice(
                    output.writeIndex(),
                    output.size() - output.writeIndex());
            final int bytes = inflater.inflate(outputBuffer);
            output.advanceWrite(bytes);
            inflater.reset();
            return bytes;
        } finally {
            INFLATER_POOL.add(inflater);
        }
    }

    @Override
    public String toString() {
        return String.format("NetworkBufferImpl{r%d|w%d->%d, registries=%s, resizeStrategy=%s}",
                readIndex, writeIndex, size(), registries != null, resizeStrategy != null);
    }

    static void copy(NetworkBuffer srcBuffer, int srcOffset,
                     NetworkBuffer dstBuffer, int dstOffset, int length) {
        assertReadOnly(dstBuffer);
        dstBuffer.ensureSize(dstOffset + length);
        ByteBuffer src = impl(srcBuffer).nioBuffer;
        ByteBuffer dst = impl(dstBuffer).nioBuffer;
        dst.put(dstOffset, src, srcOffset, length);
    }

    public static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        ByteBuffer nioBuffer1 = impl(buffer1).nioBuffer.slice(0, buffer1.size());
        ByteBuffer nioBuffer2 = impl(buffer2).nioBuffer.slice(0, buffer2.size());
        return nioBuffer1.equals(nioBuffer2);
    }

    static void assertReadOnly(NetworkBuffer buffer) {
        if (impl(buffer).readOnly) {
            throw new UnsupportedOperationException("Buffer is read-only");
        }
    }

    static final class Builder implements NetworkBuffer.Builder {
        private final int initialSize;
        private ResizeStrategy resizeStrategy;
        private Registries registries;

        public Builder(int initialSize) {
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
            ByteBuffer buffer = ByteBuffer.allocateDirect(initialSize);
            return new NetworkBufferImpl(buffer, resizeStrategy, registries);
        }
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }
}
