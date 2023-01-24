package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateCommandBlockMinecartPacket(int entityId, @NotNull String command,
                                                     boolean trackOutput) implements ClientPacket {
    public ClientUpdateCommandBlockMinecartPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(STRING), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(STRING, command);
        writer.write(BOOLEAN, trackOutput);
    }
}
