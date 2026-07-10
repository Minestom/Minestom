package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Arrays;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record PluginMessagePacket(String channel,
                                  byte[] data) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<PluginMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, PluginMessagePacket::channel,
            RAW_BYTES, PluginMessagePacket::data,
            PluginMessagePacket::new);

    public PluginMessagePacket {
        data = data.clone();
    }

    /**
     * Gets the current server brand name packet.
     * <p>
     * Sent to all players when the name changes.
     *
     * @return the current brand name packet
     */
    public static PluginMessagePacket brandPacket(String brandName) {
        final byte[] data = NetworkBuffer.makeArray(STRING, brandName);
        return new PluginMessagePacket("minecraft:brand", data);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PluginMessagePacket(String channel1, byte[] data1))) return false;
        return Arrays.equals(data(), data1) && Objects.equals(channel(), channel1);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(channel());
        result = 31 * result + Arrays.hashCode(data());
        return result;
    }
}
