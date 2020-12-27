package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid.
 */
public class AsyncPlayerPreLoginEvent extends PlayerEvent {

    private String username;
    private UUID playerUuid;

    public AsyncPlayerPreLoginEvent(@NotNull Player player, @NotNull String username, @NotNull UUID playerUuid) {
        super(player);
        this.username = username;
        this.playerUuid = playerUuid;
    }

    /**
     * Gets the player username.
     *
     * @return the player username
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Changes the player username.
     *
     * @param username the new player username
     */
    public void setUsername(@NotNull String username) {
        Check.notNull(username, "The player username cannot be null");
        this.username = username;
    }

    /**
     * Gets the player uuid.
     *
     * @return the player uuid
     */
    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Changes the player uuid.
     *
     * @param playerUuid the new player uuid
     */
    public void setPlayerUuid(@NotNull UUID playerUuid) {
        Check.notNull(playerUuid, "The player uuid cannot be null");
        this.playerUuid = playerUuid;
    }
}
