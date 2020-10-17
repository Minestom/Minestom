package net.minestom.server.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the data sent to the player when refreshing the server list.
 * <p>
 * Filled by {@link ResponseDataConsumer} and specified in {@link net.minestom.server.MinecraftServer#start(String, int, ResponseDataConsumer)}.
 */
public class ResponseData {

    private final JsonObject jsonObject = new JsonObject();

    private final JsonObject versionObject = new JsonObject();
    private final JsonObject playersObject = new JsonObject();
    private final JsonArray sampleArray = new JsonArray();
    private final JsonObject descriptionObject = new JsonObject();

    private String name;
    private int protocol;

    private int maxPlayer;
    private int online;
    private final List<PingPlayer> pingPlayers = new ArrayList<>();

    private String description;

    private String favicon;

    public void setName(String name) {
        this.name = name;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public void addPlayer(String name, UUID uuid) {
        PingPlayer pingPlayer = new PingPlayer();
        pingPlayer.name = name;
        pingPlayer.uuid = uuid;
        this.pingPlayers.add(pingPlayer);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    /**
     * Converts the response data into a {@link JsonObject}.
     *
     * @return the converted json data
     */
    public JsonObject build() {
        versionObject.addProperty("name", name);
        versionObject.addProperty("protocol", protocol);

        playersObject.addProperty("max", maxPlayer);
        playersObject.addProperty("online", online);

        for (PingPlayer pingPlayer : pingPlayers) {
            JsonObject pingPlayerObject = new JsonObject();
            pingPlayerObject.addProperty("name", pingPlayer.name);
            pingPlayerObject.addProperty("id", pingPlayer.uuid.toString());
            sampleArray.add(pingPlayerObject);
        }
        playersObject.add("sample", sampleArray);

        descriptionObject.addProperty("text", description);

        jsonObject.add("version", versionObject);
        jsonObject.add("players", playersObject);
        jsonObject.add("description", descriptionObject);
        jsonObject.addProperty("favicon", favicon);
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
