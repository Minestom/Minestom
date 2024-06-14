package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import net.minestom.server.network.plugin.LoginPluginResponse;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid.
 */
public class AsyncPlayerPreLoginEvent implements PlayerEvent {

    private final Player player;
    private final LoginPluginMessageProcessor pluginMessageProcessor;

    private String username;
    private UUID playerUuid;

    public AsyncPlayerPreLoginEvent(@NotNull Player player, @NotNull LoginPluginMessageProcessor pluginMessageProcessor) {
        this.player = player;
        this.pluginMessageProcessor = pluginMessageProcessor;
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

    /**
     * Sends a login plugin message request. Can be useful to negotiate with modded clients or
     * proxies before moving on to the Configuration state.
     *
     * @param channel the plugin message channel
     * @param requestPayload the contents of the plugin message, can be null for empty
     *
     * @return a CompletableFuture for the response. The thread on which it completes is asynchronous.
     */
    public @NotNull CompletableFuture<LoginPluginResponse> sendPluginRequest(String channel, byte[] requestPayload) {
        return pluginMessageProcessor.request(channel, requestPayload);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
