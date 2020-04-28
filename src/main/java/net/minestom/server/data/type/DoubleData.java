package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class DoubleData extends DataType<Double> {

    @Override
    public void encode(PacketWriter packetWriter, Double value) {
        packetWriter.writeDouble(value);
    }

    @Override
    public Double decode(PacketReader packetReader, byte[] value) {
        return packetReader.readDouble();
    }
}
