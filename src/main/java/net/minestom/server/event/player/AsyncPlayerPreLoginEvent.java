package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.plugin.LoginPlugin;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Called before the player initialization, it can be used to kick the player before any connection
 * or to change his final username/uuid.
 */
public record AsyncPlayerPreLoginEvent(@NotNull PlayerConnection connection,
                                       @NotNull GameProfile gameProfile,
                                       @NotNull LoginPluginMessageProcessor pluginMessageProcessor) implements MutableEvent<AsyncPlayerPreLoginEvent> {

    public AsyncPlayerPreLoginEvent(@NotNull PlayerConnection connection,
                                    @NotNull GameProfile gameProfile,
                                    @NotNull LoginPluginMessageProcessor pluginMessageProcessor) {
        this.connection = connection;
        this.gameProfile = gameProfile;
        this.pluginMessageProcessor = pluginMessageProcessor;
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

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<AsyncPlayerPreLoginEvent> {
        private final PlayerConnection connection;
        private final LoginPluginMessageProcessor pluginMessageProcessor;

        private GameProfile gameProfile;

        public Mutator(AsyncPlayerPreLoginEvent event) {
            this.connection = event.connection;
            this.gameProfile = event.gameProfile;
            this.pluginMessageProcessor = event.pluginMessageProcessor;
        }

        public void setGameProfile(GameProfile gameProfile) {
            this.gameProfile = gameProfile;
        }

        public GameProfile getGameProfile() {
            return gameProfile;
        }

        @Contract(pure = true)
        @Override
        public @NotNull AsyncPlayerPreLoginEvent mutated() {
            return new AsyncPlayerPreLoginEvent(this.connection, this.gameProfile, this.pluginMessageProcessor);
        }
    }
}
