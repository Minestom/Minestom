package net.minestom.server.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.NotNull;

/**
 * Vanilla ping responses.
 */
public class VanillaPingResponses {
    private static final GsonComponentSerializer FULL_RGB = GsonComponentSerializer.gson(),
            DOWNSAMPLE_RGB = GsonComponentSerializer.colorDownsamplingGson();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();


    private VanillaPingResponses() { }

    /**
     * Creates a modern ping response for client versions above the Netty rewrite.
     *
     * @param data the response data
     * @param supportsFullRgb if the client supports full RGB
     * @return the response object
     */
    public static @NotNull JsonObject getModernPingResponse(@NotNull ResponseData data, boolean supportsFullRgb) {
        // version
        final JsonObject versionObject = new JsonObject();
        versionObject.addProperty("name", data.getVersion());
        versionObject.addProperty("protocol", data.getProtocol());

        // players info
        final JsonObject playersObject = new JsonObject();
        playersObject.addProperty("max", data.getMaxPlayer());
        playersObject.addProperty("online", data.getOnline());

        // individual players
        final JsonArray sampleArray = new JsonArray();
        for (NamedAndIdentified entry : data.getEntries()) {
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("name", SECTION.serialize(entry.getName()));
            playerObject.addProperty("id", entry.getUuid().toString());
            sampleArray.add(playerObject);
        }

        playersObject.add("sample", sampleArray);

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("version", versionObject);
        jsonObject.add("players", playersObject);
        jsonObject.addProperty("favicon", data.getFavicon());

        // description
        if (supportsFullRgb) {
            jsonObject.add("description", FULL_RGB.serializeToTree(data.getDescription()));
        } else {
            jsonObject.add("description", DOWNSAMPLE_RGB.serializeToTree(data.getDescription()));
        }

        return jsonObject;
    }
}
