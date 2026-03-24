package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

/**
 * Responsible for parsing client and server packets.
 * <p>
 * You can retrieve the different packets per state (status/login/play)
 * from the {@link PacketRegistry} classes.
 */
public sealed interface PacketParser<T> {

    PacketRegistry<? extends T> handshake();

    PacketRegistry<? extends T> status();

    PacketRegistry<? extends T> login();

    PacketRegistry<? extends T> configuration();

    PacketRegistry<? extends T> play();

    default T parse(ConnectionState connectionState,
                             int packetId, NetworkBuffer buffer) {
        final PacketRegistry<? extends T> registry = stateRegistry(connectionState);
        return registry.create(packetId, buffer);
    }

    default PacketRegistry<? extends T> stateRegistry(ConnectionState connectionState) {
        return switch (connectionState) {
            case HANDSHAKE -> handshake();
            case STATUS -> status();
            case LOGIN -> login();
            case CONFIGURATION -> configuration();
            case PLAY -> play();
        };
    }

    record Client(
            PacketRegistry<ClientPacket.Handshake> handshake,
            PacketRegistry<ClientPacket.Status> status,
            PacketRegistry<ClientPacket.Login> login,
            PacketRegistry<ClientPacket.Configuration> configuration,
            PacketRegistry<ClientPacket.Play> play
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
            PacketRegistry<ServerPacket.Handshake> handshake,
            PacketRegistry<ServerPacket.Status> status,
            PacketRegistry<ServerPacket.Login> login,
            PacketRegistry<ServerPacket.Configuration> configuration,
            PacketRegistry<ServerPacket.Play> play
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
