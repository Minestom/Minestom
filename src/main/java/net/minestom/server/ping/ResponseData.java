package net.minestom.server.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the data sent to the player when refreshing the server list.
 *
 * <p>Filled by {@link ResponseDataConsumer} and specified in {@link
 * net.minestom.server.MinecraftServer#start(String, int, ResponseDataConsumer)}.
 */
public class ResponseData {
    private final List<PingPlayer> pingPlayers;
    private String version;
    private int protocol;
    private int maxPlayer;
    private int online;
    private Component description;

    private String favicon;

    /**
     * Constructs a new {@link ResponseData}.
     */
    public ResponseData() {
        this.pingPlayers = new ArrayList<>();
    }

    /**
     * Sets the name for the response.
     *
     * @param name The name for the response data.
     * @deprecated Use {@link #setVersion(String)}
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
     * Sets the response protocol version.
     *
     * @param protocol The protocol version for the response data.
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
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
     * Sets the response online count.
     *
     * @param online The online count for the response data.
     */
    public void setOnline(int online) {
        this.online = online;
    }

    /**
     * Adds some players to the response.
     *
     * @param players the players
     */
    public void addPlayer(Iterable<Player> players) {
        for (Player player : players) {
            this.addPlayer(player);
        }
    }

    /**
     * Adds a player to the response.
     *
     * @param player the player
     */
    public void addPlayer(Player player) {
        this.addPlayer(player.getUsername(), player.getUuid());
    }

    /**
     * Adds a player to the response.
     *
     * @param name The name of the player.
     * @param uuid The unique identifier of the player.
     */
    public void addPlayer(String name, UUID uuid) {
        PingPlayer pingPlayer = new PingPlayer();
        pingPlayer.name = name;
        pingPlayer.uuid = uuid;
        this.pingPlayers.add(pingPlayer);
    }

    /**
     * Removes all of the ping players from this {@link #pingPlayers}. The {@link #pingPlayers} list
     * will be empty this call returns.
     */
    public void clearPlayers() {
        this.pingPlayers.clear();
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
     * Sets the response favicon.
     *
     * @param favicon The favicon for the response data.
     */
    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    /**
     * Converts the response data into a {@link JsonObject}.
     *
     * @return The converted response data as a json tree.
     */
    @NotNull
    public JsonObject build() {
        // version
        final JsonObject versionObject = new JsonObject();
        versionObject.addProperty("name", this.version);
        versionObject.addProperty("protocol", this.protocol);

        // players info
        final JsonObject playersObject = new JsonObject();
        playersObject.addProperty("max", this.maxPlayer);
        playersObject.addProperty("online", this.online);

        // individual players
        final JsonArray sampleArray = new JsonArray();
        for (PingPlayer pingPlayer : this.pingPlayers) {
            JsonObject pingPlayerObject = new JsonObject();
            pingPlayerObject.addProperty("name", pingPlayer.name);
            pingPlayerObject.addProperty("id", pingPlayer.uuid.toString());
            sampleArray.add(pingPlayerObject);
        }
        playersObject.add("sample", sampleArray);

        final JsonObject descriptionObject = GsonComponentSerializer.gson().serializer()
                .toJsonTree(this.description).getAsJsonObject();

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("version", versionObject);
        jsonObject.add("players", playersObject);
        jsonObject.add("description", descriptionObject);
        jsonObject.addProperty("favicon", this.favicon);
        return jsonObject;
    }

    /**
     * Represents a player line in the server list hover.
     */
    private static class PingPlayer {
        private String name;
        private UUID uuid;
    }
}
