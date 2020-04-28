package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class BooleanData extends DataType<Boolean> {
    @Override
    public void encode(PacketWriter packetWriter, Boolean value) {
        packetWriter.writeBoolean(value);
    }

    @Override
    public Boolean decode(PacketReader packetReader, byte[] value) {
        return packetReader.readBoolean();
    }
}
