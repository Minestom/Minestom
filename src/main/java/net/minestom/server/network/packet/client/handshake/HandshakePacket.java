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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record HandshakePacket(int protocolVersion, @NotNull String serverAddress,
                              int serverPort, int nextState) implements ClientPreplayPacket {

    /**
     * Text sent if a player tries to connect with an invalid version of the client
     */
    private static final Component INVALID_VERSION_TEXT = Component.text("Invalid Version, please use " + MinecraftServer.VERSION_NAME, NamedTextColor.RED);
    private static final Component INVALID_BUNGEE_FORWARDING = Component.text("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!", NamedTextColor.RED);

    public HandshakePacket {
        if (serverAddress.length() > (BungeeCordProxy.isEnabled() ? Short.MAX_VALUE : 255)) {
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
        int maxLength = BungeeCordProxy.isEnabled() ? Short.MAX_VALUE : 255;
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
                if (split.length == 4) {
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

                        properties.add(new GameProfile.Property(nameString, valueString, signatureString));
                    }
                }

                final GameProfile gameProfile = new GameProfile(playerUuid, "test", properties);
                socketConnection.UNSAFE_setProfile(gameProfile);
            } else {
                socketConnection.sendPacket(new LoginDisconnectPacket(INVALID_BUNGEE_FORWARDING));
                socketConnection.disconnect();
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
                    connection.sendPacket(new LoginDisconnectPacket(INVALID_VERSION_TEXT));
                    connection.disconnect();
                }
            }
            default -> {
                // Unexpected error
            }
        }
    }
}
