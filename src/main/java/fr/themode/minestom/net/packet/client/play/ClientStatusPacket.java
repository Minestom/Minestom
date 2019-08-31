package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    @Override
    public void read(PacketReader reader) {
        this.action = Action.values()[reader.readVarInt()];
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;
    }

}
