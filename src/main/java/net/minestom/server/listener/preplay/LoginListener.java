package net.minestom.server.listener.preplay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.server.login.EncryptionRequestPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.network.plugin.LoginPlugin;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.minestom.server.network.NetworkBuffer.STRING;

public final class LoginListener {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);
    private static final Component ERROR_DURING_LOGIN = Component.text("Error during login!", NamedTextColor.RED);
    private static final Component ERROR_MALFORMED_USERNAME = Component.text("Error malformed username", NamedTextColor.RED);
    private static final Component ENCRYPTION_FAILED = Component.text("Encryption failed!", NamedTextColor.RED);
    private static final Component ERROR_MOJANG_RESPONSE = Component.text("Failed to contact Mojang's Session Servers (Are they down?)", NamedTextColor.RED);

    public static final Component INVALID_PROXY_RESPONSE = Component.text("Invalid proxy response!", NamedTextColor.RED);

    public static void loginStartListener(@NotNull ClientLoginStartPacket packet, @NotNull PlayerConnection connection) {
        final boolean isSocketConnection = connection instanceof PlayerSocketConnection;
        // Proxy support (only for socket clients) and cache the login username
        if (isSocketConnection) {
            PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.UNSAFE_setLoginUsername(packet.username());
            // Velocity support
            if (VelocityProxy.isEnabled()) {
                connection.loginPluginMessageProcessor().request(VelocityProxy.PLAYER_INFO_CHANNEL, new byte[0])
                        .thenAccept(response -> handleVelocityProxyResponse(socketConnection, response));
                return;
            }
        }

        if (MojangAuth.isEnabled() && isSocketConnection) {
            // Mojang auth
            if (CONNECTION_MANAGER.getOnlinePlayerByUsername(packet.username()) != null) {
                connection.kick(ALREADY_CONNECTED);
                return;
            }
            final PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;

            final byte[] publicKey = MojangAuth.getKeyPair().getPublic().getEncoded();
            byte[] nonce = new byte[4];
            ThreadLocalRandom.current().nextBytes(nonce);
            socketConnection.setNonce(nonce);
            socketConnection.sendPacket(new EncryptionRequestPacket("", publicKey, nonce, true));
        } else {
            final boolean bungee = BungeeCordProxy.isEnabled();
            // Offline
            AsyncUtils.runAsync(() -> {
                try {
                    final UUID playerUuid;
                    if (bungee && isSocketConnection)
                        playerUuid = ((PlayerSocketConnection) connection).gameProfile().uuid();
                    else playerUuid = CONNECTION_MANAGER.getPlayerConnectionUuid(connection, packet.username());
                    CONNECTION_MANAGER.createPlayer(connection, playerUuid, packet.username());

                } catch (Exception exception) {
                    connection.kick(Component.text(exception.getClass().getSimpleName() + ": " + exception.getMessage()));
                    MinecraftServer.getExceptionManager().handleException(exception);
                }
            });
        }
    }

    public static void loginEncryptionResponseListener(@NotNull ClientEncryptionResponsePacket packet, @NotNull PlayerConnection connection) {
        // Encryption is only support for socket connection
        if (!(connection instanceof PlayerSocketConnection socketConnection)) return;
        AsyncUtils.runAsync(() -> {
            final String loginUsername = socketConnection.getLoginUsername();
            if (loginUsername == null || loginUsername.isEmpty()) {
                // Shouldn't happen, but in case
                connection.kick(ERROR_MALFORMED_USERNAME);
                return;
            }

            final boolean hasPublicKey = connection.playerPublicKey() != null;
            final boolean verificationFailed = hasPublicKey || !Arrays.equals(socketConnection.getNonce(),
                    MojangCrypt.decryptUsingKey(MojangAuth.getKeyPair().getPrivate(), packet.encryptedVerifyToken()));

            if (verificationFailed) {
                MinecraftServer.LOGGER.error("Encryption failed for {}", loginUsername);
                connection.kick(ENCRYPTION_FAILED);
                return;
            }

            final byte[] digestedData = MojangCrypt.digestData("", MojangAuth.getKeyPair().getPublic(), getSecretKey(packet.sharedSecret()));
            if (digestedData == null) {
                // Incorrect key, probably because of the client
                MinecraftServer.LOGGER.error("Connection {} failed initializing encryption.", socketConnection.getRemoteAddress());
                connection.kick(ENCRYPTION_FAILED);
                return;
            }
            // Query Mojang's session server.
            final String serverId = new BigInteger(digestedData).toString(16);

            try {
                final JsonObject gameProfile = MojangUtils.authenticateSession(loginUsername, serverId, socketConnection.getRemoteAddress());

                // We have verified the session, parse response.
                socketConnection.setEncryptionKey(getSecretKey(packet.sharedSecret()));
                final UUID profileUUID = UUID.fromString(gameProfile.get("id").getAsString()
                        .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                final String profileName = gameProfile.get("name").getAsString();

                MinecraftServer.LOGGER.info("UUID of player {} is {}", profileName, profileUUID);
                CONNECTION_MANAGER.createPlayer(connection, profileUUID, profileName);
                List<GameProfile.Property> propertyList = new ArrayList<>();
                for (JsonElement element : gameProfile.get("properties").getAsJsonArray()) {
                    JsonObject object = element.getAsJsonObject();
                    propertyList.add(new GameProfile.Property(object.get("name").getAsString(), object.get("value").getAsString(), object.get("signature").getAsString()));
                }
                socketConnection.UNSAFE_setProfile(new GameProfile(profileUUID, profileName, propertyList));
            } catch (IOException e) {
                socketConnection.kick(ERROR_MOJANG_RESPONSE);
                MinecraftServer.getExceptionManager().handleException(e);
            } catch (Exception e) {
                socketConnection.kick(ERROR_DURING_LOGIN);
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }

    private static SecretKey getSecretKey(byte[] sharedSecret) {
        return MojangCrypt.decryptByteToSecretKey(MojangAuth.getKeyPair().getPrivate(), sharedSecret);
    }

    private static void handleVelocityProxyResponse(PlayerSocketConnection socketConnection, LoginPlugin.Response response) {
        final byte[] data = response.payload();
        SocketAddress socketAddress = null;
        GameProfile gameProfile = null;
        boolean success = false;
        if (data != null && data.length > 0) {
            NetworkBuffer buffer = NetworkBuffer.wrap(data, 0, data.length);
            success = VelocityProxy.checkIntegrity(buffer);
            if (success) {
                // Get the real connection address
                final InetAddress address;
                try {
                    address = InetAddress.getByName(buffer.read(STRING));
                } catch (UnknownHostException e) {
                    socketConnection.kick(INVALID_PROXY_RESPONSE);
                    MinecraftServer.getExceptionManager().handleException(e);
                    return;
                }
                final int port = ((java.net.InetSocketAddress) socketConnection.getRemoteAddress()).getPort();
                socketAddress = new InetSocketAddress(address, port);
                gameProfile = GameProfile.SERIALIZER.read(buffer);
            }
        }

        if (success) {
            socketConnection.setRemoteAddress(socketAddress);
            socketConnection.UNSAFE_setProfile(gameProfile);
            CONNECTION_MANAGER.createPlayer(socketConnection, gameProfile.uuid(), gameProfile.name());
        } else {
            socketConnection.kick(INVALID_PROXY_RESPONSE);
        }
    }

    public static void loginPluginResponseListener(@NotNull ClientLoginPluginResponsePacket packet, @NotNull PlayerConnection connection) {
        try {
            LoginPluginMessageProcessor messageProcessor = connection.loginPluginMessageProcessor();
            messageProcessor.handleResponse(packet.messageId(), packet.data());
        } catch (Throwable t) {
            connection.kick(ERROR_DURING_LOGIN);
            MinecraftServer.LOGGER.error("Error handling Login Plugin Response", t);
            MinecraftServer.getExceptionManager().handleException(t);
        }
    }

    public static void loginAckListener(@NotNull ClientLoginAcknowledgedPacket ignored, @NotNull PlayerConnection connection) {
        final Player player = Objects.requireNonNull(connection.getPlayer());
        CONNECTION_MANAGER.doConfiguration(player, true);
    }

}
