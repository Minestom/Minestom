package net.minestom.server.extras.bungee.messaging;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * BungeeCord Messaging interface.
 * <p>
 * This interface holds all the methods to read and write BungeeCord messages.
 * It also provides methods to send messages to audiences and player connections.
 * <p>
 * You can construct requests using {@link BungeeRequest} and responses using {@link BungeeResponse}.
 * For example, to send a request to a player connection, you can do:
 * <pre>
 * {@code
 *     Player player = ...;
 *     BungeeRequest request = new BungeeRequest.UUID();
 *     request.send(connection);
 * }
 * </pre>
 * <p>
 * To receive the response, you can listen to the {@link PlayerPluginMessageEvent} event.
 * For example, to receive a response from a player connection, you can do:
 * <pre>
 * {@code
 * eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
 *     if (!BungeeMessage.isIdentifier(event.channel())) return;
 *     BungeeResponse response = BungeeMessage.readResponse(event.getMessage());
 *     // Handle the response here
 *     switch (response) {
 *         case BungeeResponse.UUID(UUID uuid) -> {
 *             // Handle the UUID response
 *         }
 *         ...
 *     }
 * });
 * }
 * </pre>
 * Or you can use the shorthand version with {@link BungeeMessage#readResponse(PlayerPluginMessageEvent)}
 * Using the example below:
 * <pre>
 * {@code
 * eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
 *      BungeeResponse response = BungeeMessage.readResponse(event);
 *      if (response == null) return;
 *      // Handle the response here
 *      switch (response) {
 *          ...
 *      }
 * });
 * }
 * </pre>
 * Some notes about this class include attempting to hide all the serialization implementation behind the protocol,
 * We still expose all the serializers for the requests and responses in case you want to use them, but don't expect them
 * to be stable, but they will probably be stable as the messaging system is likely to not change.
 */
public sealed interface BungeeMessage permits BungeeRequest, BungeeResponse {
    static boolean isIdentifier(@Nullable String channel) {
        return BungeeProtocol.isIdentifier(channel);
    }

    static byte @NotNull [] write(@NotNull BungeeMessage message) {
        Check.notNull(message, "Message cannot be null");
        return switch (message) {
            case BungeeRequest request -> writeRequest(request);
            case BungeeResponse response -> writeResponse(response);
        };
    }

    // Requests
    static byte @NotNull [] writeRequest(@NotNull BungeeRequest request) {
        Check.notNull(request, "Request cannot be null");
        return NetworkBuffer.makeArray(BungeeRequest.SERIALIZER, request);
    }

    static @NotNull BungeeRequest readRequest(@NotNull NetworkBuffer buffer) {
        Check.notNull(buffer, "Buffer cannot be null!");
        return BungeeProtocol.read(buffer, BungeeRequest.SERIALIZER);
    }

    static @NotNull BungeeRequest readRequest(byte @NotNull [] bytes) {
        Check.notNull(bytes, "Bytes cannot be null!");
        return readRequest(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    // Responses
    static byte @NotNull [] writeResponse(@NotNull BungeeResponse response) {
        Check.notNull(response, "Response cannot be null");
        return NetworkBuffer.makeArray(BungeeResponse.SERIALIZER, response);
    }

    static @NotNull BungeeResponse readResponse(@NotNull NetworkBuffer buffer) {
        Check.notNull(buffer, "Buffer cannot be null!");
        return BungeeProtocol.read(buffer, BungeeResponse.SERIALIZER);
    }

    static @NotNull BungeeResponse readResponse(byte @NotNull [] bytes) {
        Check.notNull(bytes, "Bytes cannot be null!");
        return readResponse(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    /**
     * Reads the response from the {@link PlayerPluginMessageEvent} event.
     * @param event the event to read the response from
     * @return the response, or null if the event is not a BungeeCord message
     */
    static @Nullable BungeeResponse readResponse(@NotNull PlayerPluginMessageEvent event) {
        Check.notNull(event, "Event cannot be null");
        if (!isIdentifier(event.getIdentifier())) return null;
        return readResponse(event.getMessage());
    }

    static void send(@NotNull PlayerConnection connection, @NotNull BungeeMessage message) {
        Check.notNull(connection, "Connection cannot be null");
        Check.notNull(message, "Message cannot be null");
        connection.sendPacket(message.toPacket());
    }

    static void send(@NotNull Audience audience, @NotNull BungeeMessage message) {
        Check.notNull(audience, "Audience cannot be null");
        Check.notNull(message, "Message cannot be null");
        PacketSendingUtils.sendPacket(audience, message.toPacket());
    }

    static void sendSingle(@NotNull Collection<Audience> audiences, @NotNull BungeeMessage message) {
        Check.notNull(audiences, "Audiences cannot be null");
        Check.notNull(message, "Message cannot be null");
        Check.argCondition(audiences.isEmpty(), "Audiences cannot be empty");
        List<Audience> collection = new ArrayList<>(audiences);
        Collections.shuffle(collection);
        BungeeMessage.send(collection.getFirst(), message);
    }

    default @NotNull ClientPluginMessagePacket toClientPacket() {
        return new ClientPluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    default @NotNull PluginMessagePacket toPacket() {
        return new PluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    default void send(@NotNull PlayerConnection connection) {
        Check.notNull(connection, "Connection cannot be null");
        BungeeMessage.send(connection, this);
    }

    default void send(@NotNull Audience audience) {
        Check.notNull(audience, "Audience cannot be null");
        BungeeMessage.send(audience, this);
    }

    default void sendSingle(@NotNull Collection<Audience> audiences) {
        Check.notNull(audiences, "Audience cannot be null");
        Check.argCondition(audiences.isEmpty(), "Audiences cannot be empty");
        BungeeMessage.sendSingle(audiences, this);
    }
}
