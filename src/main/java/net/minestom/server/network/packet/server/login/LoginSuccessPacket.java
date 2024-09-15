package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record LoginSuccessPacket(@NotNull GameProfile gameProfile,
                                 boolean strictErrorHandling) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<LoginSuccessPacket> SERIALIZER = NetworkBufferTemplate.template(
            GameProfile.SERIALIZER, LoginSuccessPacket::gameProfile,
            BOOLEAN, LoginSuccessPacket::strictErrorHandling,
            LoginSuccessPacket::new);
}
