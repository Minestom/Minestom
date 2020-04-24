package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    @Override
    public void read(PacketReader reader) {
        this.action = Action.values()[reader.readVarInt()];
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }

}
