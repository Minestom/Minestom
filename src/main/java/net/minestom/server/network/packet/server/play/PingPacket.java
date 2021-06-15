package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class PingPacket implements ServerPacket {

    public int id;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.id = reader.readInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(id);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PING;
    }
}
