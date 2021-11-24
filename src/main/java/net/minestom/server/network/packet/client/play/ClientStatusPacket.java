package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientStatusPacket(@NotNull Action action) implements ClientPacket {
    public ClientStatusPacket(BinaryReader reader) {
        this(Action.values()[reader.readVarInt()]);
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
