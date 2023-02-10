package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record WorldBorderWarningReachPacket(int warningBlocks) implements ServerPacket {
    public WorldBorderWarningReachPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, warningBlocks);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WORLD_BORDER_WARNING_REACH;
    }
}
