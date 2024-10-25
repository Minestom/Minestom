package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
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

    private final PlayerConnection connection;
    private GameProfile gameProfile;
    private final LoginPluginMessageProcessor pluginMessageProcessor;

    public AsyncPlayerPreLoginEvent(@NotNull PlayerConnection connection,
                                    @NotNull GameProfile gameProfile,
                                    @NotNull LoginPluginMessageProcessor pluginMessageProcessor) {
        this.connection = connection;
        this.gameProfile = gameProfile;
        this.pluginMessageProcessor = pluginMessageProcessor;
    }

    public @NotNull PlayerConnection getConnection() {
        return connection;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
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

    @Deprecated
    public @NotNull String getUsername() {
        return gameProfile.name();
    }

    @Deprecated
    public void setUsername(@NotNull String username) {
        this.gameProfile = new GameProfile(gameProfile.uuid(), username, gameProfile.properties());
    }

    @Deprecated
    public @NotNull UUID getPlayerUuid() {
        return gameProfile.uuid();
    }
}
