package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ByteArrayData extends DataType<byte[]> {
    @Override
    public void encode(PacketWriter packetWriter, byte[] value) {
        packetWriter.writeVarInt(value.length);
        for (byte val : value) {
            packetWriter.writeByte(val);
        }
    }

    @Override
    public byte[] decode(PacketReader packetReader) {
        byte[] array = new byte[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readByte();
        }
        return array;
    }
}
