package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.message.ChatMessageType;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSettingsPacket(@NotNull String locale, byte viewDistance,
                                   @NotNull ChatMessageType chatMessageType, boolean chatColors,
                                   byte displayedSkinParts, @NotNull Player.MainHand mainHand,
                                   boolean enableTextFiltering, boolean allowsListing) implements ClientPacket {
    public ClientSettingsPacket {
        if (locale.length() > 128)
            throw new IllegalArgumentException("Locale cannot be longer than 128 characters.");
    }

    public ClientSettingsPacket(BinaryReader reader) {
        this(reader.readSizedString(128), reader.readByte(),
                ChatMessageType.fromPacketID(reader.readVarInt()), reader.readBoolean(),
                reader.readByte(), Player.MainHand.values()[reader.readVarInt()],
                reader.readBoolean(), reader.readBoolean());
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
