package net.minestom.server.ping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An enum containing the different types of server list ping responses.
 *
 * @see <a href="https://wiki.vg/Server_List_Ping">https://wiki.vg/Server_List_Ping</a>
 * @see ServerListPingEvent
 */
public enum ServerListPingType {
    /**
     * The client is on version 1.16 or higher and supports full RGB with JSON text formatting.
     */
    MODERN_FULL_RGB(data -> getModernPingResponse(data, true).toString()),

    /**
     * The client is on version 1.7 or higher and doesn't support full RGB but does support JSON text formatting.
     */
    MODERN_NAMED_COLORS(data -> getModernPingResponse(data, false).toString()),

    /**
     * The client is on version 1.6 and supports a description, the player count and the version information.
     */
    LEGACY_VERSIONED(data -> getLegacyPingResponse(data, true)),

    /**
     * The client is on version 1.5 or lower and supports a description and the player count.
     */
    LEGACY_UNVERSIONED(data -> getLegacyPingResponse(data, false)),

    /**
     * The ping that is sent when {@link OpenToLAN} is enabled and sending packets.
     * Only the description formatted as a legacy string is sent.
     * Ping events with this ping version are <b>not</b> cancellable.
     */
    OPEN_TO_LAN(ServerListPingType::getOpenToLANPing);

    private final Function<ResponseData, String> pingResponseCreator;

    ServerListPingType(@NotNull Function<ResponseData, String> pingResponseCreator) {
        this.pingResponseCreator = pingResponseCreator;
    }

    /**
     * Gets the ping response for this version.
     *
     * @param responseData the response data
     * @return the response
     */
    public @NotNull String getPingResponse(@NotNull ResponseData responseData) {
        return this.pingResponseCreator.apply(responseData);
    }

    private static final String LAN_PING_FORMAT = "[MOTD]%s[/MOTD][AD]%s[/AD]";
    private static final GsonComponentSerializer FULL_RGB = GsonComponentSerializer.gson(),
            NAMED_RGB = GsonComponentSerializer.colorDownsamplingGson();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();

    /**
     * Creates a ping sent when the server is sending {@link OpenToLAN} packets.
     *
     * @param data the response data
     * @return the ping
     * @see OpenToLAN
     */
    public static @NotNull String getOpenToLANPing(@NotNull ResponseData data) {
        return String.format(LAN_PING_FORMAT, SECTION.serialize(data.getDescription()), MinecraftServer.getServer().getPort());
    }

    /**
     * Creates a legacy ping response for client versions below the Netty rewrite (1.6-).
     *
     * @param data             the response data
     * @param supportsVersions if the client supports recieving the versions of the server
     * @return the response
     */
    public static @NotNull String getLegacyPingResponse(@NotNull ResponseData data, boolean supportsVersions) {
        final String motd = SECTION.serialize(data.getDescription());

        if (supportsVersions) {
            return String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                    data.getProtocol(), data.getVersion(), motd, data.getOnline(), data.getMaxPlayer());
        } else {
            return String.format("%s\u00a7%d\u00a7%d", motd, data.getOnline(), data.getMaxPlayer());
        }
    }

    /**
     * Creates a modern ping response for client versions above the Netty rewrite (1.7+).
     *
     * @param data            the response data
     * @param supportsFullRgb if the client supports full RGB
     * @return the response
     */
    public static @NotNull JsonObject getModernPingResponse(@NotNull ResponseData data, boolean supportsFullRgb) {
        // version
        final JsonObject versionObject = new JsonObject();
        versionObject.addProperty("name", data.getVersion());
        versionObject.addProperty("protocol", data.getProtocol());

        JsonObject playersObject = null;
        if (!data.arePlayersHidden()) {
            // players info
            playersObject = new JsonObject();
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
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("version", versionObject);
        jsonObject.add("players", playersObject);
        jsonObject.addProperty("favicon", data.getFavicon());

        // description
        if (supportsFullRgb) {
            jsonObject.add("description", FULL_RGB.serializeToTree(data.getDescription()));
        } else {
            jsonObject.add("description", NAMED_RGB.serializeToTree(data.getDescription()));
        }

        return jsonObject;
    }

    /**
     * Gets the server list ping version from the protocol version.
     * This only works for modern ping responses since the Netty rewrite.
     *
     * @param version the protocol version
     * @return the corresponding server list ping version
     */
    public static @NotNull ServerListPingType fromModernProtocolVersion(int version) {
        if (version >= 713) {
            return MODERN_FULL_RGB;
        } else {
            return MODERN_NAMED_COLORS;
        }
    }
}
