package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientStatusPacket extends ClientPlayPacket {

    public Action action;

    public ClientStatusPacket() {
        action = Action.REQUEST_STATS;
    }

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.action = Action.values()[reader.readVarInt()];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }

}
