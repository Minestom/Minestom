package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when a player send {@link net.minestom.server.network.packet.client.play.ClientPluginMessagePacket}
 */
public class PlayerPluginMessageEvent extends Event {

    private final Player player;
    private final String identifier;
    private final byte[] message;

    public PlayerPluginMessageEvent(Player player, String identifier, byte[] message) {
        this.player = player;
        this.identifier = identifier;
        this.message = message;
    }

    /**
     * Get the player who sent the message
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the message identifier
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the message data as a byte array
     *
     * @return the message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Get the message data as a String
     *
     * @return the message
     */
    public String getMessageString() {
        return new String(message);
    }
}
