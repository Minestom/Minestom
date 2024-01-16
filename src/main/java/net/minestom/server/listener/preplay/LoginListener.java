package net.minestom.server.listener.preplay;

import com.google.gson.Gson;
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
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.minestom.server.network.NetworkBuffer.STRING;

public final class LoginListener {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();
    private static final Gson GSON = new Gson();

    private static final Component ALREADY_CONNECTED = Component.text("You are already on this server", NamedTextColor.RED);
    public static final Component INVALID_PROXY_RESPONSE = Component.text("Invalid proxy response!", NamedTextColor.RED);

    public static void loginStartListener(@NotNull ClientLoginStartPacket packet, @NotNull PlayerConnection connection) {
        final boolean isSocketConnection = connection instanceof PlayerSocketConnection;
        // Proxy support (only for socket clients) and cache the login username
        if (isSocketConnection) {
            PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;
            socketConnection.UNSAFE_setLoginUsername(packet.username());
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
            if (CONNECTION_MANAGER.getOnlinePlayerByUsername(packet.username()) != null) {
                connection.sendPacket(new LoginDisconnectPacket(ALREADY_CONNECTED));
                connection.disconnect();
                return;
            }
            final PlayerSocketConnection socketConnection = (PlayerSocketConnection) connection;

            final byte[] publicKey = MojangAuth.getKeyPair().getPublic().getEncoded();
            byte[] nonce = new byte[4];
            ThreadLocalRandom.current().nextBytes(nonce);
            socketConnection.setNonce(nonce);
            socketConnection.sendPacket(new EncryptionRequestPacket("", publicKey, nonce));
        } else {
            final boolean bungee = BungeeCordProxy.isEnabled();
            // Offline
            final UUID playerUuid = bungee && isSocketConnection ?
                    ((PlayerSocketConnection) connection).gameProfile().uuid() :
                    CONNECTION_MANAGER.getPlayerConnectionUuid(connection, packet.username());
            CONNECTION_MANAGER.createPlayer(connection, playerUuid, packet.username());
        }
    }

    public static void loginEncryptionResponseListener(@NotNull ClientEncryptionResponsePacket packet, @NotNull PlayerConnection connection) {
        // Encryption is only support for socket connection
        if (!(connection instanceof PlayerSocketConnection socketConnection)) return;
        AsyncUtils.runAsync(() -> {
            final String loginUsername = socketConnection.getLoginUsername();
            if (loginUsername == null || loginUsername.isEmpty()) {
                // Shouldn't happen
                return;
            }

            final boolean hasPublicKey = connection.playerPublicKey() != null;
            final boolean verificationFailed = hasPublicKey || !Arrays.equals(socketConnection.getNonce(),
                    MojangCrypt.decryptUsingKey(MojangAuth.getKeyPair().getPrivate(), packet.encryptedVerifyToken()));

            if (verificationFailed) {
                MinecraftServer.LOGGER.error("Encryption failed for {}", loginUsername);
                return;
            }

            final byte[] digestedData = MojangCrypt.digestData("", MojangAuth.getKeyPair().getPublic(), getSecretKey(packet.sharedSecret()));
            if (digestedData == null) {
                // Incorrect key, probably because of the client
                MinecraftServer.LOGGER.error("Connection {} failed initializing encryption.", socketConnection.getRemoteAddress());
                connection.disconnect();
                return;
            }
            // Query Mojang's session server.
            final String serverId = new BigInteger(digestedData).toString(16);
            final String username = URLEncoder.encode(loginUsername, StandardCharsets.UTF_8);

            final String url = String.format(MojangAuth.AUTH_URL, username, serverId);
            // TODO: Add ability to add ip query tag. See: https://wiki.vg/Protocol_Encryption#Authentication

            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, throwable) -> {
                if (throwable != null) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                    if (socketConnection.getPlayer() != null) {
                        socketConnection.getPlayer().kick(Component.text("Failed to contact Mojang's Session Servers (Are they down?)"));
                    } else {
                        socketConnection.disconnect();
                    }
                    return;
                }
                try {
                    final JsonObject gameProfile = GSON.fromJson(response.body(), JsonObject.class);
                    if (gameProfile == null) {
                        // Invalid response
                        if (socketConnection.getPlayer() != null) {
                            socketConnection.getPlayer().kick(Component.text("Failed to get data from Mojang's Session Servers (Are they down?)"));
                        } else {
                            socketConnection.disconnect();
                        }
                        return;
                    }
                    socketConnection.setEncryptionKey(getSecretKey(packet.sharedSecret()));
                    UUID profileUUID = java.util.UUID.fromString(gameProfile.get("id").getAsString()
                            .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    final String profileName = gameProfile.get("name").getAsString();

                    MinecraftServer.LOGGER.info("UUID of player {} is {}", loginUsername, profileUUID);
                    CONNECTION_MANAGER.createPlayer(connection, profileUUID, profileName);
                    List<GameProfile.Property> propertyList = new ArrayList<>();
                    for (JsonElement element : gameProfile.get("properties").getAsJsonArray()) {
                        JsonObject object = element.getAsJsonObject();
                        propertyList.add(new GameProfile.Property(object.get("name").getAsString(), object.get("value").getAsString(), object.get("signature").getAsString()));
                    }
                    socketConnection.UNSAFE_setProfile(new GameProfile(profileUUID, profileName, propertyList));
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            });
        });
    }

    private static SecretKey getSecretKey(byte[] sharedSecret) {
        return MojangCrypt.decryptByteToSecretKey(MojangAuth.getKeyPair().getPrivate(), sharedSecret);
    }

    public static void loginPluginResponseListener(@NotNull ClientLoginPluginResponsePacket packet, @NotNull PlayerConnection connection) {
        // Proxy support
        if (connection instanceof PlayerSocketConnection socketConnection) {
            final String channel = socketConnection.getPluginRequestChannel(packet.messageId());
            if (channel != null) {
                boolean success = false;

                SocketAddress socketAddress = null;
                GameProfile gameProfile = null;

                // Velocity
                if (VelocityProxy.isEnabled() && channel.equals(VelocityProxy.PLAYER_INFO_CHANNEL)) {
                    byte[] data = packet.data();
                    if (data != null && data.length > 0) {
                        NetworkBuffer buffer = new NetworkBuffer(ByteBuffer.wrap(data));
                        success = VelocityProxy.checkIntegrity(buffer);
                        if (success) {
                            // Get the real connection address
                            final InetAddress address;
                            try {
                                address = InetAddress.getByName(buffer.read(STRING));
                            } catch (UnknownHostException e) {
                                MinecraftServer.getExceptionManager().handleException(e);
                                return;
                            }
                            final int port = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort();
                            socketAddress = new InetSocketAddress(address, port);
                            gameProfile = new GameProfile(buffer);
                        }
                    }
                }

                if (success) {
                    socketConnection.setRemoteAddress(socketAddress);
                    socketConnection.UNSAFE_setProfile(gameProfile);
                    CONNECTION_MANAGER.createPlayer(connection, gameProfile.uuid(), gameProfile.name());
                } else {
                    LoginDisconnectPacket disconnectPacket = new LoginDisconnectPacket(INVALID_PROXY_RESPONSE);
                    socketConnection.sendPacket(disconnectPacket);
                }
            }
        }
    }

    public static void loginAckListener(@NotNull ClientLoginAcknowledgedPacket ignored, @NotNull PlayerConnection connection) {
        final Player player = Objects.requireNonNull(connection.getPlayer());
        CONNECTION_MANAGER.doConfiguration(player, true);
    }

}
