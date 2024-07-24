package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record LoginSuccessPacket(@NotNull UUID uuid, @NotNull String username,
                                 int properties, boolean strictErrorHandling) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<LoginSuccessPacket> SERIALIZER = NetworkBufferTemplate.template(
            UUID, LoginSuccessPacket::uuid,
            STRING, LoginSuccessPacket::username,
            VAR_INT, LoginSuccessPacket::properties,
            BOOLEAN, LoginSuccessPacket::strictErrorHandling,
            LoginSuccessPacket::new);
}
