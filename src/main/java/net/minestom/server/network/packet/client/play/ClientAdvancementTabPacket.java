package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

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
