package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientPluginMessagePacket(String channel, byte[] data) implements ClientPacket.Configuration, ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientPluginMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientPluginMessagePacket::channel,
            RAW_BYTES, ClientPluginMessagePacket::data,
            ClientPluginMessagePacket::new);

    public ClientPluginMessagePacket {
        if (channel.length() > 256)
            throw new IllegalArgumentException("Channel cannot be more than 256 characters long");
        data = data.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientPluginMessagePacket(String channel1, byte[] data1))) return false;
        return Arrays.equals(data(), data1) && channel().equals(channel1);
    }

    @Override
    public int hashCode() {
        int result = channel().hashCode();
        result = 31 * result + Arrays.hashCode(data());
        return result;
    }
}
