package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientLoginPluginResponsePacket(int messageId, byte @Nullable [] data) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientLoginPluginResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientLoginPluginResponsePacket::messageId,
            RAW_BYTES.optional(), ClientLoginPluginResponsePacket::data,
            ClientLoginPluginResponsePacket::new);

    public ClientLoginPluginResponsePacket {
        data = data != null ? data.clone() : null;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ClientLoginPluginResponsePacket(int id, byte[] data1))) return false;
        return messageId() == id && Arrays.equals(data(), data1);
    }

    @Override
    public int hashCode() {
        int result = messageId();
        result = 31 * result + Arrays.hashCode(data());
        return result;
    }
}
