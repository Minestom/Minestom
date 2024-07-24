package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record PluginMessagePacket(String channel,
                                  byte[] data) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<PluginMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, PluginMessagePacket::channel,
            RAW_BYTES, PluginMessagePacket::data,
            PluginMessagePacket::new);

    /**
     * Gets the current server brand name packet.
     * <p>
     * Sent to all players when the name changes.
     *
     * @return the current brand name packet
     */
    public static @NotNull PluginMessagePacket brandPacket(String brandName) {
        final byte[] data = NetworkBuffer.makeArray(networkBuffer -> networkBuffer.write(STRING, brandName));
        return new PluginMessagePacket("minecraft:brand", data);
    }
}
