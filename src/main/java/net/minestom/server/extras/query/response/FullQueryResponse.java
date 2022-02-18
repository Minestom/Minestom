package net.minestom.server.extras.query.response;

import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extras.query.Query;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A full query response containing a dynamic set of responses.
 */
public class FullQueryResponse implements Writeable {
    private static final PlainComponentSerializer PLAIN = PlainComponentSerializer.plain();
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

        if (!MinecraftServer.getExtensionManager().getExtensions().isEmpty()) {
            for (Extension extension : MinecraftServer.getExtensionManager().getExtensions()) {
                builder.append(extension.getOrigin().getName())
                        .append(' ')
                        .append(extension.getOrigin().getVersion())
                        .append("; ");
            }

            builder.delete(builder.length() - 2, builder.length());
        }

        return builder.toString();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBytes(PADDING_11);

        // key-values
        for (var entry : this.kv.entrySet()) {
            writer.writeNullTerminatedString(entry.getKey(), Query.CHARSET);
            writer.writeNullTerminatedString(entry.getValue(), Query.CHARSET);
        }

        writer.writeNullTerminatedString("", Query.CHARSET);
        writer.writeBytes(PADDING_10);

        // players
        for (String player : this.players) {
            writer.writeNullTerminatedString(player, Query.CHARSET);
        }

        writer.writeNullTerminatedString("", Query.CHARSET);
    }
}
