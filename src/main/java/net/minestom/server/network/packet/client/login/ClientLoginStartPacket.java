package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.UUID;

public record ClientLoginStartPacket(@NotNull String username,
                                     @NotNull UUID profileId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientLoginStartPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientLoginStartPacket::username,
            UUID, ClientLoginStartPacket::profileId,
            ClientLoginStartPacket::new);

    public ClientLoginStartPacket {
        if (username.length() > 16)
            throw new IllegalArgumentException("Username is not allowed to be longer than 16 characters");
    }

    @Override
    public boolean processImmediately() {
        return true;
    }
}
