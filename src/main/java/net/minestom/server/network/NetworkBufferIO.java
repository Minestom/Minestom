package net.minestom.server.network;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;

/**
 * Self-contained interface that extends {@link NetworkBuffer} and {@link DataInput} and {@link DataOutput} for mostly reading/writing binary tags.
 * <p>
 * This interface is separate from {@link NetworkBuffer} because we don't want DataInput and DataOutput to be part of the public API.
 * This interface is also hidden public API because it is intended to be used outside the Minestom project. For example, to interface with adventure.
 */
@ApiStatus.Experimental
public sealed interface NetworkBufferIO extends NetworkBuffer, DataInput, DataOutput permits NetworkBufferImpl {
    // TODO write tests

    /**
     * Creates a new {@link OutputStream} for this {@link NetworkBuffer}.
     * @return the new data output
     */
    @Contract(pure = true)
    default OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                NetworkBufferIO.this.write(BYTE, (byte) b);
            }

            @Override
            public void write(byte @NotNull [] b, int off, int len) {
                NetworkBufferIO.this.write(RAW_BYTES, Arrays.copyOfRange(b, off, len));
            }
        };
    }

    /**
     * Creates a new {@link InputStream} for this {@link NetworkBuffer}.
     * @return the new data input
     */
    @Contract(pure = true)
    default InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return NetworkBufferIO.this.read(BYTE) & 0xFF;
            }

            @Override
            public int read(byte @NotNull [] b, int off, int len) {
                // We will let the caller get an exception if the length is invalid.
                // TODO Not a compliant impl of InputStream.
                var newBytes = NetworkBufferIO.this.read(NetworkBuffer.FixedRawBytes(len - off));
                System.arraycopy(newBytes, 0, b, off, len);
                return newBytes.length;
            }

            @Override
            public long skip(long n) {
                var currentReadIndex = readIndex();
                var readableBytes = readableBytes();
                if (currentReadIndex + n > readableBytes) {
                    n = Math.toIntExact(readableBytes - currentReadIndex);
                }
                if (n > 0) advanceRead(n);
                return n;
            }

            @Override
            public void skipNBytes(long n) throws IOException {
                var shouldSkipTo = readIndex() + n;
                var skippedTo = Math.min(shouldSkipTo, readableBytes());
                if (skippedTo != shouldSkipTo) {
                    throw new EOFException("Not enough bytes to skip");
                }
                // Otherwise advance the read.
                advanceRead(n);
            }

            @Override
            public int available() {
                return (int) readableBytes();
            }
        };
    }

    @Override
    default void readFully(byte @NotNull [] b) {
        var newBytes = read(RAW_BYTES);
        System.arraycopy(newBytes, 0, b, 0, b.length);
    }

    @Override
    default void readFully(byte @NotNull [] b, int off, int len) {
        var newBytes = read(RAW_BYTES);
        System.arraycopy(newBytes, 0, b, off, len);
    }

    @Override
    default int skipBytes(int n) {
        var currentReadIndex = readIndex();
        var readableBytes = readableBytes();
        if (currentReadIndex + n > readableBytes) {
            n = Math.toIntExact(readableBytes - currentReadIndex);
        }
        if (n > 0) advanceRead(n);
        return n;
    }

    @Override
    default boolean readBoolean() {
        return read(BOOLEAN);
    }

    @Override
    default byte readByte() {
        return read(BYTE);
    }

    @Override
    default int readUnsignedByte() {
        return read(BYTE) & 0xFF;
    }

    @Override
    default short readShort() {
        return read(SHORT);
    }

    @Override
    default int readUnsignedShort() {
        return read(SHORT) & 0xFFFF;
    }

    @Override
    default char readChar() {
        return (char) (read(SHORT) & 0xFFFF);
    }

    @Override
    default int readInt() {
        return read(INT);
    }

    @Override
    default long readLong() {
        return read(LONG);
    }

    @Override
    default float readFloat() {
        return read(FLOAT);
    }

    @Override
    default double readDouble() {
        return read(DOUBLE);
    }

    @Override
    @Deprecated
    default String readLine() {
        throw new UnsupportedOperationException("Deprecated method readLine() called, not implemented");
    }

    @Override
    @NotNull
    default String readUTF() {
        return read(STRING_IO_UTF8);
    }

    @Override
    default void write(int b) {
        write(BYTE, (byte) b);
    }

    @Override
    default void write(byte @NotNull [] b) {
        write(RAW_BYTES, b);
    }

    @Override
    default void write(byte @NotNull [] b, int off, int len) {
        write(RAW_BYTES, Arrays.copyOfRange(b, off, len));
    }

    @Override
    default void writeBoolean(boolean value) {
        write(BOOLEAN, value);
    }

    @Override
    default void writeByte(int value) {
        write(BYTE, (byte) value);
    }

    @Override
    default void writeShort(int value) {
        write(SHORT, (short) value);
    }

    @Override
    default void writeChar(int value) {
        write(BYTE, (byte) value);
    }

    @Override
    default void writeInt(int value) {
        write(INT, value);
    }

    @Override
    default void writeLong(long value) {
        write(LONG, value);
    }

    @Override
    default void writeFloat(float value) {
        write(FLOAT, value);
    }

    @Override
    default void writeDouble(double value) {
        write(DOUBLE, value);
    }

    @Override
    default void writeBytes(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            write(BYTE, (byte) string.charAt(i)); // Low byte only
        }
    }

    @Override
    default void writeChars(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        for (int i = 0; i < string.length(); i++) {
            write(SHORT, (short) string.charAt(i));
        }
    }

    @Override
    default void writeUTF(@NotNull String string) {
        Check.notNull(string, "String cannot be null!");
        write(STRING_IO_UTF8, string);
    }
}
