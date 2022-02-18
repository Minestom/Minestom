package net.minestom.server.utils.binary;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTReader;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Manages off-heap memory.
 * Not thread-safe.
 */
@ApiStatus.Internal
public final class BinaryBuffer {
    private ByteBuffer nioBuffer; // To become a `MemorySegment` once released
    private NBTReader nbtReader;
    private NBTWriter nbtWriter;

    private final int capacity;
    private int readerOffset, writerOffset;

    private BinaryBuffer(ByteBuffer buffer) {
        this.nioBuffer = buffer;
        this.capacity = buffer.capacity();
    }

    @ApiStatus.Internal
    public static BinaryBuffer ofSize(int size) {
        return new BinaryBuffer(ByteBuffer.allocateDirect(size));
    }

    @ApiStatus.Internal
    public static BinaryBuffer wrap(ByteBuffer buffer) {
        assert buffer.isDirect();
        return new BinaryBuffer(buffer);
    }


    public static BinaryBuffer copy(BinaryBuffer buffer) {
        final int size = buffer.readableBytes();
        final var temp = ByteBuffer.allocateDirect(size)
                .put(buffer.asByteBuffer(0, size));
        BinaryBuffer newBuffer = new BinaryBuffer(temp);
        newBuffer.writerOffset = size;
        return newBuffer;
    }

    public void write(ByteBuffer buffer, int index, int length) {
        this.nioBuffer.put(writerOffset, buffer, index, length);
        this.writerOffset += length;
    }

    public void write(ByteBuffer buffer) {
        write(buffer, buffer.position(), buffer.remaining());
    }

    public void write(BinaryBuffer buffer) {
        write(buffer.asByteBuffer(buffer.readerOffset, buffer.writerOffset - buffer.readerOffset));
    }

    public int readVarInt() {
        int value = 0;
        for (int i = 0; i < 5; i++) {
            final int offset = readerOffset + i;
            final byte k = nioBuffer.get(offset);
            value |= (k & 0x7F) << i * 7;
            if ((k & 0x80) != 128) {
                this.readerOffset = offset + 1;
                return value;
            }
        }
        throw new RuntimeException("VarInt is too big");
    }

    public @NotNull Marker mark() {
        return new Marker(readerOffset, writerOffset);
    }

    public void reset(int readerOffset, int writerOffset) {
        this.readerOffset = readerOffset;
        this.writerOffset = writerOffset;
    }

    public void reset(@NotNull Marker marker) {
        reset(marker.readerOffset(), marker.writerOffset());
    }

    public boolean canRead(int size) {
        return readerOffset + size <= writerOffset;
    }

    public boolean canWrite(int size) {
        return writerOffset + size < capacity;
    }

    public int capacity() {
        return capacity;
    }

    public int readerOffset() {
        return readerOffset;
    }

    public void readerOffset(int offset) {
        this.readerOffset = offset;
    }

    public int writerOffset() {
        return writerOffset;
    }

    public int readableBytes() {
        return writerOffset - readerOffset;
    }

    public void writeBytes(byte[] bytes) {
        this.nioBuffer.put(writerOffset, bytes);
        this.writerOffset += bytes.length;
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        this.nioBuffer.get(readerOffset, bytes);
        this.readerOffset += length;
        return bytes;
    }

    public byte[] readRemainingBytes() {
        return readBytes(readableBytes());
    }

    public BinaryBuffer clear() {
        this.readerOffset = 0;
        this.writerOffset = 0;
        this.nioBuffer.limit(capacity);
        return this;
    }

    public ByteBuffer asByteBuffer(int reader, int length) {
        return nioBuffer.slice(reader, length);
    }

    @ApiStatus.Internal
    public ByteBuffer asByteBuffer() {
        return nioBuffer;
    }

    public boolean writeChannel(WritableByteChannel channel) throws IOException {
        if (readerOffset == writerOffset)
            return true; // Nothing to write
        var writeBuffer = nioBuffer.slice(readerOffset, writerOffset - readerOffset);
        final int count = channel.write(writeBuffer);
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        this.readerOffset += count;
        return writeBuffer.limit() == writeBuffer.position();
    }

    public void readChannel(ReadableByteChannel channel) throws IOException {
        final int count = channel.read(nioBuffer.slice(writerOffset, capacity - writerOffset));
        if (count == -1) {
            // EOS
            throw new IOException("Disconnected");
        }
        this.writerOffset += count;
    }

    @Override
    public String toString() {
        return "BinaryBuffer{" +
                "readerOffset=" + readerOffset +
                ", writerOffset=" + writerOffset +
                ", capacity=" + capacity +
                '}';
    }

    public record Marker(int readerOffset, int writerOffset) {
    }
}
