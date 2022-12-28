package net.minestom.server.network.packet.client.handshake;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record HandshakePacket(int protocolVersion, @NotNull String serverAddress,
                              int serverPort, int nextState) implements ClientPreplayPacket {

    private final static Logger LOGGER = LoggerFactory.getLogger(HandshakePacket.class);

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final Component INVALID_VERSION_TEXT = Component.text("Invalid Version, please use " + MinecraftServer.VERSION_NAME, NamedTextColor.RED);

    /**
     * Indicates that a BungeeGuard authentication was invalid due to missing, multiple, or invalid tokens.
     */
    private static final Component INVALID_BUNGEE_FORWARDING = Component.text("Invalid connection, please connect through the BungeeCord proxy. If you believe this is an error, contact a server administrator.", NamedTextColor.RED);

    public HandshakePacket {
        if (serverAddress.length() > getMaxHandshakeLength()) {
            throw new IllegalArgumentException("Server address too long: " + serverAddress.length());
        }
    }

    public HandshakePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(STRING),
                reader.read(UNSIGNED_SHORT), reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, protocolVersion);
        int maxLength = getMaxHandshakeLength();
        if (serverAddress.length() > maxLength) {
            throw new IllegalArgumentException("serverAddress is " + serverAddress.length() + " characters long, maximum allowed is " + maxLength);
        }
        writer.write(STRING, serverAddress);
        writer.write(UNSIGNED_SHORT, serverPort);
        writer.write(VAR_INT, nextState);
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        String address = serverAddress;
        // Bungee support (IP forwarding)
        if (BungeeCordProxy.isEnabled() && connection instanceof PlayerSocketConnection socketConnection && nextState == 2) {
            final String[] split = address.split("\00");

            if (split.length == 3 || split.length == 4) {
                boolean hasProperties = split.length == 4;
                if (BungeeCordProxy.isBungeeGuardEnabled() && !hasProperties) {
                    bungeeDisconnect(socketConnection);
                    return;
                }

                address = split[0];

                final SocketAddress socketAddress = new java.net.InetSocketAddress(split[1],
                        ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort());
                socketConnection.setRemoteAddress(socketAddress);

                UUID playerUuid = java.util.UUID.fromString(
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

                        if (BungeeCordProxy.isBungeeGuardEnabled() && nameString.equals("bungeeguard-token")) {
                            if (foundBungeeGuardToken || !BungeeCordProxy.isValidBungeeGuardToken(valueString)) {
                                bungeeDisconnect(socketConnection);
                                return;
                            }

                            foundBungeeGuardToken = true;
                        }

                        properties.add(new GameProfile.Property(nameString, valueString, signatureString));
                    }

                    if (BungeeCordProxy.isBungeeGuardEnabled() && !foundBungeeGuardToken) {
                        bungeeDisconnect(socketConnection);
                        return;
                    }
                }

                final GameProfile gameProfile = new GameProfile(playerUuid, "test", properties);
                socketConnection.UNSAFE_setProfile(gameProfile);
            } else {
                bungeeDisconnect(socketConnection);
                return;
            }
        }

        if (connection instanceof PlayerSocketConnection) {
            // Give to the connection the server info that the client used
            ((PlayerSocketConnection) connection).refreshServerInformation(address, serverPort, protocolVersion);
        }

        switch (nextState) {
            case 1 -> connection.setConnectionState(ConnectionState.STATUS);
            case 2 -> {
                if (protocolVersion == MinecraftServer.PROTOCOL_VERSION) {
                    connection.setConnectionState(ConnectionState.LOGIN);
                } else {
                    // Incorrect client version
                    disconnect(connection, INVALID_VERSION_TEXT);
                }
            }
            default -> {
                // Unexpected error
            }
        }
    }

    private static int getMaxHandshakeLength() {
        // BungeeGuard limits handshake length to 2500 characters, while vanilla limits it to 255
        return BungeeCordProxy.isEnabled() ? (BungeeCordProxy.isBungeeGuardEnabled() ? 2500 : Short.MAX_VALUE) : 255;
    }

    private void disconnect(@NotNull PlayerConnection connection, @NotNull Component reason) {
        connection.sendPacket(new LoginDisconnectPacket(reason));
        connection.disconnect();
    }

    private void bungeeDisconnect(@NotNull PlayerConnection connection) {
        LOGGER.warn("{} tried to log in without valid BungeeGuard forwarding information.", connection.getIdentifier());
        disconnect(connection, INVALID_BUNGEE_FORWARDING);
    }

}
