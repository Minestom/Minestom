package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientChangeGameModePacket(@NotNull GameMode gameMode) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChangeGameModePacket> SERIALIZER = NetworkBufferTemplate.template(
            GameMode.NETWORK_TYPE, ClientChangeGameModePacket::gameMode,
            ClientChangeGameModePacket::new);
}
