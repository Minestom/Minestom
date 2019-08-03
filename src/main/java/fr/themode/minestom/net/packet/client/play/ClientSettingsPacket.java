package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientSettingsPacket implements ClientPlayPacket {

    private String locale;
    private byte viewDistance;
    // TODO chat mode
    private boolean chatColors;
    private byte displayedSkinParts;
    // TODO main hand

    @Override
    public void process(Player player) {

    }

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
