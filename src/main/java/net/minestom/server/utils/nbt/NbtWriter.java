package net.minestom.server.utils.nbt;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.utils.nbt.NBT.*;

public class NbtWriter {

    private PacketWriter packet;

    public NbtWriter(PacketWriter packet) {
        this.packet = packet;
    }

    public void writeByte(String name, byte value) {
        writeHeader(NBT_BYTE, name);
        packet.writeByte(value);
    }

    public void writeShort(String name, short value) {
        writeHeader(NBT_SHORT, name);
        packet.writeShort(value);
    }

    public void writeInt(String name, int value) {
        writeHeader(NBT_INT, name);
        packet.writeInt(value);
    }

    public void writeLong(String name, long value) {
        writeHeader(NBT_LONG, name);
        packet.writeLong(value);
    }

    public void writeFloat(String name, float value) {
        writeHeader(NBT_FLOAT, name);
        packet.writeFloat(value);
    }

    public void writeDouble(String name, double value) {
        writeHeader(NBT_DOUBLE, name);
        packet.writeDouble(value);
    }

    public void writeByteArray(String name, byte[] value) {
        writeHeader(NBT_BYTE_ARRAY, name);
        packet.writeInt(value.length);
        for (byte val : value) {
            packet.writeByte(val);
        }
    }

    public void writeString(String name, String value) {
        writeHeader(NBT_STRING, name);
        packet.writeShortSizedString(value);
    }

    public void writeList(String name, byte type, int size, Runnable callback) {
        writeHeader(NBT_LIST, name);
        packet.writeByte(type);
        packet.writeInt(size);
        callback.run();
        if (type == NBT_COMPOUND)
            packet.writeByte((byte) 0x00); // End compount
    }

    public void writeCompound(String name, NbtConsumer consumer) {
        writeHeader(NBT_COMPOUND, name);
        consumer.accept(this);
        packet.writeByte((byte) 0x00); // End compound
    }

    public void writeIntArray(String name, int[] value) {
        writeHeader(NBT_INT_ARRAY, name);
        packet.writeInt(value.length);
        for (int val : value) {
            packet.writeInt(val);
        }
    }

    public void writeLongArray(String name, long[] value) {
        writeHeader(NBT_LONG_ARRAY, name);
        packet.writeInt(value.length);
        for (long val : value) {
            packet.writeLong(val);
        }
    }

    private void writeHeader(byte type, String name) {
        Check.argCondition(!MathUtils.isBetween(type, NBT_BYTE, NBT_LONG_ARRAY),
                "The NbtTag type " + type + " is not valid");
        Check.notNull(name, "The NbtTag name cannot be null");
        packet.writeByte(type);
        packet.writeShortSizedString(name);
    }

}
