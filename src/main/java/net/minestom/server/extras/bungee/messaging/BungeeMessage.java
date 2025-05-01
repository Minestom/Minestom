package net.minestom.server.extras.bungee.messaging;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public sealed interface BungeeMessage permits BungeeRequest, BungeeResponse {
    static boolean isIdentifier(@NotNull String channel) {
        return BungeeProtocol.CHANNEL.equals(channel) || "bungeecord:main".equals(channel);
    }

    static byte @NotNull [] write(@NotNull BungeeMessage message) {
        return switch (message) {
            case BungeeRequest request -> writeRequest(request);
            case BungeeResponse response -> writeResponse(response);
        };
    }

    // Requests
    static byte @NotNull [] writeRequest(@NotNull BungeeRequest request) {
        return NetworkBuffer.makeArray(BungeeRequest.SERIALIZER, request);
    }

    static @NotNull BungeeRequest readRequest(@NotNull NetworkBuffer buffer) {
        return BungeeProtocol.read(buffer, BungeeRequest.SERIALIZER);
    }

    static @NotNull BungeeRequest readRequest(byte @NotNull [] bytes) {
        return readRequest(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    static @NotNull BungeeRequest readRequest(@NotNull ClientPluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(BungeeProtocol.CHANNEL), "Channel is not the `{0}` channel!", BungeeProtocol.CHANNEL);
        return readRequest(packet.data());
    }

    static @NotNull BungeeRequest readRequest(@NotNull PluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(BungeeProtocol.CHANNEL), "Channel is not the `{0}` channel!", BungeeProtocol.CHANNEL);
        return readRequest(packet.data());
    }

    // Responses
    static byte @NotNull [] writeResponse(@NotNull BungeeResponse response) {
        return NetworkBuffer.makeArray(BungeeResponse.SERIALIZER, response);
    }

    static @NotNull BungeeResponse readResponse(@NotNull NetworkBuffer buffer) {
        return BungeeProtocol.read(buffer, BungeeResponse.SERIALIZER);
    }

    static @NotNull BungeeResponse readResponse(byte @NotNull [] bytes) {
        return readResponse(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    static @NotNull BungeeResponse readResponse(@NotNull ClientPluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(BungeeProtocol.CHANNEL), "Channel is not the `{0}` channel!", BungeeProtocol.CHANNEL);
        return readResponse(packet.data());
    }

    static @NotNull BungeeResponse readResponse(@NotNull PluginMessagePacket packet) {
        Check.argCondition(!packet.channel().equals(BungeeProtocol.CHANNEL), "Channel is not the `{0}` channel!", BungeeProtocol.CHANNEL);
        return readResponse(packet.data());
    }

    default @NotNull ClientPluginMessagePacket toClientPacket() {
        return new ClientPluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    default @NotNull PluginMessagePacket toServerPacket() {
        return new PluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    default void send(@NotNull PlayerConnection connection) {
        Check.notNull(connection, "Connection cannot be null");
        connection.sendPacket(toServerPacket());
    }

    default void send(@NotNull Audience audience) {
        Check.notNull(audience, "Audience cannot be null");
        PacketSendingUtils.sendPacket(audience, toServerPacket());
    }
}
