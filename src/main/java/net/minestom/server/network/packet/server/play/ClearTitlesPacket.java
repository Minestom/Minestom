package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClearTitlesPacket(boolean reset) implements ServerPacket {
    public ClearTitlesPacket(BinaryReader reader) {
        this(reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(reset);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CLEAR_TITLES;
    }
}
