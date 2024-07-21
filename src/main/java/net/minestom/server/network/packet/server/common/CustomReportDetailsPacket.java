package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record CustomReportDetailsPacket(
        @NotNull Map<String, String> details
) implements ServerPacket.Configuration, ServerPacket.Play {
    private static final int MAX_DETAILS = 32;

    public CustomReportDetailsPacket {
        details = Map.copyOf(details);
    }

    public CustomReportDetailsPacket(@NotNull NetworkBuffer reader) {
        this(reader.readMap(NetworkBuffer.STRING, NetworkBuffer.STRING, MAX_DETAILS));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeMap(NetworkBuffer.STRING, NetworkBuffer.STRING, details);
    }

}
