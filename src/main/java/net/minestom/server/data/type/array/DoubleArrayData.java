package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class DoubleArrayData extends DataType<double[]> {

    @Override
    public void encode(PacketWriter packetWriter, double[] value) {
        packetWriter.writeVarInt(value.length);
        for (double val : value) {
            packetWriter.writeDouble(val);
        }
    }

    @Override
    public double[] decode(PacketReader packetReader) {
        double[] array = new double[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readDouble();
        }
        return array;
    }
}
