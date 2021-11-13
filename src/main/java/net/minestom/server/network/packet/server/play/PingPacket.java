package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PingPacket(int id) implements ServerPacket {
    public PingPacket(BinaryReader reader) {
        this(reader.readInt());
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
