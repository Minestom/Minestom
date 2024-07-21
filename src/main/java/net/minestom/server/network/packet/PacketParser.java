package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for parsing client & server packets.
 * <p>
 * You can retrieve the different packets per state (status/login/play)
 * from the {@link PacketRegistry} classes.
 */
public sealed interface PacketParser<T> {

    @NotNull T parse(@NotNull ConnectionState connectionState,
                     int packetId, @NotNull NetworkBuffer buffer);

    record Client(
            PacketRegistry.Client statusHandler,
            PacketRegistry.Client loginHandler,
            PacketRegistry.Client configurationHandler,
            PacketRegistry.Client playHandler
    ) implements PacketParser<ClientPacket> {

        public Client() {
            this(new PacketRegistry.ClientStatus(), new PacketRegistry.ClientLogin(),
                    new PacketRegistry.ClientConfiguration(), new PacketRegistry.ClientPlay()
            );
        }

        @Override
        public @NotNull ClientPacket parse(@NotNull ConnectionState connectionState,
                                           int packetId, @NotNull NetworkBuffer buffer) {
            return switch (connectionState) {
                case HANDSHAKE -> {
                    assert packetId == 0;
                    yield new ClientHandshakePacket(buffer);
                }
                case STATUS -> statusHandler.create(packetId, buffer);
                case LOGIN -> loginHandler.create(packetId, buffer);
                case CONFIGURATION -> configurationHandler.create(packetId, buffer);
                case PLAY -> playHandler.create(packetId, buffer);
            };
        }
    }

    record Server(
            PacketRegistry.Server statusHandler,
            PacketRegistry.Server loginHandler,
            PacketRegistry.Server configurationHandler,
            PacketRegistry.Server playHandler
    ) implements PacketParser<ServerPacket> {

        public Server() {
            this(new PacketRegistry.ServerStatus(), new PacketRegistry.ServerLogin(),
                    new PacketRegistry.ServerConfiguration(), new PacketRegistry.ServerPlay()
            );
        }

        @Override
        public @NotNull ServerPacket parse(@NotNull ConnectionState connectionState,
                                           int packetId, @NotNull NetworkBuffer buffer) {
            return switch (connectionState) {
                case HANDSHAKE -> throw new UnsupportedOperationException("No client-bound Handshake packet");
                case STATUS -> statusHandler.create(packetId, buffer);
                case LOGIN -> loginHandler.create(packetId, buffer);
                case CONFIGURATION -> configurationHandler.create(packetId, buffer);
                case PLAY -> playHandler.create(packetId, buffer);
            };
        }
    }
}
