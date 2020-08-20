package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale;
    public byte viewDistance;
    public Player.ChatMode chatMode;
    public boolean chatColors;
    public byte displayedSkinParts;
    public Player.MainHand mainHand;

    @Override
    public void read(BinaryReader reader) {
        this.locale = reader.readSizedString();
        this.viewDistance = reader.readByte();
        this.chatMode = Player.ChatMode.values()[reader.readVarInt()];
        this.chatColors = reader.readBoolean();
        this.displayedSkinParts = reader.readByte();
        this.mainHand = Player.MainHand.values()[reader.readVarInt()];
    }
}
