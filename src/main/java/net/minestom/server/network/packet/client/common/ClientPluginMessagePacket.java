package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientPluginMessagePacket(@NotNull String channel, byte[] data) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPluginMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientPluginMessagePacket::channel,
            RAW_BYTES, ClientPluginMessagePacket::data,
            ClientPluginMessagePacket::new);

    public ClientPluginMessagePacket {
        if (channel.length() > 256)
            throw new IllegalArgumentException("Channel cannot be more than 256 characters long");
    }
}
