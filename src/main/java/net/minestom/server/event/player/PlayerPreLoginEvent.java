package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.utils.validate.Check;

import java.util.UUID;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid
 */
public class PlayerPreLoginEvent extends Event {

    private final Player player;
    private String username;
    private UUID playerUuid;

    public PlayerPreLoginEvent(Player player, String username, UUID playerUuid) {
        this.player = player;
        this.username = username;
        this.playerUuid = playerUuid;
    }

    /**
     * Get the player who is trying to connect
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the player username
     *
     * @return the player username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Change the player username
     *
     * @param username the new player username
     */
    public void setUsername(String username) {
        Check.notNull(username, "The player username cannot be null");
        this.username = username;
    }

    /**
     * Get the player uuid
     *
     * @return the player uuid
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Change the player uuid
     *
     * @param playerUuid the new player uuid
     */
    public void setPlayerUuid(UUID playerUuid) {
        Check.notNull(playerUuid, "The player uuid cannot be null");
        this.playerUuid = playerUuid;
    }
}
