package fr.themode.minestom.net.packet;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class PacketWriter {

    private Buffer buffer;

    public PacketWriter(Buffer buffer) {
        this.buffer = buffer;
    }

    public void writeBoolean(boolean b) {
        buffer.putBoolean(b);
    }

    public void writeByte(byte b) {
        buffer.putByte(b);
    }

    public void writeShort(short s) {
        buffer.putShort(s);
    }

    public void writeInt(int i) {
        buffer.putInt(i);
    }

    public void writeLong(long l) {
        buffer.putLong(l);
    }

    public void writeFloat(float f) {
        buffer.putFloat(f);
    }

    public void writeDouble(double d) {
        buffer.putDouble(d);
    }

    public void writeVarInt(int i) {
        Utils.writeVarInt(buffer, i);
    }

    public void writeSizedString(String string) {
        Utils.writeString(buffer, string);
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
        buffer.putBytes(bytes);
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

    public void writeBuffer(Buffer buffer) {
        this.buffer.putBuffer(buffer);
    }

    public void writeUuid(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeBlockPosition(BlockPosition blockPosition) {
        Utils.writePosition(buffer, blockPosition);
    }

    public void writeBlockPosition(int x, int y, int z) {
        Utils.writePosition(buffer, x, y, z);
    }

    public void writeItemStack(ItemStack itemStack) {
        Utils.writeItemStack(buffer, itemStack);
    }

}
