package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid.
 */
public class AsyncPlayerPreLoginEvent implements PlayerEvent {

    private final Player player;
    private String username;
    private UUID playerUuid;

    public AsyncPlayerPreLoginEvent(@NotNull Player player) {
        this.player = player;
        this.username = player.getUsername();
        this.playerUuid = player.getUuid();
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
        this.playerUuid = playerUuid;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
