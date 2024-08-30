package net.minestom.server.network.packet.client.common;

import net.minestom.server.entity.PlayerSettings;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientSettingsPacket(@NotNull PlayerSettings settings) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSettingsPacket> SERIALIZER = NetworkBufferTemplate.template(
            PlayerSettings.NETWORK_TYPE, ClientSettingsPacket::settings,
            ClientSettingsPacket::new);
}
