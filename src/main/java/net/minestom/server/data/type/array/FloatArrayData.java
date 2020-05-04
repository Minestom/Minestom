package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class FloatArrayData extends DataType<float[]> {

    @Override
    public void encode(PacketWriter packetWriter, float[] value) {
        packetWriter.writeVarInt(value.length);
        for (float val : value) {
            packetWriter.writeFloat(val);
        }
    }

    @Override
    public float[] decode(PacketReader packetReader) {
        float[] array = new float[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readFloat();
        }
        return array;
    }
}
