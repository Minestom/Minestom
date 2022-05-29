package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player send {@link net.minestom.server.network.packet.client.play.ClientPluginMessagePacket}.
 */
public class PlayerPluginMessageEvent implements PlayerInstanceEvent {

    private final Player player;
    private final String identifier;
    private final byte[] message;

    public PlayerPluginMessageEvent(@NotNull Player player, @NotNull String identifier, @NotNull byte[] message) {
        this.player = player;
        this.identifier = identifier;
        this.message = message;
    }

    /**
     * Gets the message identifier.
     *
     * @return the identifier
     */
    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the message data as a byte array.
     *
     * @return the message
     */
    @NotNull
    public byte[] getMessage() {
        return message;
    }

    /**
     * Gets the message data as a String.
     *
     * @return the message
     */
    @NotNull
    public String getMessageString() {
        return new String(message);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
