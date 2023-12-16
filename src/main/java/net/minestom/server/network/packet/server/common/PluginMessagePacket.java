package net.minestom.server.network.packet.server.common;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record PluginMessagePacket(String channel, byte[] data) implements ServerPacket {
    public PluginMessagePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(RAW_BYTES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, channel);
        writer.write(RAW_BYTES, data);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_PLUGIN_MESSAGE;
            case PLAY -> ServerPacketIdentifier.PLUGIN_MESSAGE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION, ConnectionState.PLAY);
        };
    }

    /**
     * Gets the current server brand name packet.
     * <p>
     * Sent to all players when the name changes.
     *
     * @return the current brand name packet
     */
    public static @NotNull PluginMessagePacket getBrandPacket() {
        final String brandName = MinecraftServer.getBrandName();
        final byte[] data = NetworkBuffer.makeArray(networkBuffer -> networkBuffer.write(STRING, brandName));
        return new PluginMessagePacket("minecraft:brand", data);
    }
}
