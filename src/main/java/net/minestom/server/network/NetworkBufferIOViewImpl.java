package net.minestom.server.network;

import java.util.Arrays;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Implementation of the {@link NetworkBuffer.IOView} interface.
 * <br>
 * Note: Big endian byte order is used for all read/write operations.
 *
 * @param buffer the buffer to read from and write to
 */
record NetworkBufferIOViewImpl(NetworkBuffer buffer) implements NetworkBuffer.IOView {
    NetworkBufferIOViewImpl {
        Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    public void readFully(byte[] b) {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        buffer.ensureReadable(len);
        buffer.copyTo(buffer.readIndex(), b, off, len);
        buffer.advanceRead(len);
    }

    @Override
    public int skipBytes(int n) {
        var readableBytes = buffer.readableBytes();
        if (n > readableBytes) {
            n = (int) readableBytes;
        }
        if (n > 0) buffer.advanceRead(n);
        return n;
    }

    @Override
    public boolean readBoolean() {
        return buffer.read(BOOLEAN);
    }

    @Override
    public byte readByte() {
        return buffer.read(BYTE);
    }

    @Override
    public int readUnsignedByte() {
        return buffer.read(UNSIGNED_BYTE);
    }

    @Override
    public short readShort() {
        return buffer.read(SHORT);
    }

    @Override
    public int readUnsignedShort() {
        return buffer.read(UNSIGNED_SHORT);
    }

    @Override
    public char readChar() {
        return (char) (buffer.read(SHORT) & 0xFFFF);
    }

    @Override
    public int readInt() {
        return buffer.read(INT);
    }

    @Override
    public long readLong() {
        return buffer.read(LONG);
    }

    @Override
    public float readFloat() {
        return buffer.read(FLOAT);
    }

    @Override
    public double readDouble() {
        return buffer.read(DOUBLE);
    }

    @Override
    public String readLine() {
        throw new UnsupportedOperationException("Deprecated method readLine() called, not implemented");
    }

    @Override
    public String readUTF() {
        return buffer.read(STRING_IO_UTF8);
    }

    @Override
    public void write(int b) {
        buffer.write(BYTE, (byte) b);
    }

    @Override
    public void write(byte[] b) {
        buffer.write(RAW_BYTES, b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.write(RAW_BYTES, Arrays.copyOfRange(b, off, off + len));
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.write(BOOLEAN, value);
    }

    @Override
    public void writeByte(int value) {
        buffer.write(BYTE, (byte) value);
    }

    @Override
    public void writeShort(int value) {
        buffer.write(SHORT, (short) value);
    }

    @Override
    public void writeChar(int value) {
        buffer.write(SHORT, (short) value);
    }

    @Override
    public void writeInt(int value) {
        buffer.write(INT, value);
    }

    @Override
    public void writeLong(long value) {
        buffer.write(LONG, value);
    }

    @Override
    public void writeFloat(float value) {
        buffer.write(FLOAT, value);
    }

    @Override
    public void writeDouble(double value) {
        buffer.write(DOUBLE, value);
    }

    @Override
    public void writeBytes(String string) {
        Objects.requireNonNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            buffer.write(BYTE, (byte) string.charAt(i)); // Low byte only
        }
    }

    @Override
    public void writeChars(String string) {
        Objects.requireNonNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            buffer.write(SHORT, (short) string.charAt(i));
        }
    }

    @Override
    public void writeUTF(String string) {
        Objects.requireNonNull(string, "String cannot be null!");
        buffer.write(STRING_IO_UTF8, string);
    }
}
