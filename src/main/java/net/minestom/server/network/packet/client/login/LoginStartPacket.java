package net.minestom.server.network.packet.client.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.EncryptionRequestPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class LoginStartPacket implements ClientPreplayPacket {

    private static final ColoredText ALREADY_CONNECTED_JSON = ColoredText.of(ChatColor.RED, "You are already on this server");

    public String username;

    @Override
    public void process(@NotNull PlayerConnection connection) {

        // Cache the login username and start compression if enabled
        if (connection instanceof NettyPlayerConnection) {
            NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;
            nettyPlayerConnection.UNSAFE_setLoginUsername(username);

            // Compression
            final int threshold = MinecraftServer.getCompressionThreshold();
            if (threshold > 0) {
                nettyPlayerConnection.enableCompression(threshold);
            }
        }

        // Proxy support (only for netty clients)
        if (connection instanceof NettyPlayerConnection) {
            final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;

            {
                // Velocity support
                if (VelocityProxy.isEnabled()) {

                    final int messageId = ThreadLocalRandom.current().nextInt();
                    final String channel = VelocityProxy.PLAYER_INFO_CHANNEL;

                    // Important in order to retrieve the channel in the response packet
                    nettyPlayerConnection.addPluginRequestEntry(messageId, channel);

                    LoginPluginRequestPacket loginPluginRequestPacket = new LoginPluginRequestPacket();
                    loginPluginRequestPacket.messageId = messageId;
                    loginPluginRequestPacket.channel = channel;
                    loginPluginRequestPacket.data = null;
                    connection.sendPacket(loginPluginRequestPacket);

                    return;
                }
            }

        }

        if (MojangAuth.isUsingMojangAuth() && connection instanceof NettyPlayerConnection) {
            // Mojang auth
            if (CONNECTION_MANAGER.getPlayer(username) != null) {
                connection.sendPacket(new LoginDisconnectPacket(ALREADY_CONNECTED_JSON));
                connection.disconnect();
                return;
            }

            final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;

            nettyPlayerConnection.setConnectionState(ConnectionState.LOGIN);
            EncryptionRequestPacket encryptionRequestPacket = new EncryptionRequestPacket(nettyPlayerConnection);
            nettyPlayerConnection.sendPacket(encryptionRequestPacket);
        } else {
            // Offline
            final UUID playerUuid = CONNECTION_MANAGER.getPlayerConnectionUuid(connection, username);

            CONNECTION_MANAGER.startPlayState(connection, playerUuid, username);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.username = reader.readSizedString();
    }

}
