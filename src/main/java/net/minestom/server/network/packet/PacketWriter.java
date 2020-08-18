package net.minestom.server.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.buffer.BufferWrapper;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

public class PacketWriter extends OutputStream {

    private final ByteBuf buffer = Unpooled.buffer();
    private final NBTWriter nbtWriter = new NBTWriter(this, false);

    public void writeBoolean(boolean b) {
        buffer.writeBoolean(b);
    }

    public void writeByte(byte b) {
        buffer.writeByte(b);
    }

    public void writeChar(char s) {
        buffer.writeChar(s);
    }

    public void writeShort(short s) {
        buffer.writeShort(s);
    }

    public void writeInt(int i) {
        buffer.writeInt(i);
    }

    public void writeLong(long l) {
        buffer.writeLong(l);
    }

    public void writeFloat(float f) {
        buffer.writeFloat(f);
    }

    public void writeDouble(double d) {
        buffer.writeDouble(d);
    }

    public void writeVarInt(int i) {
        Utils.writeVarInt(this, i);
    }

    public void writeVarLong(long l) {
        Utils.writeVarLong(this, l);
    }

    public void writeSizedString(String string) {
        byte[] bytes;
        bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }

    public void writeShortSizedString(String string) {
        byte[] bytes;
        bytes = string.getBytes(StandardCharsets.UTF_8);
        writeShort((short) bytes.length);
        writeBytes(bytes);
    }

    public void writeVarIntArray(int[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (int element : array) {
            writeVarInt(element);
        }
    }

    public void writeBytes(byte[] bytes) {
        buffer.writeBytes(bytes);
    }

    public void writeStringArray(String[] array) {
        if (array == null) {
            writeVarInt(0);
            return;
        }
        writeVarInt(array.length);
        for (String element : array) {
            writeSizedString(element);
        }
    }

    public void write(Consumer<PacketWriter> consumer) {
        if (consumer != null)
            consumer.accept(this);
    }

    public void writeBufferAndFree(BufferWrapper buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        final int size = buffer.getSize();
        byte[] cache = new byte[size];
        byteBuffer.position(0).get(cache, 0, size);
        writeBytes(cache);
        buffer.free();
    }

    public void writeUuid(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeBlockPosition(BlockPosition blockPosition) {
        writeBlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public void writeBlockPosition(int x, int y, int z) {
        writeLong(SerializerUtils.positionToLong(x, y, z));
    }

    public void writeItemStack(ItemStack itemStack) {
        NBTUtils.writeItemStack(this, itemStack);
    }

    public void writeNBT(String name, NBT tag) {
        try {
            nbtWriter.writeNamed("", tag);
        } catch (IOException e) {
            // should not throw, as nbtWriter points to this PacketWriter
            e.printStackTrace();
        }
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[buffer.readableBytes()];
        final int readerIndex = buffer.readerIndex();
        buffer.getBytes(readerIndex, bytes);
        return bytes;
    }

    @Override
    public void write(int b) {
        writeByte((byte) b);
    }
}
