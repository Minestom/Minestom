package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class BooleanArrayData extends DataType<boolean[]> {

    @Override
    public void encode(PacketWriter packetWriter, boolean[] value) {
        packetWriter.writeVarInt(value.length);
        for (boolean val : value) {
            packetWriter.writeBoolean(val);
        }
    }

    @Override
    public boolean[] decode(PacketReader packetReader) {
        boolean[] array = new boolean[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readBoolean();
        }
        return array;
    }
}
