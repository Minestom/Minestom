package net.minestom.server.extras.query.response;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A full query response containing a dynamic set of responses.
 */
public class FullQueryResponse {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final byte[] PADDING_10 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            PADDING_11 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private Map<String, String> kv;
    private List<String> players;

    /**
     * Creates a new full query response with default values set.
     */
    public FullQueryResponse() {
        this.kv = new HashMap<>();

        // populate defaults
        for (QueryKey key : QueryKey.VALUES) {
            this.kv.put(key.getKey(), key.getValue());
        }

        this.players = MinecraftServer.getConnectionManager().getOnlinePlayers()
                .stream()
                .map(player -> PLAIN.serialize(player.getName()))
                .toList();
    }

    /**
     * Puts a key-value mapping into the response.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(@NotNull QueryKey key, @NotNull String value) {
        this.put(key.getKey(), value);
    }

    /**
     * Puts a key-value mapping into the response.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(@NotNull String key, @NotNull String value) {
        this.kv.put(key, value);
    }

    /**
     * Gets the map containing the key-value mappings.
     *
     * @return the map
     */
    public @NotNull Map<String, String> getKeyValuesMap() {
        return this.kv;
    }

    /**
     * Sets the map containing the key-value mappings.
     *
     * @param map the map
     */
    public void setKeyValuesMap(@NotNull Map<String, String> map) {
        this.kv = Objects.requireNonNull(map, "map");
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    public void addPlayers(@NotNull String @NotNull ... players) {
        Collections.addAll(this.players, players);
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    public void addPlayers(@NotNull Collection<String> players) {
        this.players.addAll(players);
    }

    /**
     * Gets the list of players.
     *
     * @return the list
     */
    public @NotNull List<String> getPlayers() {
        return this.players;
    }

    /**
     * Sets the list of players.
     *
     * @param players the players
     */
    public void setPlayers(@NotNull List<String> players) {
        this.players = Objects.requireNonNull(players, "players");
    }

    /**
     * Generates the default plugins value. That being the server name and version followed
     * by the name and version for each extension.
     *
     * @return the string result
     */
    public static String generatePluginsValue() {
        StringBuilder builder = new StringBuilder(MinecraftServer.getBrandName())
                .append(' ')
                .append(MinecraftServer.VERSION_NAME);

        return builder.toString();
    }

    public static final NetworkBuffer.Type<FullQueryResponse> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, FullQueryResponse value) {
            buffer.write(NetworkBuffer.RAW_BYTES, PADDING_11);
            // key-values
            for (var entry : value.kv.entrySet()) {
                buffer.write(NetworkBuffer.STRING_TERMINATED, entry.getKey());
                buffer.write(NetworkBuffer.STRING_TERMINATED, entry.getValue());
            }
            buffer.write(NetworkBuffer.STRING_TERMINATED, "");
            buffer.write(NetworkBuffer.RAW_BYTES, PADDING_10);
            // players
            for (String player : value.players) {
                buffer.write(NetworkBuffer.STRING_TERMINATED, player);
            }
            buffer.write(NetworkBuffer.STRING_TERMINATED, "");
        }

        @Override
        public FullQueryResponse read(@NotNull NetworkBuffer buffer) {
            throw new UnsupportedOperationException("FullQueryResponse is write-only");
        }
    };
}
