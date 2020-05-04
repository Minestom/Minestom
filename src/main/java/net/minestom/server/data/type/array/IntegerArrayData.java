package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class IntegerArrayData extends DataType<int[]> {

    @Override
    public void encode(PacketWriter packetWriter, int[] value) {
        packetWriter.writeVarInt(value.length);
        for (int val : value) {
            packetWriter.writeInt(val);
        }
    }

    @Override
    public int[] decode(PacketReader packetReader) {
        int[] array = new int[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readInteger();
        }
        return array;
    }
}
