package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.player.ClientSettings;
import org.jetbrains.annotations.NotNull;

public record ClientSettingsPacket(@NotNull ClientSettings settings) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSettingsPacket> SERIALIZER = NetworkBufferTemplate.template(
            ClientSettings.NETWORK_TYPE, ClientSettingsPacket::settings,
            ClientSettingsPacket::new);
}
