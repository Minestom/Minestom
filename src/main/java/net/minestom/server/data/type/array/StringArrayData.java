package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class StringArrayData extends DataType<String[]> {

    @Override
    public void encode(PacketWriter packetWriter, String[] value) {
        packetWriter.writeVarInt(value.length);
        for (String val : value) {
            packetWriter.writeSizedString(val);
        }
    }

    @Override
    public String[] decode(PacketReader packetReader) {
        String[] array = new String[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readSizedString();
        }
        return array;
    }
}
