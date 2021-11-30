package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record DisplayScoreboardPacket(byte position, String scoreName) implements ServerPacket {
    public DisplayScoreboardPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readSizedString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(position);
        writer.writeSizedString(scoreName);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DISPLAY_SCOREBOARD;
    }
}
