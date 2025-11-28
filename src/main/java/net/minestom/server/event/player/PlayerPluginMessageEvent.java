package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;

/**
 * Called when a player send {@link ClientPluginMessagePacket}.
 */
public class PlayerPluginMessageEvent implements PlayerInstanceEvent {

    private final Player player;
    private final String identifier;
    private final byte[] message;

    public PlayerPluginMessageEvent(Player player, String identifier, byte[] message) {
        this.player = player;
        this.identifier = identifier;
        this.message = message;
    }

    /**
     * Gets the message identifier.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the message data as a byte array.
     *
     * @return the message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Gets the message data as a String.
     *
     * @return the message
     */
    public String getMessageString() {
        return new String(message);
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
