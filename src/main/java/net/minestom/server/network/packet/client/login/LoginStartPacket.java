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
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public record LoginStartPacket(@NotNull String username) implements ClientPreplayPacket {
    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);

    public LoginStartPacket(BinaryReader reader) {
        this(reader.readSizedString(16));
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        final boolean isSocketConnection = connection instanceof PlayerSocketConnection;
        // Cache the login username and start compression if enabled
        if (isSocketConnection) {
            PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.UNSAFE_setLoginUsername(username);

            // Compression
            final int threshold = MinecraftServer.getCompressionThreshold();
            if (threshold > 0) {
                socketConnection.startCompression();
            }
        }
        // Proxy support (only for socket clients)
        if (isSocketConnection) {
            final PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            // Velocity support
            if (VelocityProxy.isEnabled()) {
                final int messageId = ThreadLocalRandom.current().nextInt();
                final String channel = VelocityProxy.PLAYER_INFO_CHANNEL;
                // Important in order to retrieve the channel in the response packet
                socketConnection.addPluginRequestEntry(messageId, channel);
                connection.sendPacket(new LoginPluginRequestPacket(messageId, channel, null));
                return;
            }
        }

        if (MojangAuth.isEnabled() && isSocketConnection) {
            // Mojang auth
            if (CONNECTION_MANAGER.getPlayer(username) != null) {
                connection.sendPacket(new LoginDisconnectPacket(ALREADY_CONNECTED));
                connection.disconnect();
                return;
            }
            final PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.setConnectionState(ConnectionState.LOGIN);

            final byte[] publicKey = MojangAuth.getKeyPair().getPublic().getEncoded();
            byte[] nonce = new byte[4];
            ThreadLocalRandom.current().nextBytes(nonce);
            socketConnection.setNonce(nonce);
            socketConnection.sendPacket(new EncryptionRequestPacket("", publicKey, nonce));
        } else {
            final boolean bungee = BungeeCordProxy.isEnabled();
            // Offline
            final UUID playerUuid = bungee && isSocketConnection ?
                    ((PlayerSocketConnection) connection).getBungeeUuid() :
                    CONNECTION_MANAGER.getPlayerConnectionUuid(connection, username);

            Player player = CONNECTION_MANAGER.startPlayState(connection, playerUuid, username, true);
            if (bungee && isSocketConnection) {
                player.setSkin(((PlayerSocketConnection) connection).getBungeeSkin());
            }
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if (username.length() > 16)
            throw new IllegalArgumentException("Username is not allowed to be longer than 16 characters");
        writer.writeSizedString(username);
    }
}
