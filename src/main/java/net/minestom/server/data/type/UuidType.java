package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

import java.util.UUID;

public class UuidType extends DataType<UUID> {
    @Override
    public void encode(PacketWriter packetWriter, UUID value) {
        packetWriter.writeUuid(value);
    }

    @Override
    public UUID decode(PacketReader packetReader) {
        return packetReader.readUuid();
    }
}
