package net.minestom.server.network;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPacketsHandler;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Responsible for processing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link ClientPacketsHandler} classes.
 */
public record PacketProcessor(@NotNull ClientPacketsHandler statusHandler,
                              @NotNull ClientPacketsHandler loginHandler,
                              @NotNull ClientPacketsHandler playHandler) {
    public PacketProcessor() {
        this(new ClientPacketsHandler.Status(),
                new ClientPacketsHandler.Login(),
                new ClientPacketsHandler.Play());
    }

    public @NotNull ClientPacket create(@NotNull ConnectionState connectionState, int packetId, ByteBuffer body) {
        NetworkBuffer buffer = new NetworkBuffer(body);
        final ClientPacket clientPacket = switch (connectionState) {
            case PLAY -> playHandler.create(packetId, buffer);
            case LOGIN -> loginHandler.create(packetId, buffer);
            case STATUS -> statusHandler.create(packetId, buffer);
            case UNKNOWN -> {
                assert packetId == 0;
                yield new HandshakePacket(buffer);
            }
        };
        body.position(buffer.readIndex());
        return clientPacket;
    }

    public ClientPacket process(@NotNull PlayerConnection connection, int packetId, ByteBuffer body) {
        final ClientPacket packet = create(connection.getConnectionState(), packetId, body);
        if (packet instanceof ClientPreplayPacket prePlayPacket) {
            prePlayPacket.process(connection);
        } else {
            final Player player = connection.getPlayer();
            assert player != null;
            player.addPacketToQueue(packet);
        }
        return packet;
    }
}
