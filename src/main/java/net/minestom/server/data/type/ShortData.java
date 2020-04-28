package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ShortData extends DataType<Short> {

    @Override
    public void encode(PacketWriter packetWriter, Short value) {
        packetWriter.writeShort(value);
    }

    @Override
    public Short decode(PacketReader packetReader) {
        return packetReader.readShort();
    }
}
