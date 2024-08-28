package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for parsing client & server packets.
 * <p>
 * You can retrieve the different packets per state (status/login/play)
 * from the {@link PacketRegistry} classes.
 */
public sealed interface PacketParser<T> {

    @NotNull PacketRegistry<T> handshake();

    @NotNull PacketRegistry<T> status();

    @NotNull PacketRegistry<T> login();

    @NotNull PacketRegistry<T> configuration();

    @NotNull PacketRegistry<T> play();

    default @NotNull T parse(@NotNull ConnectionState connectionState,
                             int packetId, @NotNull NetworkBuffer buffer) {
        final PacketRegistry<T> registry = stateRegistry(connectionState);
        return registry.create(packetId, buffer);
    }

    default @NotNull PacketRegistry<T> stateRegistry(@NotNull ConnectionState connectionState) {
        return switch (connectionState) {
            case HANDSHAKE -> handshake();
            case STATUS -> status();
            case LOGIN -> login();
            case CONFIGURATION -> configuration();
            case PLAY -> play();
        };
    }

    record Client(
            PacketRegistry<ClientPacket> handshake,
            PacketRegistry<ClientPacket> status,
            PacketRegistry<ClientPacket> login,
            PacketRegistry<ClientPacket> configuration,
            PacketRegistry<ClientPacket> play
    ) implements PacketParser<ClientPacket> {
        public Client() {
            this(
                    new PacketRegistry.ClientHandshake(),
                    new PacketRegistry.ClientStatus(),
                    new PacketRegistry.ClientLogin(),
                    new PacketRegistry.ClientConfiguration(),
                    new PacketRegistry.ClientPlay()
            );
        }
    }

    record Server(
            PacketRegistry<ServerPacket> handshake,
            PacketRegistry<ServerPacket> status,
            PacketRegistry<ServerPacket> login,
            PacketRegistry<ServerPacket> configuration,
            PacketRegistry<ServerPacket> play
    ) implements PacketParser<ServerPacket> {
        public Server() {
            this(
                    new PacketRegistry.ServerHandshake(),
                    new PacketRegistry.ServerStatus(),
                    new PacketRegistry.ServerLogin(),
                    new PacketRegistry.ServerConfiguration(),
                    new PacketRegistry.ServerPlay()
            );
        }
    }
}
