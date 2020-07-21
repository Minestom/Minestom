package net.minestom.server.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represent the data sent to the player when refreshing his server list
 */
public class ResponseData {

    private JsonObject jsonObject = new JsonObject();

    private JsonObject versionObject = new JsonObject();
    private JsonObject playersObject = new JsonObject();
    private JsonArray sampleArray = new JsonArray();
    private JsonObject descriptionObject = new JsonObject();

    private String name;
    private int protocol;

    private int maxPlayer;
    private int online;
    private List<PingPlayer> pingPlayers = new ArrayList<>();

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

    private static class PingPlayer {
        private String name;
        private UUID uuid;
    }
}
