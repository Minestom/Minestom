package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale;
    public byte viewDistance;
    public Player.ChatMode chatMode;
    public boolean chatColors;
    public byte displayedSkinParts;
    public Player.MainHand mainHand;

    @Override
    public void read(PacketReader reader) {
        this.locale = reader.readSizedString();
        this.viewDistance = reader.readByte();
        this.chatMode = Player.ChatMode.values()[reader.readVarInt()];
        this.chatColors = reader.readBoolean();
        this.displayedSkinParts = reader.readByte();
        this.mainHand = Player.MainHand.values()[reader.readVarInt()];
    }
}
