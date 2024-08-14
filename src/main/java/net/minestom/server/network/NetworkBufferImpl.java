package net.minestom.server.network;

import net.minestom.server.registry.Registries;
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
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

final class NetworkBufferImpl implements NetworkBuffer {
    ByteBuffer nioBuffer;
    int readIndex, writeIndex;

    BinaryTagWriter nbtWriter;
    BinaryTagReader nbtReader;

    final @Nullable ResizeStrategy resizeStrategy;
    final @Nullable Registries registries;

    NetworkBufferImpl(@NotNull ByteBuffer buffer,
                      @Nullable ResizeStrategy resizeStrategy,
                      @Nullable Registries registries) {
        this.nioBuffer = buffer.order(ByteOrder.BIG_ENDIAN);
        this.resizeStrategy = resizeStrategy;
        this.registries = registries;

        this.readIndex = buffer.position();
        this.writeIndex = buffer.position();
    }

    public <T> void write(@NotNull Type<T> type, @UnknownNullability T value) {
        type.write(this, value);
    }

    public <T> @UnknownNullability T read(@NotNull Type<T> type) {
        return type.read(this);
    }

    public void copyTo(int srcOffset, byte @NotNull [] dest, int destOffset, int length) {
        this.nioBuffer.get(srcOffset, dest, destOffset, length);
    }

    public byte @NotNull [] extractBytes(@NotNull Consumer<@NotNull NetworkBuffer> extractor) {
        final int startingPosition = readIndex();
        extractor.accept(this);
        final int endingPosition = readIndex();
        byte[] output = new byte[endingPosition - startingPosition];
        copyTo(startingPosition, output, 0, output.length);
        return output;
    }

    public void clear() {
        this.writeIndex = 0;
        this.readIndex = 0;
    }

    public int writeIndex() {
        return writeIndex;
    }

    public int readIndex() {
        return readIndex;
    }

    public void writeIndex(int writeIndex) {
        this.writeIndex = writeIndex;
    }

    public void readIndex(int readIndex) {
        this.readIndex = readIndex;
    }

    @Override
    public void index(int readIndex, int writeIndex) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
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

    public int readableBytes() {
        return writeIndex - readIndex;
    }

    @Override
    public int size() {
        return nioBuffer.capacity();
    }

    public void resize(int newSize) {
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(newSize);
        nioBuffer.position(0);
        newBuffer.put(nioBuffer);
        nioBuffer = newBuffer.clear();
    }

    public void ensureSize(int length) {
        final ResizeStrategy strategy = this.resizeStrategy;
        if (strategy == null) return;

        final long capacity = nioBuffer.capacity();
        final long targetSize = writeIndex + length;
        if (capacity >= targetSize) return;

        final long newCapacity = strategy.resize(capacity, targetSize);
        // Check if long is within the bounds of an int
        if (newCapacity > Integer.MAX_VALUE) {
            throw new RuntimeException("Buffer size is too large, harass maintainers for `MemorySegment` support");
        }
        resize((int) newCapacity);
    }

    @Override
    public void compact() {
        nioBuffer.position(readIndex);
        nioBuffer.limit(writeIndex);
        nioBuffer.compact();
        writeIndex -= readIndex;
        readIndex = 0;
    }

    @Override
    public NetworkBuffer slice(int index, int length) {
        NetworkBufferImpl slice = new NetworkBufferImpl(nioBuffer.slice(index, length), resizeStrategy, registries);
        slice.readIndex = 0;
        slice.writeIndex = length;
        return slice;
    }

    @Override
    public int readChannel(ReadableByteChannel channel) throws IOException {
        final int count = channel.read(nioBuffer.slice(writeIndex, size() - writeIndex));
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        advanceWrite(count);
        return count;
    }

    @Override
    public void decrypt(Cipher cipher, int start, int length) {
        ByteBuffer input = nioBuffer.slice(start, length);
        try {
            cipher.update(input, input.duplicate());
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decompress(int start, int length, NetworkBuffer output) throws DataFormatException {
        Inflater inflater = new Inflater(); // TODO: Pool?
        inflater.setInput(nioBuffer.slice(start, length));
        ByteBuffer outputBuffer = impl(output).nioBuffer.slice(
                output.writeIndex(),
                output.size() - output.writeIndex());
        final int bytes = inflater.inflate(outputBuffer);
        output.advanceWrite(bytes);
        inflater.reset();
    }

    @Override
    public String toString() {
        return String.format("NetworkBufferImpl{r%d|w%d->%d, registries=%s, resizeStrategy=%s}",
                readIndex, writeIndex, size(), registries != null, resizeStrategy != null);
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
