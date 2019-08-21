package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    @Override
    public void read(Buffer buffer) {
        this.action = Action.values()[Utils.readVarInt(buffer)];
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;
    }

}
