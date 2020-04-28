package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class CharacterData extends DataType<Character> {

    @Override
    public void encode(PacketWriter packetWriter, Character value) {
        packetWriter.writeChar(value);
    }

    @Override
    public Character decode(PacketReader packetReader, byte[] value) {
        return packetReader.readChar();
    }
}
