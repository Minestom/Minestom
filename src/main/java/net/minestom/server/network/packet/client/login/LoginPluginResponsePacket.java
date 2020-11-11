package net.minestom.server.network.packet.client.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class LoginPluginResponsePacket implements ClientPreplayPacket {

    private final static ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static final ColoredText INVALID_PROXY_RESPONSE = ColoredText.of(ChatColor.RED, "Invalid proxy response!");

    public int messageId;
    public boolean successful;
    public byte[] data;

    @Override
    public void process(@NotNull PlayerConnection connection) {

        // Proxy support
        if (connection instanceof NettyPlayerConnection) {
            final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;
            final String channel = nettyPlayerConnection.getPluginRequestChannel(messageId);

            if (channel != null) {
                boolean success = false;
                SocketAddress socketAddress = null;

                // Velocity
                if (VelocityProxy.isEnabled() && channel.equals(VelocityProxy.PLAYER_INFO_CHANNEL)) {
                    if (data != null) {
                        BinaryReader reader = new BinaryReader(data);
                        success = VelocityProxy.checkIntegrity(reader);
                        if (success) {
                            // Get the real connection address
                            final InetAddress address = VelocityProxy.readAddress(reader);
                            final int port = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort();
                            socketAddress = new InetSocketAddress(address, port);
                        }
                    }
                }

                if (success) {
                    if (socketAddress != null) {
                        nettyPlayerConnection.setRemoteAddress(socketAddress);
                    }

                    // Proxy usage always mean that the server is in offline mode
                    final String username = nettyPlayerConnection.getLoginUsername();
                    final UUID playerUuid = CONNECTION_MANAGER.getPlayerConnectionUuid(connection, username);

                    CONNECTION_MANAGER.startPlayState(connection, playerUuid, username);
                } else {
                    LoginDisconnectPacket disconnectPacket = new LoginDisconnectPacket(INVALID_PROXY_RESPONSE);
                    nettyPlayerConnection.sendPacket(disconnectPacket);
                }

            }
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.messageId = reader.readVarInt();
        this.successful = reader.readBoolean();
        if (successful) {
            this.data = reader.getRemainingBytes();
        }
    }
}
