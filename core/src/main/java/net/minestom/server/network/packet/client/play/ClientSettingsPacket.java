package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale = "";
    public byte viewDistance;
    public Player.ChatMode chatMode = Player.ChatMode.ENABLED;
    public boolean chatColors;
    public byte displayedSkinParts;
    public Player.MainHand mainHand = Player.MainHand.RIGHT;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.locale = reader.readSizedString(128);
        this.viewDistance = reader.readByte();
        this.chatMode = Player.ChatMode.values()[reader.readVarInt()];
        this.chatColors = reader.readBoolean();
        this.displayedSkinParts = reader.readByte();
        this.mainHand = Player.MainHand.values()[reader.readVarInt()];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if(locale.length() > 128)
            throw new IllegalArgumentException("Locale cannot be longer than 128 characters.");
        writer.writeSizedString(locale);
        writer.writeByte(viewDistance);
        writer.writeVarInt(chatMode.ordinal());
        writer.writeBoolean(chatColors);
        writer.writeByte(displayedSkinParts);
        writer.writeVarInt(mainHand.ordinal());
    }
}
