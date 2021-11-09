package net.minestom.server.ping;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the data sent to the player when responding to a ping event.
 *
 * @see ServerListPingEvent
 */
public class ResponseData {
    private static final Component DEFAULT_DESCRIPTION = Component.text("Minestom Server");

    private final List<NamedAndIdentified> entries;

    private String version;
    private int protocol;
    private int maxPlayer;
    private int online;
    private Component description;
    private String favicon;
    private boolean playersHidden;

    /**
     * Constructs a new {@link ResponseData}.
     */
    public ResponseData() {
        this.entries = new ArrayList<>();
        this.version = MinecraftServer.VERSION_NAME;
        this.protocol = MinecraftServer.PROTOCOL_VERSION;
        this.online = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        this.maxPlayer = this.online + 1;
        this.description = DEFAULT_DESCRIPTION;
        this.favicon = "";
        this.playersHidden = false;
    }

    /**
     * Sets the name for the response.
     *
     * @param name The name for the response data.
     * @deprecated This is named incorrectly, use {@link #setVersion(String)} instead
     */
    @Deprecated
    public void setName(String name) {
        this.setVersion(name);
    }

    /**
     * Sets the version name for the response.
     *
     * @param version The version name for the response data.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the version name for the response.
     *
     * @return the version name for the response.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the response protocol version.
     *
     * @param protocol The protocol version for the response data.
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * Get the response protocol version.
     *
     * @return the response protocol version.
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Sets the response maximum player count.
     *
     * @param maxPlayer The maximum player count for the response data.
     */
    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    /**
     * Get the response maximum player count.
     *
     * @return the response maximum player count.
     */
    public int getMaxPlayer() {
        return maxPlayer;
    }

    /**
     * Sets the response online count.
     *
     * @param online The online count for the response data.
     */
    public void setOnline(int online) {
        this.online = online;
    }

    /**
     * Get the response online count.
     *
     * @return the response online count.
     */
    public int getOnline() {
        return online;
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     * @deprecated Use {@link #addEntries(Collection)}}
     */
    @Deprecated
    public void addPlayer(Iterable<Player> players) {
        for (Player player : players) {
            this.addPlayer(player);
        }
    }

    /**
     * Adds a player to the response.
     *
     * @param player the player
     * @deprecated Use {@link #addEntry(NamedAndIdentified)}
     */
    @Deprecated
    public void addPlayer(Player player) {
        this.addEntry(player);
    }

    /**
     * Adds a player to the response.
     *
     * @param name The name of the player.
     * @param uuid The unique identifier of the player.
     * @deprecated Use {@link #addEntry(NamedAndIdentified)} with {@link NamedAndIdentified#of(String, UUID)}
     */
    @Deprecated
    public void addPlayer(String name, UUID uuid) {
        this.addEntry(NamedAndIdentified.of(name, uuid));
    }

    /**
     * Adds a player to the response.
     * <p>
     * {@link UUID#randomUUID()} is used as the player's UUID.
     *
     * @param name The name of the player.
     * @deprecated Use {@link #addEntry(NamedAndIdentified)} with {@link NamedAndIdentified#named(String)}
     */
    @Deprecated
    public void addPlayer(String name) {
        this.addEntry(NamedAndIdentified.named(name));
    }

    /**
     * Removes all of the ping players from this {@link #entries}. The {@link #entries} list
     * will be empty this call returns.
     *
     * @deprecated Use {@link #clearEntries()}
     */
    @Deprecated
    public void clearPlayers() {
        this.clearEntries();
    }

    /**
     * Get the list of the response players.
     *
     * @return the list of the response players.
     * @deprecated Use {@link #getEntries()}. This return value is now unmodifiable and this operation is incredibly costly.
     */
    @Deprecated(forRemoval = true) // to throw an error for people using it - this method is *horrible*
    public List<PingPlayer> getPlayers() {
        return this.entries.stream()
                .map(entry -> PingPlayer.of(PlainComponentSerializer.plain().serialize(entry.getName()), entry.getUuid())).toList();
    }

    /**
     * Sets the response description.
     *
     * @param description The description for the response data.
     * @deprecated Use {@link #setDescription(Component)}
     */
    @Deprecated
    public void setDescription(String description) {
        this.description = LegacyComponentSerializer.legacySection().deserialize(description);
    }

    /**
     * Sets the response description.
     *
     * @param description The description for the response data.
     */
    public void setDescription(Component description) {
        this.description = description;
    }

    /**
     * Get the response description
     *
     * @return the response description
     */
    public Component getDescription() {
        return description;
    }

    /**
     * Sets the response favicon.
     * <p>
     * MUST start with "data:image/png;base64,"
     *
     * @param favicon The favicon for the response data.
     */
    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    /**
     * Get the response favicon.
     *
     * @return the response favicon.
     */
    public String getFavicon() {
        return favicon;
    }

    /**
     * Adds an entry to the response data sample. This can be a player or a custom object.
     *
     * @param entry the entry
     * @see NamedAndIdentified
     */
    public void addEntry(@NotNull NamedAndIdentified entry) {
        this.entries.add(entry);
    }

    /**
     * Adds a series of entries to the response data sample. These can be players or a custom object.
     *
     * @param entries the entries
     * @see NamedAndIdentified
     */
    public void addEntries(@NotNull NamedAndIdentified... entries) {
        this.addEntries(Arrays.asList(entries));
    }

    /**
     * Adds a series of entries to the response data sample. These can be players or a custom object.
     *
     * @param entries the entries
     * @see NamedAndIdentified
     */
    public void addEntries(@NotNull Collection<? extends NamedAndIdentified> entries) {
        this.entries.addAll(entries);
    }

    /**
     * Clears the entries.
     */
    public void clearEntries() {
        this.entries.clear();
    }

    /**
     * Gets a modifiable collection of the current entries.
     *
     * @return the entries
     */
    public @NotNull Collection<NamedAndIdentified> getEntries() {
        return this.entries;
    }

    /**
     * Sets whether the players are hidden or not.
     * In the vanilla client, `???` will be displayed where the online and maximum players would be.
     *
     * @param playersHidden if the players are hidden
     */
    public void setPlayersHidden(boolean playersHidden) {
        this.playersHidden = playersHidden;
    }

    /**
     * Returns if the players are hidden or not.
     *
     * @return if the players are hidden
     */
    public boolean arePlayersHidden() {
        return playersHidden;
    }

    /**
     * Converts the response data into a {@link JsonObject}.
     *
     * @return The converted response data as a json tree.
     * @deprecated Use {@link ServerListPingType#getPingResponse(ResponseData)}
     */
    @Deprecated
    public @NotNull JsonObject build() {
        return ServerListPingType.getModernPingResponse(this, true);
    }

    /**
     * Represents a player line in the server list hover.
     *
     * @deprecated See {@link NamedAndIdentified}
     */
    @Deprecated
    public static class PingPlayer {

        private static @NotNull PingPlayer of(@NotNull String name, @NotNull UUID uuid) {
            return new PingPlayer(name, uuid);
        }

        private final String name;
        private final UUID uuid;

        private PingPlayer(@NotNull String name, @NotNull UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull UUID getUuid() {
            return uuid;
        }
    }
}
