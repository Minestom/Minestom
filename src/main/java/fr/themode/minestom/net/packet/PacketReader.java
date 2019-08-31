package fr.themode.minestom.net.packet;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;

public class PacketReader {

    private Buffer buffer;

    public PacketReader(Buffer buffer) {
        this.buffer = buffer;
    }

    public int readVarInt() {
        return Utils.readVarInt(buffer);
    }

    public boolean readBoolean() {
        return buffer.getBoolean();
    }

    public byte readByte() {
        return buffer.getByte();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    public String readSizedString() {
        return Utils.readString(buffer);
    }

    public byte[] getRemainingBytes() {
        return buffer.getAllBytes();
    }

    public BlockPosition readBlockPosition() {
        return Utils.readPosition(buffer);
    }

}
