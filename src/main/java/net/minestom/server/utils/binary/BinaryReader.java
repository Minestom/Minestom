package net.minestom.server.utils.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Class used to read from a byte array
 * WARNING: not thread-safe
 */
public class BinaryReader extends InputStream {

    private final ByteBuf buffer;
    private final NBTReader nbtReader = new NBTReader(this, false);

    public BinaryReader(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public BinaryReader(byte[] bytes) {
        this(Unpooled.wrappedBuffer(bytes));
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

    public char readChar() {
        return buffer.readChar();
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
        final int length = readVarInt();
        final byte[] bytes = readBytes(length);
        return new String(bytes);
    }

    public String readShortSizedString() {
        final short length = readShort();
        final byte[] bytes = readBytes(length);
        return new String(bytes);
    }

    public byte[] readBytes(int length) {
        ByteBuf buf = buffer.readBytes(length);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return bytes;
    }

    public byte[] getRemainingBytes() {
        return readBytes(buffer.readableBytes());
    }

    public BlockPosition readBlockPosition() {
        final long value = buffer.readLong();
        return SerializerUtils.longToBlockPosition(value);
    }

    public UUID readUuid() {
        final long most = readLong();
        final long least = readLong();
        return new UUID(most, least);
    }

    public ItemStack readSlot() {
        return NBTUtils.readItemStack(this);
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    @Override
    public int read() {
        return readByte() & 0xFF;
    }

    @Override
    public int available() {
        return buffer.readableBytes();
    }

    public NBT readTag() throws IOException, NBTException {
        return nbtReader.read();
    }
}
