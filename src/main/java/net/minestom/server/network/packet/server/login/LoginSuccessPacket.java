package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record LoginSuccessPacket(@NotNull UUID uuid, @NotNull String username,
                                 int properties, boolean strictErrorHandling) implements ServerPacket.Login {
    public LoginSuccessPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), reader.read(STRING), reader.read(VAR_INT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, uuid);
        writer.write(STRING, username);
        writer.write(VAR_INT, properties);
        writer.write(BOOLEAN, strictErrorHandling);
    }

}
