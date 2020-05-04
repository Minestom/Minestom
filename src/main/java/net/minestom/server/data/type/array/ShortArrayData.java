package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ShortArrayData extends DataType<short[]> {

    @Override
    public void encode(PacketWriter packetWriter, short[] value) {
        packetWriter.writeVarInt(value.length);
        for (short val : value) {
            packetWriter.writeShort(val);
        }
    }

    @Override
    public short[] decode(PacketReader packetReader) {
        short[] array = new short[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readShort();
        }
        return array;
    }
}
