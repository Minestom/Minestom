package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
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
    public int getId() {
        return ServerPacketIdentifier.PLUGIN_MESSAGE;
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
