package net.minestom.server.data;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public abstract class DataType<T> {

    public abstract void encode(PacketWriter packetWriter, T value);

    public abstract T decode(PacketReader packetReader, byte[] value);

}