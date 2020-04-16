package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientAdvancementTabPacket extends ClientPlayPacket {

    public Action action;
    public String tabIdentifier;

    @Override
    public void read(PacketReader reader) {
        this.action = Action.values()[reader.readVarInt()];

        if (action == Action.OPENED_TAB) {
            this.tabIdentifier = reader.readSizedString();
        }
    }

    public enum Action {
        OPENED_TAB,
        CLOSED_SCREEN
    }

}
