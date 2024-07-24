package net.minestom.server.network.packet.client.common;

import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientSettingsPacket(@NotNull String locale, byte viewDistance,
                                   @NotNull ChatMessageType chatMessageType, boolean chatColors,
                                   byte displayedSkinParts, @NotNull Player.MainHand mainHand,
                                   boolean enableTextFiltering, boolean allowsListing) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSettingsPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientSettingsPacket::locale,
            BYTE, ClientSettingsPacket::viewDistance,
            Enum(ChatMessageType.class), ClientSettingsPacket::chatMessageType,
            BOOLEAN, ClientSettingsPacket::chatColors,
            BYTE, ClientSettingsPacket::displayedSkinParts,
            Enum(Player.MainHand.class), ClientSettingsPacket::mainHand,
            BOOLEAN, ClientSettingsPacket::enableTextFiltering,
            BOOLEAN, ClientSettingsPacket::allowsListing,
            ClientSettingsPacket::new);

    public ClientSettingsPacket {
        if (locale.length() > 128)
            throw new IllegalArgumentException("Locale cannot be longer than 128 characters.");
    }
}
