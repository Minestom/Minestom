package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.UUID;

public record ClientLoginStartPacket(@NotNull String username,
                                     @NotNull UUID profileId) implements ClientPacket {

    public ClientLoginStartPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(UUID));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        if (username.length() > 16)
            throw new IllegalArgumentException("Username is not allowed to be longer than 16 characters");
        writer.write(STRING, username);
        writer.write(UUID, profileId);
    }
}
