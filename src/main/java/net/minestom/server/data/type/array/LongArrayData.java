package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class LongArrayData extends DataType<long[]> {

    @Override
    public void encode(PacketWriter packetWriter, long[] value) {
        packetWriter.writeVarInt(value.length);
        for (long val : value) {
            packetWriter.writeLong(val);
        }
    }

    @Override
    public long[] decode(PacketReader packetReader) {
        long[] array = new long[packetReader.readVarInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = packetReader.readLong();
        }
        return array;
    }
}
