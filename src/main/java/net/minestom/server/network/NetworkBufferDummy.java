package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import javax.crypto.Cipher;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

/**
 * A Dummy buffer is known as the dry run buffer for size calculations. It should support all write operations and fail on reads.
 * <br>
 * Note: A lot of runtime checks are removed, as this is not meant to catch exceptions.
 *
 * @see NetworkBuffer#dummy(Registries)
 */
final class NetworkBufferDummy implements NetworkBuffer, NetworkBuffer.Direct {
    private final @Nullable Registries registries;
    private long writeIndex;

    NetworkBufferDummy(long writeIndex, @Nullable Registries registries) {
        this.writeIndex = writeIndex;
        this.registries = registries;
        super();
    }

    @Contract("-> fail")
    public void assertDummy() {
        throw new UnsupportedOperationException("Buffer is a dummy buffer");
    }

    @Override
    public <T> T read(Type<T> type) throws IndexOutOfBoundsException {
        assertDummy();
        return null;
    }

    @Override
    public void copyTo(long srcOffset, byte[] dest, int destOffset, int length) {
        assertDummy();
    }

    // Copy forwarding into the same buffer is applicable.
    @Override
    public void copyTo(long srcOffset, NetworkBuffer destBuffer, long destOffset, long length) {
        if (destBuffer instanceof NetworkBufferDummy) return;
        assertDummy();
    }

    @Override
    public void fill(long srcOffset, long length, byte value) {
        // noop
    }

    @Override
    public byte[] extractReadBytes(Consumer<? super NetworkBuffer> extractor) {
        assertDummy();
        return null;
    }

    @Override
    public byte[] extractWrittenBytes(Consumer<? super NetworkBuffer> extractor) {
        assertDummy();
        return null;
    }

    @Override
    public long writeIndex() {
        return writeIndex;
    }

    @Contract("-> fail")
    @Override
    public long readIndex() {
        assertDummy();
        return 0;
    }

    @Override
    public NetworkBuffer writeIndex(long writeIndex) {
        this.writeIndex = writeIndex;
        return this;
    }

    @Override
    public NetworkBuffer readIndex(long readIndex) {
        assertDummy();
        return this;
    }

    @Override
    public @Range(from = 0, to = Long.MAX_VALUE) long capacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public NetworkBuffer readOnly() {
        return this;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(@Range(from = 0, to = Long.MAX_VALUE) long length) {
        assertDummy();
    }

    @Override
    public boolean requestCapacity(long length) {
        return false;
    }

    @Override
    public NetworkBuffer trimmed(NetworkBufferFactory factory) {
        return this;
    }

    @Override
    public NetworkBuffer copy(NetworkBufferFactory factory, long index, long length, long readIndex, long writeIndex) {
        assertDummy();
        return null;
    }

    @Override
    public NetworkBuffer slice(long index, long length, long readIndex, long writeIndex) {
        return new NetworkBufferDummy(writeIndex, registries);
    }

    @Override
    public int readChannel(ReadableByteChannel channel) {
        assertDummy();
        return 0;
    }

    @Override
    public boolean writeChannel(WritableByteChannel channel) {
        assertDummy();
        return false;
    }

    @Override
    public void cipher(Cipher cipher, long start, long length) {
        // noop
    }

    @Override
    public long compress(long start, long length, NetworkBuffer output) {
        if (output instanceof NetworkBufferDummy) return length;
        assertDummy();
        return 0;
    }

    @Override
    public long decompress(long start, long length, NetworkBuffer output) {
        if (output instanceof NetworkBufferDummy) return length;
        assertDummy();
        return 0;
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
        assertDummy();
        return false;
    }

    @Override
    public void putBytes(long index, byte[] value) {
        // noop
    }

    @Override
    public void getBytes(long index, byte[] value) {
        assertDummy();
    }

    @Override
    public void putByte(long index, byte value) {
        // noop
    }

    @Override
    public byte getByte(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putShort(long index, short value) {
        // noop
    }

    @Override
    public short getShort(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putInt(long index, int value) {
        // noop
    }

    @Override
    public int getInt(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putLong(long index, long value) {
        // noop
    }

    @Override
    public long getLong(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putFloat(long index, float value) {
        // noop
    }

    @Override
    public float getFloat(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putDouble(long index, double value) {
        // noop
    }

    @Override
    public double getDouble(long index) {
        assertDummy();
        return 0;
    }

    @Override
    public void putString(long index, String value) {
        // noop
    }

    @Override
    public String getString(long index) {
        assertDummy();
        return null;
    }

    @Override
    public String getString(long index, long byteLength) {
        assertDummy();
        return null;
    }

    @Override
    public String toString() {
        return String.format("NetworkBufferDummy{w%d, registries=%b}", writeIndex(), registries() != null);
    }
}
