package net.minestom.server.network;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Implementation of the {@link NetworkBuffer.IOView} interface.
 * @param buffer the buffer to read from and write to
 * @implNote Big endian byte order is used for all read/write operations.
 *
 */
record NetworkBufferIOViewImpl(@NotNull NetworkBuffer buffer) implements NetworkBuffer.IOView {
    @Override
    public @NotNull OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                buffer.write(BYTE, (byte) b);
            }

            @Override
            public void write(byte @NotNull [] b, int off, int len) {
                buffer.write(RAW_BYTES, Arrays.copyOfRange(b, off, len));
            }
        };
    }

    @Override
    public @NotNull InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return buffer.read(BYTE) & 0xFF;
            }

            @Override
            public int read(byte @NotNull [] b, int off, int len) {
                // We will let the caller get an exception if the length is invalid.
                // TODO Not a compliant impl of InputStream.
                var newBytes = buffer.read(NetworkBuffer.FixedRawBytes(len - off));
                System.arraycopy(newBytes, 0, b, off, len);
                return newBytes.length;
            }

            @Override
            public long skip(long n) {
                var currentReadIndex = buffer.readIndex();
                var readableBytes = buffer.readableBytes();
                if (currentReadIndex + n > readableBytes) {
                    n = readableBytes - currentReadIndex;
                }
                if (n > 0) buffer.advanceRead(n);
                return n;
            }

            @Override
            public void skipNBytes(long n) throws IOException {
                var shouldSkipTo = buffer.readIndex() + n;
                var skippedTo = Math.min(shouldSkipTo, buffer.readableBytes());
                if (skippedTo != shouldSkipTo) {
                    throw new EOFException("Not enough bytes to skip");
                }
                // Otherwise advance the read.
                buffer.advanceRead(n);
            }

            @Override
            public int available() {
                return (int) buffer.readableBytes();
            }
        };
    }

    @Override
    public void readFully(byte @NotNull [] b) {
        var newBytes = buffer.read(RAW_BYTES);
        System.arraycopy(newBytes, 0, b, 0, b.length);
    }

    @Override
    public void readFully(byte @NotNull [] b, int off, int len) {
        var newBytes = buffer.read(RAW_BYTES);
        System.arraycopy(newBytes, 0, b, off, len);
    }

    @Override
    public int skipBytes(int n) {
        var currentReadIndex = buffer.readIndex();
        var readableBytes = buffer.readableBytes();
        if (currentReadIndex + n > readableBytes) {
            n = Math.toIntExact(readableBytes - currentReadIndex);
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
        return buffer.read(BYTE) & 0xFF;
    }

    @Override
    public short readShort() {
        return buffer.read(SHORT);
    }

    @Override
    public int readUnsignedShort() {
        return buffer.read(SHORT) & 0xFFFF;
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
    @Deprecated
    public String readLine() {
        throw new UnsupportedOperationException("Deprecated method readLine() called, not implemented");
    }

    @Override
    @NotNull
    public String readUTF() {
        return buffer.read(STRING_IO_UTF8);
    }

    @Override
    public void write(int b) {
        buffer.write(BYTE, (byte) b);
    }

    @Override
    public void write(byte @NotNull [] b) {
        buffer.write(RAW_BYTES, b);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        buffer.write(RAW_BYTES, Arrays.copyOfRange(b, off, len));
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
        buffer.write(BYTE, (byte) value);
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
    public void writeBytes(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            buffer.write(BYTE, (byte) string.charAt(i)); // Low byte only
        }
    }

    @Override
    public void writeChars(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            buffer.write(SHORT, (short) string.charAt(i));
        }
    }

    @Override
    public void writeUTF(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        buffer.write(STRING_IO_UTF8, string);
    }
}
