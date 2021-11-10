package net.minestom.server.extras.query.response;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * An enum of default query keys.
 */
public enum QueryKey {
    HOSTNAME(() -> "A Minestom Server"),
    GAME_TYPE(() -> "SMP"),
    GAME_ID("game_id", () -> "MINECRAFT"),
    VERSION(() -> MinecraftServer.VERSION_NAME),
    PLUGINS(FullQueryResponse::generatePluginsValue),
    MAP(() -> "world"),
    NUM_PLAYERS("numplayers", () -> String.valueOf(MinecraftServer.getConnectionManager().getOnlinePlayers().size())),
    MAX_PLAYERS("maxplayers", () -> String.valueOf(MinecraftServer.getConnectionManager().getOnlinePlayers().size() + 1)),
    HOST_PORT("hostport", () -> String.valueOf(MinecraftServer.getServer().getPort())),
    HOST_IP("hostip", () -> Objects.requireNonNullElse(MinecraftServer.getServer().getAddress(), "localhost"));

    static QueryKey[] VALUES = QueryKey.values();

    private final String key;
    private final Supplier<String> value;

    QueryKey(@NotNull Supplier<String> value) {
        this(null, value);
    }

    QueryKey(@Nullable String key, @NotNull Supplier<String> value) {
        this.key = Objects.requireNonNullElse(key, this.name().toLowerCase(Locale.ROOT).replace('_', ' '));
        this.value = value;
    }

    /**
     * Gets the key of this query key.
     *
     * @return the key
     */
    public @NotNull String getKey() {
        return this.key;
    }

    /**
     * Gets the value of this query key.
     *
     * @return the value
     */
    public @NotNull String getValue() {
        return this.value.get();
    }
}
