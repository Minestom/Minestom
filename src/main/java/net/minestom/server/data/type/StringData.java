package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class StringData extends DataType<String> {

    @Override
    public void encode(PacketWriter packetWriter, String value) {
        packetWriter.writeSizedString(value);
    }

    @Override
    public String decode(PacketReader packetReader) {
        return packetReader.readSizedString();
    }
}
