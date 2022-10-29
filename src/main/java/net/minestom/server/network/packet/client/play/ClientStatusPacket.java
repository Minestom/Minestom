package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientStatusPacket(@NotNull Action action) implements ClientPacket {
    public ClientStatusPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Action.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Action.class, action);
    }

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }
}
