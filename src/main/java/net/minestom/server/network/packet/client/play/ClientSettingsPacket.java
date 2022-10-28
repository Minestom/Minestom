package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
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
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(locale);
        writer.writeByte(viewDistance);
        writer.writeVarInt(chatMessageType.getPacketID());
        writer.writeBoolean(chatColors);
        writer.writeByte(displayedSkinParts);
        writer.writeVarInt(mainHand.ordinal());
        writer.writeBoolean(enableTextFiltering);
        writer.writeBoolean(allowsListing);
    }
}
