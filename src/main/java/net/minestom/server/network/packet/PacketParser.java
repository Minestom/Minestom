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

    @NotNull PacketRegistry<T> handshakeRegistry();

    @NotNull PacketRegistry<T> statusRegistry();

    @NotNull PacketRegistry<T> loginRegistry();

    @NotNull PacketRegistry<T> configurationRegistry();

    @NotNull PacketRegistry<T> playRegistry();

    default @NotNull T parse(@NotNull ConnectionState connectionState,
                             int packetId, @NotNull NetworkBuffer buffer) {
        final PacketRegistry<T> registry = stateRegistry(connectionState);
        return registry.create(packetId, buffer);
    }

    default @NotNull PacketRegistry<T> stateRegistry(@NotNull ConnectionState connectionState) {
        return switch (connectionState) {
            case HANDSHAKE -> handshakeRegistry();
            case STATUS -> statusRegistry();
            case LOGIN -> loginRegistry();
            case CONFIGURATION -> configurationRegistry();
            case PLAY -> playRegistry();
        };
    }

    record Client(
            PacketRegistry<ClientPacket> handshakeRegistry,
            PacketRegistry<ClientPacket> statusRegistry,
            PacketRegistry<ClientPacket> loginRegistry,
            PacketRegistry<ClientPacket> configurationRegistry,
            PacketRegistry<ClientPacket> playRegistry
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
            PacketRegistry<ServerPacket> handshakeRegistry,
            PacketRegistry<ServerPacket> statusRegistry,
            PacketRegistry<ServerPacket> loginRegistry,
            PacketRegistry<ServerPacket> configurationRegistry,
            PacketRegistry<ServerPacket> playRegistry
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
