package net.minestom.server.network.packet.client.login;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.EncryptionRequestPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class LoginStartPacket implements ClientPreplayPacket {

    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);

    public String username = "";

    @Override
    public void process(@NotNull PlayerConnection connection) {

        final boolean isNettyClient = connection instanceof NettyPlayerConnection;

        // Cache the login username and start compression if enabled
        if (isNettyClient) {
            NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;
            nettyPlayerConnection.UNSAFE_setLoginUsername(username);

            // Compression
            final int threshold = MinecraftServer.getCompressionThreshold();
            if (threshold > 0) {
                nettyPlayerConnection.startCompression();
            }
        }

        // Proxy support (only for netty clients)
        if (isNettyClient) {
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

        if (MojangAuth.isEnabled() && isNettyClient) {
            // Mojang auth
            if (CONNECTION_MANAGER.getPlayer(username) != null) {
                connection.sendPacket(new LoginDisconnectPacket(ALREADY_CONNECTED));
                connection.disconnect();
                return;
            }

            final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) connection;

            nettyPlayerConnection.setConnectionState(ConnectionState.LOGIN);
            EncryptionRequestPacket encryptionRequestPacket = new EncryptionRequestPacket(nettyPlayerConnection);
            nettyPlayerConnection.sendPacket(encryptionRequestPacket);
        } else {
            final boolean bungee = BungeeCordProxy.isEnabled();
            // Offline
            final UUID playerUuid = bungee && isNettyClient ?
                    ((NettyPlayerConnection) connection).getBungeeUuid() :
                    CONNECTION_MANAGER.getPlayerConnectionUuid(connection, username);

            Player player = CONNECTION_MANAGER.startPlayState(connection, playerUuid, username, true);
            if (bungee && isNettyClient) {
                player.setSkin(((NettyPlayerConnection) connection).getBungeeSkin());
            }
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.username = reader.readSizedString(16);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if(username.length() > 16)
            throw new IllegalArgumentException("Username is not allowed to be longer than 16 characters");
        writer.writeSizedString(username);
    }
}
