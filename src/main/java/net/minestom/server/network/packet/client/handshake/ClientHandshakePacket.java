package net.minestom.server.network.packet.client.handshake;

import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientHandshakePacket(int protocolVersion, @NotNull String serverAddress,
                                    int serverPort, int intent) implements ClientPacket {

    public ClientHandshakePacket {
        if (serverAddress.length() > getMaxHandshakeLength()) {
            throw new IllegalArgumentException("Server address too long: " + serverAddress.length());
        }
    }

    public ClientHandshakePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(STRING),
                reader.read(UNSIGNED_SHORT), reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, protocolVersion);
        int maxLength = getMaxHandshakeLength();
        if (serverAddress.length() > maxLength) {
            throw new IllegalArgumentException("serverAddress is " + serverAddress.length() + " characters long, maximum allowed is " + maxLength);
        }
        writer.write(STRING, serverAddress);
        writer.write(UNSIGNED_SHORT, serverPort);
        writer.write(VAR_INT, intent);
    }

    private static int getMaxHandshakeLength() {
        // BungeeGuard limits handshake length to 2500 characters, while vanilla limits it to 255
        return BungeeCordProxy.isEnabled() ? (BungeeCordProxy.isBungeeGuardEnabled() ? 2500 : Short.MAX_VALUE) : 255;
    }

}
