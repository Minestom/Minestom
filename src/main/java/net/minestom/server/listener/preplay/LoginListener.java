package net.minestom.server.listener.preplay;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.mojangAuth.MojangAuth;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.ClientConfigurationAckPacket;
import net.minestom.server.network.packet.server.login.EncryptionRequestPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.network.plugin.LoginPlugin;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.SecureRandom;

import static net.minestom.server.network.NetworkBuffer.STRING;

public final class LoginListener {
    private static final SecureRandom NONCE_RANDOM = new SecureRandom();

    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);
    private static final Component ERROR_DURING_LOGIN = Component.text("Error during login!", NamedTextColor.RED);
    private static final Component ERROR_MALFORMED_USERNAME = Component.text("Error malformed username", NamedTextColor.RED);
    private static final Component ENCRYPTION_FAILED = Component.text("Encryption failed!", NamedTextColor.RED);
    private static final Component ERROR_MOJANG_RESPONSE = Component.text("Failed to contact Mojang's Session Servers (Are they down?)", NamedTextColor.RED);

    public static final Component INVALID_PROXY_RESPONSE = Component.text("Invalid proxy response!", NamedTextColor.RED);

    public static void loginStartListener(ClientLoginStartPacket packet, PlayerConnection connection) {
        final Auth auth = MinecraftServer.process().auth();
        switch (auth) {
            case Auth.Velocity _ when connection instanceof PlayerSocketConnection socketConnection ->
                    connection.loginPluginMessageProcessor().request(Auth.Velocity.PLAYER_INFO_CHANNEL, new byte[0])
                            .thenAccept(response -> handleVelocityProxyResponse(socketConnection, response));

            case Auth.Online(KeyPair keyPair) when connection instanceof PlayerSocketConnection socketConnection -> {
                if (MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(packet.username()) != null) {
                    connection.kick(ALREADY_CONNECTED);
                    return;
                }
                final MojangAuth.LoginChallenge challenge = MojangAuth.LoginChallenge.create(packet.username(), NONCE_RANDOM);
                socketConnection.UNSAFE_setLoginChallenge(challenge);
                socketConnection.sendPacket(new EncryptionRequestPacket("", keyPair.getPublic().getEncoded(), challenge.nonce(), true));
            }

            case Auth.Bungee _ -> {
                // LEGACY FORWARDING — use the game profile set during handshake
                assert connection instanceof PlayerSocketConnection;
                final GameProfile bungeeProfile = ((PlayerSocketConnection) connection).gameProfile();
                assert bungeeProfile != null;
                enterConfig(connection, new GameProfile(bungeeProfile.uuid(), packet.username(), bungeeProfile.properties()));
            }

            // Offline, plus Velocity/Online on non-socket connections
            default -> enterConfig(connection, new GameProfile(packet.profileId(), packet.username()));
        }
    }

    public static void loginEncryptionResponseListener(ClientEncryptionResponsePacket packet, PlayerConnection connection) {
        if (!(MinecraftServer.process().auth() instanceof Auth.Online(KeyPair keyPair))) {
            connection.kick(Component.text("Encryption is not supported in offline mode", NamedTextColor.RED));
            return;
        }
        // Encryption is only supported for socket connections
        if (!(connection instanceof PlayerSocketConnection socketConnection)) return;
        final MojangAuth.LoginChallenge challenge = socketConnection.loginChallenge();
        if (challenge == null || challenge.username().isEmpty()) {
            // Shouldn't happen, but in case
            connection.kick(ERROR_MALFORMED_USERNAME);
            return;
        }

        try {
            final SecretKey encryptionKey = MojangAuth.verifyEncryptionResponse(
                    keyPair, challenge, packet, connection.playerPublicKey() != null);
            final String serverId = MojangAuth.serverIdHash(keyPair.getPublic(), encryptionKey);
            final JsonObject hasJoined = MojangUtils.authenticateSession(
                    challenge.username(), serverId, clientIpForMojang(socketConnection.getRemoteAddress()));
            final GameProfile profile = MojangAuth.parseProfile(hasJoined);
            socketConnection.setEncryptionKey(encryptionKey);
            MinecraftServer.LOGGER.info("UUID of player {} is {}", profile.name(), profile.uuid());
            enterConfig(connection, profile);
        } catch (MojangAuth.AuthException e) {
            MinecraftServer.LOGGER.error("Encryption failed for {}: {}", challenge.username(), e.getMessage(), e);
            connection.kick(ENCRYPTION_FAILED);
        } catch (IOException e) {
            socketConnection.kick(ERROR_MOJANG_RESPONSE);
            MinecraftServer.getExceptionManager().handleException(e);
        } catch (Exception e) {
            socketConnection.kick(ERROR_DURING_LOGIN);
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    private static @Nullable InetAddress clientIpForMojang(@Nullable SocketAddress address) {
        if (!ServerFlag.AUTH_PREVENT_PROXY_CONNECTIONS) return null;
        return address instanceof InetSocketAddress inet ? inet.getAddress() : null;
    }

    private static void handleVelocityProxyResponse(PlayerSocketConnection socketConnection, LoginPlugin.Response response) {
        if (!(MinecraftServer.process().auth() instanceof Auth.Velocity velocity)) {
            socketConnection.kick(Component.text("Login plugin response is not supported in this auth mode", NamedTextColor.RED));
            return;
        }

        final byte[] data = response.payload();
        if (data == null || data.length == 0) {
            socketConnection.kick(INVALID_PROXY_RESPONSE);
            return;
        }
        final NetworkBuffer buffer = NetworkBuffer.wrap(data, 0, data.length);
        if (!velocity.checkIntegrity(buffer)) {
            socketConnection.kick(INVALID_PROXY_RESPONSE);
            return;
        }

        // Get the real connection address
        final InetAddress address;
        try {
            address = InetAddress.getByName(buffer.read(STRING));
        } catch (UnknownHostException e) {
            socketConnection.kick(INVALID_PROXY_RESPONSE);
            MinecraftServer.getExceptionManager().handleException(e);
            return;
        }
        final int port = ((InetSocketAddress) socketConnection.getRemoteAddress()).getPort();
        socketConnection.setRemoteAddress(new InetSocketAddress(address, port));
        enterConfig(socketConnection, GameProfile.SERIALIZER.read(buffer));
    }

    public static void loginPluginResponseListener(ClientLoginPluginResponsePacket packet, PlayerConnection connection) {
        try {
            LoginPluginMessageProcessor messageProcessor = connection.loginPluginMessageProcessor();
            messageProcessor.handleResponse(packet.messageId(), packet.data());
        } catch (Throwable t) {
            connection.kick(ERROR_DURING_LOGIN);
            MinecraftServer.LOGGER.error("Error handling Login Plugin Response", t);
            MinecraftServer.getExceptionManager().handleException(t);
        }
    }

    public static void loginAckListener(ClientLoginAcknowledgedPacket ignored, PlayerConnection connection) {
        if (!(connection instanceof PlayerSocketConnection socketConnection))
            throw new UnsupportedOperationException("Only socket");
        final GameProfile gameProfile = socketConnection.gameProfile();
        assert gameProfile != null;
        try {
            final Player player = MinecraftServer.getConnectionManager().createPlayer(connection, gameProfile);
            executeConfig(player, true);
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(t);
            connection.kick(ERROR_DURING_LOGIN);
        }
    }

    public static void configAckListener(ClientConfigurationAckPacket packet, Player player) {
        executeConfig(player, false);
    }

    public static void selectKnownPacks(ClientSelectKnownPacksPacket packet, Player player) {
        player.getPlayerConnection().receiveKnownPacksResponse(packet.entries());
    }

    public static void finishConfigListener(ClientFinishConfigurationPacket packet, Player player) {
        MinecraftServer.getConnectionManager().transitionConfigToPlay(player);
    }

    private static void enterConfig(PlayerConnection connection, GameProfile gameProfile) {
        Thread.startVirtualThread(() -> {
            try {
                var newGameProfile = MinecraftServer.getConnectionManager().transitionLoginToConfig(connection, gameProfile);
                if (connection instanceof PlayerSocketConnection socketConnection) {
                    socketConnection.UNSAFE_setProfile(newGameProfile);
                }
            } catch (Throwable t) {
                MinecraftServer.getExceptionManager().handleException(t);
            }
        });
    }

    private static void executeConfig(Player player, boolean isFirstConfig) {
        // We have to create another thread (even though we should already be in a virtual thread)
        // because configuration handling involves waiting for the client to send a known packs packet.
        // Which mean that we have to free up the current thread to continue reading the socket.
        Thread.startVirtualThread(() -> {
            try {
                MinecraftServer.getConnectionManager().doConfiguration(player, isFirstConfig);
            } catch (Throwable t) {
                MinecraftServer.getExceptionManager().handleException(t);
                player.kick(ERROR_DURING_LOGIN);
            }
        });
    }
}
