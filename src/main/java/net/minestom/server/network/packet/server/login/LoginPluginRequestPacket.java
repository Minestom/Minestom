package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record LoginPluginRequestPacket(int messageId, @NotNull String channel,
                                       byte[] data) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<LoginPluginRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, LoginPluginRequestPacket::messageId,
            STRING, LoginPluginRequestPacket::channel,
            RAW_BYTES, LoginPluginRequestPacket::data,
            LoginPluginRequestPacket::new);
}
