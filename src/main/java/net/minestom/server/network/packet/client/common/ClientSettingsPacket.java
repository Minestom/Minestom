package net.minestom.server.network.packet.client.common;

import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientSettingsPacket(@NotNull String locale, byte viewDistance,
                                   @NotNull ChatMessageType chatMessageType, boolean chatColors,
                                   byte displayedSkinParts, @NotNull Player.MainHand mainHand,
                                   boolean enableTextFiltering, boolean allowsListing) implements ClientPacket {
    public ClientSettingsPacket {
        if (locale.length() > 128)
            throw new IllegalArgumentException("Locale cannot be longer than 128 characters.");
    }

    public ClientSettingsPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(BYTE),
                ChatMessageType.fromPacketID(reader.read(VAR_INT)), reader.read(BOOLEAN),
                reader.read(BYTE), reader.readEnum(Player.MainHand.class),
                reader.read(BOOLEAN), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, locale);
        writer.write(BYTE, viewDistance);
        writer.write(VAR_INT, chatMessageType.getPacketID());
        writer.write(BOOLEAN, chatColors);
        writer.write(BYTE, displayedSkinParts);
        writer.write(VAR_INT, mainHand.ordinal());
        writer.write(BOOLEAN, enableTextFiltering);
        writer.write(BOOLEAN, allowsListing);
    }
}
