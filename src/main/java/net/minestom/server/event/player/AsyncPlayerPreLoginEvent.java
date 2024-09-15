package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.network.plugin.LoginPlugin;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid.
 */
public class AsyncPlayerPreLoginEvent implements Event {

    private UUID uuid;
    private String username;
    private final LoginPluginMessageProcessor pluginMessageProcessor;

    public AsyncPlayerPreLoginEvent(@NotNull UUID uuid,
                                    @NotNull String username,
                                    @NotNull LoginPluginMessageProcessor pluginMessageProcessor) {
        this.uuid = uuid;
        this.username = username;
        this.pluginMessageProcessor = pluginMessageProcessor;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public @NotNull UUID getPlayerUuid() {
        return uuid;
    }

    /**
     * Sends a login plugin message request. Can be useful to negotiate with modded clients or
     * proxies before moving on to the Configuration state.
     *
     * @param channel        the plugin message channel
     * @param requestPayload the contents of the plugin message, can be null for empty
     * @return a CompletableFuture for the response. The thread on which it completes is asynchronous.
     */
    public @NotNull CompletableFuture<LoginPlugin.Response> sendPluginRequest(String channel, byte[] requestPayload) {
        return pluginMessageProcessor.request(channel, requestPayload);
    }
}
