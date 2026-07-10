package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;

import java.util.UUID;

public record LoginSuccessPacket(GameProfile gameProfile, UUID sessionId) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<LoginSuccessPacket> SERIALIZER = NetworkBufferTemplate.template(
            GameProfile.SERIALIZER, LoginSuccessPacket::gameProfile,
            NetworkBuffer.UUID, LoginSuccessPacket::sessionId,
            LoginSuccessPacket::new);
}
