package net.minestom.server.ping;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.lan.OpenToLAN;

import java.util.function.Function;

/**
 * An enum containing the different types of server list ping responses.
 *
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Server_List_Ping">the Minecraft Wiki</a>
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
     * The ping that is sent when {@link OpenToLAN} is enabled and sending packets.
     * Only the description formatted as a legacy string is sent.
     * Ping events with this ping version are <b>not</b> cancellable.
     */
    OPEN_TO_LAN(ServerListPingType::getOpenToLANPing);

    private final Function<Status, String> pingResponseCreator;

    ServerListPingType(Function<Status, String> pingResponseCreator) {
        this.pingResponseCreator = pingResponseCreator;
    }

    /**
     * Gets the ping response for this version.
     *
     * @param status the response data
     * @return the response
     */
    public String getPingResponse(Status status) {
        return this.pingResponseCreator.apply(status);
    }

    private static final String LAN_PING_FORMAT = "[MOTD]%s[/MOTD][AD]%s[/AD]";
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();

    /**
     * Creates a ping sent when the server is sending {@link OpenToLAN} packets.
     *
     * @param status the response data
     * @return the ping
     * @see OpenToLAN
     */
    public static String getOpenToLANPing(Status status) {
        return String.format(LAN_PING_FORMAT, SECTION.serialize(status.description()), MinecraftServer.getServer().getPort());
    }

    /**
     * Creates a modern ping response for client versions above the Netty rewrite (1.7+).
     *
     * @param status          the response data
     * @param supportsFullRgb if the client supports full RGB colors in text components
     * @return the response
     */
    public static JsonObject getModernPingResponse(Status status, boolean supportsFullRgb) {
        JsonObject element = (JsonObject) Status.CODEC.encode(Transcoder.JSON, status).orElseThrow();

        // reset description element with downscaled colors if this version does not support RGB
        if (!supportsFullRgb) {
            GsonComponentSerializer serializer = GsonComponentSerializer.colorDownsamplingGson();
            element.add("description", serializer.serializeToTree(status.description()));
        }

        return element;
    }

    /**
     * Gets the server list ping version from the protocol version.
     * This only works for modern ping responses since the Netty rewrite.
     *
     * @param version the protocol version
     * @return the corresponding server list ping version
     */
    public static ServerListPingType fromModernProtocolVersion(int version) {
        return version >= 713 ? MODERN_FULL_RGB : MODERN_NAMED_COLORS;
    }
}
