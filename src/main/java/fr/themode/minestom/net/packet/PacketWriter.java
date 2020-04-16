package fr.themode.minestom.net.packet;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.utils.buffer.BufferWrapper;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

public class PacketWriter {

    private Packet packet;

    public PacketWriter(Packet packet) {
        this.packet = packet;
    }

    public void writeBoolean(boolean b) {
        packet.putBoolean(b);
    }

    public void writeByte(byte b) {
        packet.putByte(b);
    }

    public void writeShort(short s) {
        packet.putShort(s);
    }

    public void writeInt(int i) {
        packet.putInt(i);
    }

    public void writeLong(long l) {
        packet.putLong(l);
    }

    public void writeFloat(float f) {
        packet.putFloat(f);
    }

    public void writeDouble(double d) {
        packet.putDouble(d);
    }

    public void writeVarInt(int i) {
        Utils.writeVarInt(packet, i);
    }

    public void writeSizedString(String string) {
        Utils.writeString(packet, string);
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
        packet.putBytes(bytes);
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
        int size = buffer.getSize();
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
        Utils.writePosition(packet, blockPosition);
    }

    public void writeBlockPosition(int x, int y, int z) {
        Utils.writePosition(packet, x, y, z);
    }

    public void writeItemStack(ItemStack itemStack) {
        Utils.writeItemStack(packet, itemStack);
    }

}
