package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class IntegerData extends DataType<Integer> {

    @Override
    public void encode(PacketWriter packetWriter, Integer value) {
        packetWriter.writeVarInt(value);
    }

    @Override
    public Integer decode(PacketReader packetReader, byte[] value) {
        return packetReader.readVarInt();
    }
}