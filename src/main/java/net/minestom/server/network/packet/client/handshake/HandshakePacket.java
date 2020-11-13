package net.minestom.server.network.packet.client.handshake;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class HandshakePacket implements ClientPreplayPacket {

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final ColoredText INVALID_VERSION_TEXT = ColoredText.of(ChatColor.RED, "Invalid Version, please use " + MinecraftServer.VERSION_NAME);

    private static final ColoredText INVALID_BUNGEE_FORWARDING = ColoredText.of(ChatColor.RED, "If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.protocolVersion = reader.readVarInt();
        this.serverAddress = reader.readSizedString(256);
        this.serverPort = reader.readUnsignedShort();
        this.nextState = reader.readVarInt();
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {

        if (BungeeCordProxy.isEnabled() && connection instanceof NettyPlayerConnection) {
            NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;

            if (serverAddress != null) {
                final String[] split = serverAddress.split("\00");

                if (split.length == 3 || split.length == 4) {
                    this.serverAddress = split[0];

                    final SocketAddress socketAddress = new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort());
                    nettyPlayerConnection.setRemoteAddress(socketAddress);
                } else {
                    nettyPlayerConnection.sendPacket(new LoginDisconnectPacket(INVALID_BUNGEE_FORWARDING));
                    nettyPlayerConnection.disconnect();
                    return;
                }
            } else {
                // Happen when a client ping the server, ignore
                return;
            }
        }

        switch (nextState) {
            case 1:
                connection.setConnectionState(ConnectionState.STATUS);
                break;
            case 2:
                if (protocolVersion == MinecraftServer.PROTOCOL_VERSION) {
                    connection.setConnectionState(ConnectionState.LOGIN);

                    if (connection instanceof NettyPlayerConnection) {
                        // Give to the connection the server info that the client used
                        ((NettyPlayerConnection) connection).refreshServerInformation(serverAddress, serverPort);
                    }
                } else {
                    // Incorrect client version
                    connection.sendPacket(new LoginDisconnectPacket(INVALID_VERSION_TEXT.toString()));
                    connection.disconnect();
                }
                break;
            default:
                // Unexpected error
                break;
        }
    }
}
