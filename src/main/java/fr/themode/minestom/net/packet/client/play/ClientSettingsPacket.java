package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale;
    public byte viewDistance;
    public Player.ChatMode chatMode;
    public boolean chatColors;
    public byte displayedSkinParts;
    public Player.MainHand mainHand;

    @Override
    public void read(Buffer buffer) {
        this.locale = Utils.readString(buffer);
        this.viewDistance = buffer.getByte();
        this.chatMode = Player.ChatMode.values()[Utils.readVarInt(buffer)];
        this.chatColors = buffer.getBoolean();
        this.displayedSkinParts = buffer.getByte();
        this.mainHand = Player.MainHand.values()[Utils.readVarInt(buffer)];
    }
}
