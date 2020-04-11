package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientAdvancementTabPacket extends ClientPlayPacket {

    public Action action;
    public String tabIdentifier;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(i -> {
            action = Action.values()[i];
            if (action == Action.OPENED_TAB) {
                reader.readSizedString((string, length) -> {
                    tabIdentifier = string;
                    callback.run();
                });
            } else {
                callback.run();
            }
        });
    }

    public enum Action {
        OPENED_TAB,
        CLOSED_SCREEN
    }

}
