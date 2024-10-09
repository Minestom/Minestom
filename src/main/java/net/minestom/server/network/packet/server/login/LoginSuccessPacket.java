package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.NotNull;

public record LoginSuccessPacket(@NotNull GameProfile gameProfile) implements ServerPacket.Login {
    public static final NetworkBuffer.Type<LoginSuccessPacket> SERIALIZER = NetworkBufferTemplate.template(
            GameProfile.SERIALIZER, LoginSuccessPacket::gameProfile,
            LoginSuccessPacket::new);
}
