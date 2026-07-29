package net.minestom.server.utils.mojang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.ServerFlag;
import net.minestom.server.utils.url.URLUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utils class using mojang API.
 */
public final class MojangUtils {
    private static final String FROM_UUID_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final String FROM_USERNAME_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/%s";

    // Auth
    private static final String BASE_AUTH_URL = ServerFlag.AUTH_URL.concat("?username=%s&serverId=%s");
    private static final String PREVENT_PROXY_CONNECTIONS_AUTH_URL = BASE_AUTH_URL.concat("&ip=%s");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    /**
     * Gets a player's UUID from their username
     *
     * @param username The players username
     * @return The {@link UUID}
     * @throws IOException with text detailing the exception
     */
    @Blocking
    public static UUID getUUID(String username) throws IOException {
        // Thanks stackoverflow: https://stackoverflow.com/a/19399768/13247146
        return UUID.fromString(
                formatUUID(retrieve(String.format(FROM_USERNAME_URL, validateUsername(username))).get("id").getAsString())
        );
    }

    /**
     * Gets a player's username from their UUID
     *
     * @param playerUUID The {@link UUID} of the player
     * @return The player's username
     * @throws IOException with text detailing the exception
     */
    @Blocking
    public static String getUsername(UUID playerUUID) throws IOException {
        return retrieve(String.format(FROM_UUID_URL, playerUUID)).get("name").getAsString();
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     *
     * @param uuid The UUID as a {@link UUID}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the UUID is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUuid(UUID uuid) {
        try {
            return retrieve(String.format(FROM_UUID_URL, uuid));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     *
     * @param uuid The UUID as a {@link String}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the UUID is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUuid(String uuid) {
        final UUID parsed;
        try {
            parsed = UUID.fromString(formatUUID(uuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
        return fromUuid(parsed);
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     *
     * @param username The username as a {@link String}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the username is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) return null;
        try {
            return retrieve(String.format(FROM_USERNAME_URL, username));
        } catch (IOException e) {
            return null;
        }
    }

    @Blocking
    @ApiStatus.Internal
    public static JsonObject authenticateSession(String loginUsername, String serverId, @Nullable SocketAddress userSocket) throws IOException {
        final String username = encode(loginUsername);
        final String encodedServerId = encode(serverId);

        final String url;
        // getAddress() is null for unresolved addresses, fall back to the base URL then
        final InetAddress address = ServerFlag.AUTH_PREVENT_PROXY_CONNECTIONS
                && userSocket instanceof InetSocketAddress inetSocketAddress
                ? inetSocketAddress.getAddress() : null;
        if (address != null) {
            url = String.format(PREVENT_PROXY_CONNECTIONS_AUTH_URL, username, encodedServerId, encode(address.getHostAddress()));
        } else {
            url = String.format(BASE_AUTH_URL, username, encodedServerId);
        }

        return retrieve(url);
    }

    private static String formatUUID(String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String validateUsername(String username) throws IOException {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IOException("Invalid username: " + username);
        }
        return username;
    }

    /**
     * Gets the JsonObject from a URL, expects a mojang player URL so the errors might not make sense if it is not
     *
     * @param url The url to retrieve
     * @return The {@link JsonObject} of the result
     * @throws IOException with the text detailing the exception
     */
    private static JsonObject retrieve(String url) throws IOException {
        // Retrieve from the rate-limited Mojang API
        final String response = URLUtils.getText(url);
        // If our response is "", that means the url did not get a proper object from the url
        // So the username or UUID was invalid, and therefore we return null
        if (response.isEmpty()) throw new IOException("The Mojang API is down");
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if (jsonObject.has("errorMessage")) {
            throw new IOException(jsonObject.get("errorMessage").getAsString());
        }
        return jsonObject;
    }
}
