package net.minestom.server.network.packet;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.play.ClientConfigurationAckPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.StartConfigurationPacket;
import net.minestom.server.utils.ObjectPool;
import org.jetbrains.annotations.ApiStatus;

/**
 * Constants and utilities for vanilla packets.
 */
@ApiStatus.Internal
public final class PacketVanilla {
    public static final PacketParser<ClientPacket> CLIENT_PACKET_PARSER = new PacketParser.Client();
    public static final PacketParser<ServerPacket> SERVER_PACKET_PARSER = new PacketParser.Server();

    /**
     * Pool containing a buffer able to hold the largest packet.
     * <p>
     * Size starts with {@link ServerFlag#POOLED_BUFFER_SIZE} and doubles until {@link ServerFlag#MAX_PACKET_SIZE}.
     */
    public static final ObjectPool<NetworkBuffer> PACKET_POOL = ObjectPool.pool(
            () -> NetworkBuffer.staticBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process()),
            NetworkBuffer::clear);

    public static ConnectionState nextClientState(ClientPacket packet, ConnectionState currentState) {
        return switch (packet) {
            case ClientHandshakePacket handshakePacket -> switch (handshakePacket.intent()) {
                case STATUS -> ConnectionState.STATUS;
                case LOGIN, TRANSFER -> ConnectionState.LOGIN;
            };
            case ClientLoginAcknowledgedPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientConfigurationAckPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientFinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }

    public static ConnectionState nextServerState(ServerPacket packet, ConnectionState currentState) {
        // Client chooses between STATUS or LOGIN state directly after the first handshake packet
        if (currentState == ConnectionState.HANDSHAKE)
            throw new IllegalStateException("No server Handshake packet exists");
        return switch (packet) {
            case LoginSuccessPacket ignored -> ConnectionState.CONFIGURATION;
            case StartConfigurationPacket ignored -> ConnectionState.CONFIGURATION;
            case FinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }
}
