package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player send {@link ClientPluginMessagePacket}.
 */
public record PlayerPluginMessageEvent(@NotNull Player player, @NotNull String identifier, byte @NotNull [] message) implements PlayerInstanceEvent {

    /**
     * Gets the message identifier.
     *
     * @return the identifier
     */
    @Override
    public @NotNull String identifier() {
        return identifier;
    }

    /**
     * Gets the message data as a byte array.
     *
     * @return the message
     */
    @Override
    public byte @NotNull[] message() {
        return message;
    }

    /**
     * Gets the message data as a String.
     *
     * @return the message
     */
    public @NotNull String messageAsString() {
        return new String(message);
    }
}
