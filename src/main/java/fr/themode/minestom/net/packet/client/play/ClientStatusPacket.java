package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(value -> {
            action = Action.values()[value];
            callback.run();
        });
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;
    }

}
