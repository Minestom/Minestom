package net.minestom.server.network.packet.client.login;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.crypto.PlayerPublicKey;
import net.minestom.server.crypto.SignatureValidator;
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
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public record LoginStartPacket(@NotNull String username,
                               @Nullable PlayerPublicKey publicKey,
                               @Nullable UUID profileId) implements ClientPreplayPacket {
    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);

    public LoginStartPacket(BinaryReader reader) {
        this(reader.readSizedString(16),
                reader.readBoolean() ? new PlayerPublicKey(reader) : null,
                reader.readBoolean() ? reader.readUuid() : null);
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        // TODO use uuid
        // TODO configurable check & messages
        if (publicKey != null) {
            if (!SignatureValidator.YGGDRASIL.validate(binaryWriter -> {
                if (profileId != null) {
                    binaryWriter.writeLong(profileId.getMostSignificantBits());
                    binaryWriter.writeLong(profileId.getLeastSignificantBits());
                } else {
                    MinecraftServer.LOGGER.warn("Profile ID was null for player {}, signature will not match!", username);
                }
                binaryWriter.writeLong(publicKey.expiresAt().toEpochMilli());
                binaryWriter.writeBytes(publicKey.publicKey().getEncoded());
            }, publicKey.signature())) {
                connection.sendPacket(new LoginDisconnectPacket(Component.text("Invalid Profile Public Key!")));
                connection.disconnect();
            }
            if (publicKey.expiresAt().isBefore(Instant.now())) {
                connection.sendPacket(new LoginDisconnectPacket(Component.text("Expired Profile Public Key!")));
                connection.disconnect();
            }
            connection.setPlayerPublicKey(publicKey);
        }
        final boolean isSocketConnection = connection instanceof PlayerSocketConnection;
        // Proxy support (only for socket clients) and cache the login username
        if (isSocketConnection) {
            PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.UNSAFE_setLoginUsername(username);
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
