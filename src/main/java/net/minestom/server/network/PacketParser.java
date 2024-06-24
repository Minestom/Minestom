package net.minestom.server.network;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPacketsHandler;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Responsible for parsing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link ClientPacketsHandler} classes.
 */
public record PacketParser(
        ClientPacketsHandler statusHandler,
        ClientPacketsHandler loginHandler,
        ClientPacketsHandler configurationHandler,
        ClientPacketsHandler playHandler
) {

    public PacketParser() {
        this(new ClientPacketsHandler.Status(), new ClientPacketsHandler.Login(),
                new ClientPacketsHandler.Configuration(), new ClientPacketsHandler.Play());
    }

    public @NotNull ClientPacket parse(@NotNull ConnectionState connectionState,
                                       int packetId, @NotNull ByteBuffer body) {
        NetworkBuffer buffer = new NetworkBuffer(body);
        final ClientPacket clientPacket = switch (connectionState) {
            case HANDSHAKE -> {
                assert packetId == 0;
                yield new ClientHandshakePacket(buffer);
            }
            case STATUS -> statusHandler.create(packetId, buffer);
            case LOGIN -> loginHandler.create(packetId, buffer);
            case CONFIGURATION -> configurationHandler.create(packetId, buffer);
            case PLAY -> playHandler.create(packetId, buffer);
        };
        body.position(buffer.readIndex());
        return clientPacket;
    }
}
