package net.minestom.server.utils.buffer;

import net.minestom.server.utils.Utils;

import java.nio.ByteBuffer;

public class BufferWrapper {

    private final ByteBuffer byteBuffer;
    private int size;

    protected BufferWrapper(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public void putByte(byte b) {
        this.byteBuffer.put(b);
        size += Byte.BYTES;
    }

    public void putShort(short s) {
        this.byteBuffer.putShort(s);
        size += Short.BYTES;
    }

    public void putInt(int n) {
        this.byteBuffer.putInt(n);
        size += Integer.BYTES;
    }

    public void putLong(long l) {
        this.byteBuffer.putLong(l);
        size += Long.BYTES;
    }

    public void putVarInt(int n) {
        Utils.writeVarIntBuffer(this, n);
        size += Utils.getVarIntSize(n);
    }

    public void putBytes(byte[] bytes) {
        this.byteBuffer.put(bytes);
        size += Byte.BYTES * bytes.length;
    }

    public void free() {
        BufferUtils.giveBuffer(getByteBuffer());
    }

    public int getSize() {
        return size;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
