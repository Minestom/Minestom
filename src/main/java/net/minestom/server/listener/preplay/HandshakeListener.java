package net.minestom.server.listener.preplay;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HandshakeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandshakeListener.class);

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final Component INVALID_VERSION_TEXT = Component.text("Invalid Version, please use " + MinecraftServer.VERSION_NAME, NamedTextColor.RED);

    /**
     * Indicates that a BungeeGuard authentication was invalid due to missing, multiple, or invalid tokens.
     */
    private static final Component INVALID_BUNGEE_FORWARDING = Component.text("Invalid connection, please connect through the BungeeCord proxy. If you believe this is an error, contact a server administrator.", NamedTextColor.RED);

    public static void listener(@NotNull ClientHandshakePacket packet, @NotNull PlayerConnection connection) {
        String address = packet.serverAddress();
        switch (packet.intent()) {
            case STATUS -> {
            }
            case LOGIN, TRANSFER -> address = handlePlayerLogin(packet, connection, address);
            default -> {
                // Unexpected error
            }
        }

        if (connection instanceof PlayerSocketConnection socketConnection) {
            socketConnection.refreshServerInformation(address, packet.serverPort(), packet.protocolVersion());
        }
    }

    private static String handlePlayerLogin(@NotNull ClientHandshakePacket packet, @NotNull PlayerConnection connection,
                                            @NotNull String address) {
        if (packet.protocolVersion() != MinecraftServer.PROTOCOL_VERSION) {
            // Incorrect client version
            connection.kick(INVALID_VERSION_TEXT);
            return address;
        }

        connection.markTransferred(packet.intent() == ClientHandshakePacket.Intent.TRANSFER);

        // Bungee support (IP forwarding)
        final Auth auth = MinecraftServer.process().auth();
        if (auth instanceof Auth.Bungee bungee && connection instanceof PlayerSocketConnection socketConnection) {
            final String[] split = address.split("\00");

            if (split.length == 3 || split.length == 4) {
                boolean hasProperties = split.length == 4;
                if (bungee.guard() && !hasProperties) {
                    bungeeDisconnect(socketConnection);
                    return address;
                }

                address = split[0];

                final SocketAddress socketAddress = new InetSocketAddress(split[1],
                        ((InetSocketAddress) connection.getRemoteAddress()).getPort());
                socketConnection.setRemoteAddress(socketAddress);

                UUID playerUuid = UUID.fromString(
                        split[2]
                                .replaceFirst(
                                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                                )
                );

                List<GameProfile.Property> properties = new ArrayList<>();
                if (hasProperties) {
                    boolean foundBungeeGuardToken = false;
                    final String rawPropertyJson = split[3];
                    final JsonArray propertyJson = JsonParser.parseString(rawPropertyJson).getAsJsonArray();
                    for (JsonElement element : propertyJson) {
                        final JsonObject jsonObject = element.getAsJsonObject();
                        final JsonElement name = jsonObject.get("name");
                        final JsonElement value = jsonObject.get("value");
                        final JsonElement signature = jsonObject.get("signature");
                        if (name == null || value == null) continue;

                        final String nameString = name.getAsString();
                        final String valueString = value.getAsString();
                        final String signatureString = signature == null ? null : signature.getAsString();

                        if (bungee.guard() && nameString.equals("bungeeguard-token")) {
                            if (foundBungeeGuardToken || !bungee.validToken(valueString)) {
                                bungeeDisconnect(socketConnection);
                                return address;
                            }

                            foundBungeeGuardToken = true;
                        }

                        properties.add(new GameProfile.Property(nameString, valueString, signatureString));
                    }

                    if (bungee.guard() && !foundBungeeGuardToken) {
                        bungeeDisconnect(socketConnection);
                        return address;
                    }
                }

                final GameProfile gameProfile = new GameProfile(playerUuid, "test", properties);
                socketConnection.UNSAFE_setProfile(gameProfile);
            } else {
                bungeeDisconnect(socketConnection);
                return address;
            }
        }

        return address;
    }

    private static void bungeeDisconnect(PlayerConnection connection) {
        LOGGER.warn("{} tried to log in without valid BungeeGuard forwarding information.", connection.getIdentifier());
        connection.kick(INVALID_BUNGEE_FORWARDING);
    }

}
