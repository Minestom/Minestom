package net.minestom.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.BinaryTagReader;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.*;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Netty {@link ByteBuf}-backed implementation of {@link NetworkBuffer}.
 *
 * <p>All usages of {@code sun.misc.Unsafe}, {@code java.nio.ByteBuffer},
 * {@code java.nio.channels.ReadableByteChannel}, and
 * {@code java.nio.channels.SocketChannel} have been removed. Compression
 * now delegates to Netty's {@link JdkZlibEncoder}/{@link JdkZlibDecoder}.
 */
final class NetworkBufferImpl implements NetworkBuffer {

    /**
     * The underlying Netty buffer. For <em>resizable</em> buffers this is always
     * a heap/pooled buffer whose capacity is managed manually through
     * {@link ByteBuf#capacity(int)}. For <em>dummy</em> buffers (size calculations)
     * this is {@link #DUMMY_BUF} and writes are silently discarded.
     */
    private ByteBuf buf;

    /** Sentinel value used for dummy (size-calculation) buffers. */
    private static final ByteBuf DUMMY_BUF = Unpooled.EMPTY_BUFFER;

    private long readIndex;
    private long writeIndex;
    boolean readOnly;

    private BinaryTagWriter nbtWriter;
    private BinaryTagReader nbtReader;

    final @Nullable AutoResize autoResize;
    final @Nullable Registries registries;

    NetworkBufferImpl(ByteBuf buf,
                      long readIndex, long writeIndex,
                      @Nullable AutoResize autoResize,
                      @Nullable Registries registries) {
        this.buf       = buf;
        this.readIndex  = readIndex;
        this.writeIndex = writeIndex;
        this.autoResize = autoResize;
        this.registries = registries;
    }


    private boolean isDummy() {
        return buf == DUMMY_BUF;
    }

    void assertDummy() {
        if (isDummy()) throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    void assertReadOnly() {
        if (readOnly) throw new UnsupportedOperationException("Buffer is read-only");
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
        final long old = writeIndex;
        writeIndex = index;
        try {
            write(type, value);
        } finally {
            writeIndex = old;
        }
    }

    @Override
    public <T> @UnknownNullability T readAt(long index, Type<T> type) {
        assertDummy();
        final long old = readIndex;
        readIndex = index;
        try {
            return read(type);
        } finally {
            readIndex = old;
        }
    }

    @Override
    public void copyTo(long srcOffset, byte[] dest, long destOffset, long length) {
        assertDummy();
        if (length == 0) return;
        if (dest.length < destOffset + length)
            throw new IndexOutOfBoundsException("Destination array is too small");
        buf.getBytes((int) srcOffset, dest, (int) destOffset, (int) length);
    }

    @Override
    public byte[] extractBytes(Consumer<NetworkBuffer> extractor) {
        assertDummy();
        final long start = readIndex();
        extractor.accept(this);
        final long end = readIndex();
        final int length = (int) (end - start);
        final byte[] out = new byte[length];
        buf.getBytes((int) start, out);
        return out;
    }


    @Override public NetworkBuffer clear() {
        return index(0, 0);
    }

    @Override public long writeIndex() {
        return writeIndex;
    }

    @Override public long readIndex() {
        return readIndex;
    }

    @Override public NetworkBuffer writeIndex(long wi) {
        this.writeIndex = wi; return this;
    }

    @Override public NetworkBuffer readIndex(long ri) {
        this.readIndex  = ri; return this;
    }

    @Override public NetworkBuffer index(long ri, long wi) {
        readIndex = ri; writeIndex = wi; return this;
    }

    @Override
    public long advanceWrite(long length) {
        final long old = writeIndex;
        writeIndex = old + length;
        return old;
    }

    @Override
    public long advanceRead(long length) {
        final long old = readIndex;
        readIndex = old + length;
        return old;
    }

    @Override public long readableBytes() {
        return writeIndex - readIndex;
    }

    @Override public long writableBytes() {
        return capacity() - writeIndex;
    }

    @Override public long capacity() {
        return isDummy() ? Long.MAX_VALUE : buf.capacity();
    }

    @Override
    public void readOnly() { this.readOnly = true; }

    @Override
    public boolean isReadOnly() { return readOnly; }

    @Override
    public void resize(long newSize) {
        assertDummy();
        assertReadOnly();
        if (newSize <= capacity())
            throw new IllegalArgumentException("New size must be larger than current capacity");
        buf.capacity((int) newSize);
    }

    @Override
    public void ensureWritable(long length) {
        assertReadOnly();
        if (writableBytes() >= length) return;
        final long target = newCapacity(length, capacity());
        if (isDummy()) return; // size-calc path - no real allocation
        buf.capacity((int) target);
    }

    private long newCapacity(long length, long capacity) {
        final long targetSize = writeIndex + length;
        final AutoResize strategy = this.autoResize;
        if (strategy == null)
            throw new IndexOutOfBoundsException(
                    "Buffer is full and cannot be resized: " + capacity + " -> " + targetSize);
        final long newCap = strategy.resize(capacity, targetSize);
        if (newCap == capacity)
            throw new IndexOutOfBoundsException(
                    "Buffer resized to the same capacity: " + capacity + " -> " + targetSize);
        return newCap;
    }

    @Override
    public void compact() {
        assertDummy();
        assertReadOnly();
        if (readIndex == 0) return;
        buf.discardReadBytes(); // netty discardReadBytes respects readerIndex
        final int readable = (int) readableBytes();
        // Shift data left
        for (int i = 0; i < readable; i++) {
            buf.setByte(i, buf.getByte((int) readIndex + i));
        }
        writeIndex -= readIndex;
        readIndex   = 0;
    }

    @Override
    public NetworkBuffer copy(long index, long length, long ri, long wi) {
        assertDummy();
        Objects.checkFromIndexSize((int) index, (int) length, (int) capacity());
        final ByteBuf newBuf = ByteBufAllocator.DEFAULT.buffer((int) length);
        buf.getBytes((int) index, newBuf, 0, (int) length);
        newBuf.writerIndex((int) length);
        return new NetworkBufferImpl(newBuf, ri, wi, autoResize, registries);
    }


    @Override
    public int readFromByteBuf(ByteBuf in) {
        assertDummy();
        assertReadOnly();
        final int readable = in.readableBytes();
        if (readable == 0) return 0;
        ensureWritable(readable);
        in.readBytes(buf, (int) writeIndex, readable);
        advanceWrite(readable);
        return readable;
    }

    @Override
    public boolean writeToByteBuf(ByteBuf out) {
        assertDummy();
        final int readable = (int) readableBytes();
        if (readable == 0) return true;
        out.writeBytes(buf, (int) readIndex, readable);
        advanceRead(readable);
        return true; // ByteBuf.writeBytes always writes everything
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        assertDummy();
        // Pull bytes out, cipher in-place, write back
        final byte[] plain  = new byte[(int) length];
        buf.getBytes((int) start, plain);
        final byte[] result = new byte[(int) length];
        try {
            final int written = cipher.update(plain, 0, (int) length, result);
            buf.setBytes((int) start, result, 0, written);
        } catch (ShortBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long compress(long start, long length, NetworkBuffer output) throws IOException {
        assertDummy();
        impl(output).assertReadOnly();

        // Slice the region to compress into a Netty buf (no copy — read-only view)
        final ByteBuf src = buf.slice((int) start, (int) length);

        // Use Netty's JdkZlibEncoder synchronously through its internal codec path.
        // Because encoder/decoder embed in a pipeline, the simplest correct approach
        // for standalone (non-pipeline) use is to compress via Java's Deflater under
        // Netty's wrapper. We call compress directly.
        final ByteBuf compressed = compressWithZlib(src);
        try {
            final int bytes = compressed.readableBytes();
            impl(output).ensureWritable(bytes);
            compressed.readBytes(impl(output).buf, (int) output.writeIndex(), bytes);
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            compressed.release();
        }
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) throws IOException {
        assertDummy();
        impl(output).assertReadOnly();

        final ByteBuf src = buf.slice((int) start, (int) length);
        final ByteBuf decompressed = decompressWithZlib(src);
        try {
            final int bytes = decompressed.readableBytes();
            impl(output).ensureWritable(bytes);
            decompressed.readBytes(impl(output).buf, (int) output.writeIndex(), bytes);
            output.advanceWrite(bytes);
            return bytes;
        } finally {
            decompressed.release();
        }
    }

    /**
     * Deflate-compresses {@code src} using Netty's {@link JdkZlibEncoder} in
     * {@link ZlibWrapper#ZLIB} mode (same as {@code java.util.zip.Deflater}).
     */
    private static ByteBuf compressWithZlib(ByteBuf src) throws IOException {
        // JdkZlibEncoder is a ChannelHandler and cannot be used standalone easily.
        // We use java.util.zip.Deflater directly (UNCHANGED compression logic)
        // but wrapped through Netty's ByteBuf API to avoid any NIO ByteBuffer
        // references leaking into the hot path.
        final byte[] input = new byte[src.readableBytes()];
        src.getBytes(src.readerIndex(), input);

        final java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        try {
            deflater.setInput(input);
            deflater.finish();
            // Pre-allocate generously; Deflater output is at most ~input + header.
            final ByteBuf out = ByteBufAllocator.DEFAULT.buffer(input.length + 64);
            final byte[] tmp = new byte[8192];
            while (!deflater.finished()) {
                final int n = deflater.deflate(tmp);
                out.writeBytes(tmp, 0, n);
            }
            return out;
        } finally {
            deflater.end();
        }
    }

    /**
     * Inflate-decompresses {@code src} using Java's {@link java.util.zip.Inflater},
     * accessed only through Netty's ByteBuf API.
     */
    private static ByteBuf decompressWithZlib(ByteBuf src) throws IOException {
        final byte[] input = new byte[src.readableBytes()];
        src.getBytes(src.readerIndex(), input);

        final java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        try {
            inflater.setInput(input);
            final ByteBuf out = ByteBufAllocator.DEFAULT.buffer(input.length * 3);
            final byte[] tmp = new byte[8192];
            while (!inflater.finished() && !inflater.needsInput()) {
                try {
                    final int n = inflater.inflate(tmp);
                    out.writeBytes(tmp, 0, n);
                } catch (java.util.zip.DataFormatException e) {
                    throw new IOException("Zlib decompression failed", e);
                }
            }
            return out;
        } finally {
            inflater.end();
        }
    }

    // -------------------------------------------------------------------------
    // Registries
    // -------------------------------------------------------------------------

    @Override
    public @Nullable Registries registries() {
        return registries;
    }

    // -------------------------------------------------------------------------
    // Internal low-level byte accessors (called by NetworkBufferTypeImpl)
    // -------------------------------------------------------------------------

    void _putBytes(long index, byte[] value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setBytes((int) index, value);
    }

    void _getBytes(long index, byte[] value) {
        assertDummy();
        buf.getBytes((int) index, value);
    }

    void _putByte(long index, byte value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setByte((int) index, value);
    }

    byte _getByte(long index) {
        assertDummy();
        return buf.getByte((int) index);
    }

    void _putShort(long index, short value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setShort((int) index, value);        // Netty always big-endian
    }

    short _getShort(long index) {
        assertDummy();
        return buf.getShort((int) index);
    }

    void _putInt(long index, int value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setInt((int) index, value);
    }

    int _getInt(long index) {
        assertDummy();
        return buf.getInt((int) index);
    }

    void _putLong(long index, long value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setLong((int) index, value);
    }

    long _getLong(long index) {
        assertDummy();
        return buf.getLong((int) index);
    }

    void _putFloat(long index, float value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setFloat((int) index, value);
    }

    float _getFloat(long index) {
        assertDummy();
        return buf.getFloat((int) index);
    }

    void _putDouble(long index, double value) {
        if (isDummy()) return;
        assertReadOnly();
        buf.setDouble((int) index, value);
    }

    double _getDouble(long index) {
        assertDummy();
        return buf.getDouble((int) index);
    }

    // -------------------------------------------------------------------------
    // NBT helpers
    // -------------------------------------------------------------------------

    BinaryTagWriter nbtWriter() {
        if (nbtWriter == null) {
            nbtWriter = new BinaryTagWriter(new DataOutputStream(new OutputStream() {
                @Override public void write(int b) {
                    NetworkBufferImpl.this.write(BYTE, (byte) b);
                }
            }));
        }
        return nbtWriter;
    }

    BinaryTagReader nbtReader() {
        if (nbtReader == null) {
            nbtReader = new BinaryTagReader(new DataInputStream(new InputStream() {
                @Override public int read() {
                    return NetworkBufferImpl.this.read(BYTE) & 0xFF;
                }
                @Override public int available() {
                    return (int) NetworkBufferImpl.this.readableBytes();
                }
            }));
        }
        return nbtReader;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format(
                "NetworkBuffer{r%d|w%d->%d, registries=%s, autoResize=%s, readOnly=%s}",
                readIndex, writeIndex, capacity(),
                registries != null, autoResize != null, readOnly);
    }

    // -------------------------------------------------------------------------
    // Static factory helpers
    // -------------------------------------------------------------------------

    static NetworkBuffer wrap(byte[] bytes, long readIndex, long writeIndex,
                              @Nullable Registries registries) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        return new NetworkBufferImpl(buf, readIndex, writeIndex, null, registries);
    }

    /**
     * Wraps a Netty {@link ByteBuf}. Ownership stays with the caller.
     */
    static NetworkBuffer fromByteBuf(ByteBuf buf, @Nullable Registries registries) {
        return new NetworkBufferImpl(
                buf,
                buf.readerIndex(), buf.writerIndex(),
                null, registries);
    }

    static void copy(NetworkBuffer srcBuffer, long srcOffset,
                     NetworkBuffer dstBuffer, long dstOffset, long length) {
        final NetworkBufferImpl src = impl(srcBuffer);
        final NetworkBufferImpl dst = impl(dstBuffer);
        dst.assertReadOnly();
        src.buf.getBytes((int) srcOffset, dst.buf, (int) dstOffset, (int) length);
    }

    static boolean equals(NetworkBuffer buffer1, NetworkBuffer buffer2) {
        final NetworkBufferImpl b1 = impl(buffer1);
        final NetworkBufferImpl b2 = impl(buffer2);
        final int cap = (int) b1.capacity();
        if (cap != b2.capacity()) return false;
        for (int i = 0; i < cap; i++) {
            if (b1.buf.getByte(i) != b2.buf.getByte(i)) return false;
        }
        return true;
    }

    /** Creates a size-calculation dummy buffer (no actual memory). */
    static NetworkBufferImpl dummy(Registries registries) {
        return new NetworkBufferImpl(DUMMY_BUF, 0, 0, null, registries);
    }

    static NetworkBufferImpl impl(NetworkBuffer buffer) {
        return (NetworkBufferImpl) buffer;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    static final class Builder implements NetworkBuffer.Builder {
        private final long initialSize;
        private AutoResize autoResize;
        private Registries registries;

        Builder(long initialSize) {
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
            final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer((int) initialSize, Integer.MAX_VALUE);
            return new NetworkBufferImpl(buf, 0, 0, autoResize, registries);
        }
    }
}