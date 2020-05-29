package net.minestom.server.utils.nbt;

import net.minestom.server.network.packet.PacketWriter;

import static net.minestom.server.utils.nbt.NBT.*;

public class NbtWriter {

    private PacketWriter packet;

    public NbtWriter(PacketWriter packet) {
        this.packet = packet;
    }

    public void writeByte(String name, byte value) {
        packet.writeByte(NBT_BYTE);
        packet.writeShortSizedString(name);
        packet.writeByte(value);
    }

    public void writeShort(String name, short value) {
        packet.writeByte(NBT_SHORT);
        packet.writeShortSizedString(name);
        packet.writeShort(value);
    }

    public void writeInt(String name, int value) {
        packet.writeByte(NBT_INT);
        packet.writeShortSizedString(name);
        packet.writeInt(value);
    }

    public void writeLong(String name, long value) {
        packet.writeByte(NBT_LONG);
        packet.writeShortSizedString(name);
        packet.writeLong(value);
    }

    public void writeFloat(String name, float value) {
        packet.writeByte(NBT_FLOAT);
        packet.writeShortSizedString(name);
        packet.writeFloat(value);
    }

    public void writeDouble(String name, double value) {
        packet.writeByte(NBT_DOUBLE);
        packet.writeShortSizedString(name);
        packet.writeDouble(value);
    }

    // FIXME: not sure
    public void writeByteArray(String name, byte[] value) {
        packet.writeByte(NBT_BYTE_ARRAY);
        packet.writeShortSizedString(name);
        packet.writeInt(value.length);
        for (byte val : value) {
            packet.writeByte(val);
        }
    }

    public void writeString(String name, String value) {
        packet.writeByte(NBT_STRING);
        packet.writeShortSizedString(name);
        packet.writeShortSizedString(value);
    }

    public void writeList(String name, byte type, int size, Runnable callback) {
        packet.writeByte(NBT_LIST);
        packet.writeShortSizedString(name == null ? "" : name);
        packet.writeByte(type);
        packet.writeInt(size);
        callback.run();
        if (type == NBT_COMPOUND)
            packet.writeByte((byte) 0x00); // End compount
    }

    public void writeCompound(String name, NbtConsumer consumer) {
        packet.writeByte(NBT_COMPOUND);
        packet.writeShortSizedString(name == null ? "" : name);
        consumer.accept(this);
        packet.writeByte((byte) 0x00); // End compound
    }

}
