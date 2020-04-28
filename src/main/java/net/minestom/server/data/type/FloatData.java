package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class FloatData extends DataType<Float> {

    @Override
    public void encode(PacketWriter packetWriter, Float value) {
        packetWriter.writeFloat(value);
    }

    @Override
    public Float decode(PacketReader packetReader) {
        return packetReader.readFloat();
    }
}
