package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class LongData extends DataType<Long> {
    @Override
    public void encode(PacketWriter packetWriter, Long value) {
        packetWriter.writeLong(value);
    }

    @Override
    public Long decode(PacketReader packetReader) {
        return packetReader.readLong();
    }
}
