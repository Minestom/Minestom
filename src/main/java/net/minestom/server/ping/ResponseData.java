package net.minestom.server.ping;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
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
        this.online = MinecraftServer.getConnectionManager().getOnlinePlayerCount();
        this.maxPlayer = this.online + 1;
        this.description = DEFAULT_DESCRIPTION;
        this.favicon = "";
        this.playersHidden = false;
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
}
