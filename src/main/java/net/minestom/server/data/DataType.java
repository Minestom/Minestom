package net.minestom.server.data;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public abstract class DataType<T> {

    /**
     * Encode the data type
     *
     * @param packetWriter the data writer
     * @param value        the value to encode
     */
    public abstract void encode(PacketWriter packetWriter, T value);

    /**
     * Decode the data type
     *
     * @param packetReader the data reader
     * @return the decoded value
     */
    public abstract T decode(PacketReader packetReader);

}