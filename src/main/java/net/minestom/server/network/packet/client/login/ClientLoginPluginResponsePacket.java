package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientLoginPluginResponsePacket(int messageId, byte @Nullable [] data) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientLoginPluginResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientLoginPluginResponsePacket::messageId,
            RAW_BYTES, ClientLoginPluginResponsePacket::data,
            ClientLoginPluginResponsePacket::new);

    @Override
    public boolean processImmediately() {
        return true;
    }
}
