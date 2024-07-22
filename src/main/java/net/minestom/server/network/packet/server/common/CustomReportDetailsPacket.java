package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record CustomReportDetailsPacket(
        @NotNull Map<String, String> details
) implements ServerPacket.Configuration, ServerPacket.Play {
    private static final int MAX_DETAILS = 32;

    public static NetworkBuffer.Type<CustomReportDetailsPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, CustomReportDetailsPacket packet) {
            writer.writeMap(NetworkBuffer.STRING, NetworkBuffer.STRING, packet.details);
        }

        @Override
        public CustomReportDetailsPacket read(@NotNull NetworkBuffer reader) {
            return new CustomReportDetailsPacket(reader.readMap(NetworkBuffer.STRING, NetworkBuffer.STRING, MAX_DETAILS));
        }
    };

    public CustomReportDetailsPacket {
        details = Map.copyOf(details);
    }
}
