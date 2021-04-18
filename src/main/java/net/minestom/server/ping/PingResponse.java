package net.minestom.server.ping;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Separation between the versions of response, used to determine how {@link ResponseData}
 * is serialized.
 *
 * @param <T> the type of the data returned in the response
 */
@ApiStatus.NonExtendable
@FunctionalInterface
public interface PingResponse<T> {
    /**
     * Indicates the client supports full RGB with JSON text formatting.
     */
    PingResponse<JsonObject> FULL_RGB = data -> VanillaPingResponses.getModernPingResponse(data, true);

    /**
     * Indicates the client doesn't support full RGB but does support JSON text formatting.
     */
    PingResponse<JsonObject> NAMED_COLORS = data -> VanillaPingResponses.getModernPingResponse(data, true);

    /**
     * Indicates the client is incompatible with the Netty rewrite.
     *
     * @see <a href="https://wiki.vg/Server_List_Ping#1.6">https://wiki.vg/Server_List_Ping#1.6</a>
     * @deprecated This is not yet supported in Minestom
     */
    @Deprecated(forRemoval = false)
    PingResponse<Object> LEGACY_PING = null;

    /**
     * Indicates the client is on a beta version of Minecraft.
     *
     * @see <a href="https://wiki.vg/Server_List_Ping#Beta_1.8_to_1.3">https://wiki.vg/Server_List_Ping#Beta_1.8_to_1.3</a>
     * @deprecated This is not yet supported in Minestom
     */
    @Deprecated(forRemoval = false)
    PingResponse<Object> LEGACY_PING_BETA = null;

    /**
     * Creates a response from some data.
     *
     * @param data the data
     * @return the response
     */
    @NotNull T getResponse(@NotNull ResponseData data);
}
