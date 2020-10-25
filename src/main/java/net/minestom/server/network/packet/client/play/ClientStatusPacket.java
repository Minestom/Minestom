package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.action = Action.values()[reader.readVarInt()];
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }

}
