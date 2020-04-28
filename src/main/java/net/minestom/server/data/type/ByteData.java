package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ByteData extends DataType<Byte> {
    @Override
    public void encode(PacketWriter packetWriter, Byte value) {
        packetWriter.writeByte(value);
    }

    @Override
    public Byte decode(PacketReader packetReader, byte[] value) {
        return packetReader.readByte();
    }
}
