package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class CharacterArrayData extends DataType<char[]> {
    @Override
    public void encode(PacketWriter packetWriter, char[] value) {
        packetWriter.writeVarInt(value.length);
        for (char val : value) {
            packetWriter.writeChar(val);
        }
    }

    @Override
    public char[] decode(PacketReader packetReader) {
        char[] array = new char[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readChar();
        }
        return array;
    }
}
