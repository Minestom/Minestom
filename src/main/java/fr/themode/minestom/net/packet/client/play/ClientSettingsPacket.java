package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientSettingsPacket extends ClientPlayPacket {

    public String locale;
    public byte viewDistance;
    // TODO chat mode
    public boolean chatColors;
    public byte displayedSkinParts;
    // TODO main hand

    @Override
    public void read(Buffer buffer) {
        this.locale = Utils.readString(buffer);
        this.viewDistance = buffer.getByte();
        Utils.readVarInt(buffer); // chat mode
        this.chatColors = buffer.getBoolean();
        this.displayedSkinParts = buffer.getByte();
        Utils.readVarInt(buffer); // main hand
    }
}
