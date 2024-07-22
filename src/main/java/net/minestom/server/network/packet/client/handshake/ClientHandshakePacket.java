package net.minestom.server.network.packet.client.handshake;

import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientHandshakePacket(int protocolVersion, @NotNull String serverAddress,
                                    int serverPort, @NotNull Intent intent) implements ClientPacket {

    public ClientHandshakePacket {
        if (serverAddress.length() > getMaxHandshakeLength()) {
            throw new IllegalArgumentException("Server address too long: " + serverAddress.length());
        }
    }

    public static NetworkBuffer.Type<ClientHandshakePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, ClientHandshakePacket value) {
            writer.write(VAR_INT, value.protocolVersion);
            int maxLength = getMaxHandshakeLength();
            if (value.serverAddress.length() > maxLength) {
                throw new IllegalArgumentException("serverAddress is " + value.serverAddress.length() + " characters long, maximum allowed is " + maxLength);
            }
            writer.write(STRING, value.serverAddress);
            writer.write(UNSIGNED_SHORT, value.serverPort);
            // Not a writeEnum call because the indices are not 0-based
            writer.write(VAR_INT, value.intent.id());
        }

        @Override
        public @NotNull ClientHandshakePacket read(@NotNull NetworkBuffer reader) {
            return new ClientHandshakePacket(reader.read(VAR_INT), reader.read(STRING),
                    reader.read(UNSIGNED_SHORT),
                    // Not a readEnum call because the indices are not 0-based
                    Intent.fromId(reader.read(VAR_INT)));
        }
    };

    @Override
    public boolean processImmediately() {
        return true;
    }

    private static int getMaxHandshakeLength() {
        // BungeeGuard limits handshake length to 2500 characters, while vanilla limits it to 255
        return BungeeCordProxy.isEnabled() ? (BungeeCordProxy.isBungeeGuardEnabled() ? 2500 : Short.MAX_VALUE) : 255;
    }

    public enum Intent {
        STATUS,
        LOGIN,
        TRANSFER;

        public static @NotNull Intent fromId(int id) {
            return switch (id) {
                case 1 -> STATUS;
                case 2 -> LOGIN;
                case 3 -> TRANSFER;
                default -> throw new IllegalArgumentException("Unknown connection intent: " + id);
            };
        }

        public int id() {
            return ordinal() + 1;
        }
    }
}
