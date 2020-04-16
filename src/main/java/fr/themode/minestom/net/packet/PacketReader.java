package fr.themode.minestom.net.packet;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.SerializerUtils;
import fr.themode.minestom.utils.Utils;
import io.netty.buffer.ByteBuf;

public class PacketReader {

    private ByteBuf buffer;
    private int length;

    public PacketReader(ByteBuf buffer, int length) {
        this.buffer = buffer;
        this.length = length;
    }

    public int readVarInt() {
        return Utils.readVarInt(buffer);
    }

    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    public byte readByte() {
        return buffer.readByte();
    }

    public short readShort() {
        return buffer.readShort();
    }

    public int readUnsignedShort() {
        return buffer.readUnsignedShort();
    }

    public int readInteger() {
        return buffer.readInt();
    }

    public long readLong() {
        return buffer.readLong();
    }

    public float readFloat() {
        return buffer.readFloat();
    }

    public double readDouble() {
        return buffer.readDouble();
    }

    public String readSizedString() {
        int length = readVarInt();
        ByteBuf buf = buffer.readBytes(length);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new String(bytes);
    }

    public String readShortSizedString() {
        short length = readShort();
        ByteBuf buf = buffer.readBytes(length);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new String(bytes);
    }

    public byte[] getRemainingBytes() {
        ByteBuf buf = buffer.readBytes(buffer.readableBytes());
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public BlockPosition readBlockPosition() {
        long value = buffer.readLong();
        return SerializerUtils.longToBlockPosition(value);
    }

    public ItemStack readSlot() {
        return Utils.readItemStack(this);
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    public int getPacketLength() {
        return length;
    }
}
