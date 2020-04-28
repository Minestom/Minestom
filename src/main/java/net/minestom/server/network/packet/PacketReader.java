package net.minestom.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;

public class PacketReader {

    private ByteBuf buffer;

    public PacketReader(ByteBuf buffer) {
        this.buffer = buffer;
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
        buf.release();
        return new String(bytes);
    }

    public String readShortSizedString() {
        short length = readShort();
        ByteBuf buf = buffer.readBytes(length);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return new String(bytes);
    }

    public byte[] getRemainingBytes() {
        ByteBuf buf = buffer.readBytes(buffer.readableBytes());
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
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
}
