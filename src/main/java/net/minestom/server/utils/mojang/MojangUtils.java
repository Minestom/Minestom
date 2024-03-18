package net.minestom.server.utils.mojang;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.url.URLUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Utils class using mojang API.
 */
public final class MojangUtils {
    private static final String FROM_UUID_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final String FROM_USERNAME_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final Cache<String, JsonObject> URL_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    /**
     * Gets a player's UUID from their username
     * @param username The players username
     * @return The {@link UUID}
     * @throws IOException with text detailing the exception
     */
    @Blocking
    public static @NotNull UUID getUUID(String username) throws IOException {
        // Thanks stackoverflow: https://stackoverflow.com/a/19399768/13247146
        return UUID.fromString(
                retrieve(String.format(FROM_USERNAME_URL, username)).get("id")
                        .getAsString()
                        .replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                        )
        );
    }

    /**
     * Gets a player's username from their UUID
     * @param playerUUID The {@link UUID} of the player
     * @return The player's username
     * @throws IOException with text detailing the exception
     */
    @Blocking
    public static @NotNull String getUsername(UUID playerUUID) throws IOException {
        return retrieve(String.format(FROM_UUID_URL, playerUUID)).get("name").getAsString();
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     * @param uuid The UUID as a {@link UUID}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the UUID is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUuid(@NotNull UUID uuid) {
        return fromUuid(uuid.toString());
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     * @param uuid The UUID as a {@link String}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the UUID is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUuid(@NotNull String uuid) {
        try {
            return retrieve(String.format(FROM_UUID_URL, uuid));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a {@link JsonObject} with the response from the mojang API
     * @param username The username as a {@link String}
     * @return The {@link JsonObject} or {@code null} if the mojang API is down or the username is invalid
     */
    @Blocking
    public static @Nullable JsonObject fromUsername(@NotNull String username) {
        try {
            return retrieve(String.format(FROM_USERNAME_URL, username));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the JsonObject from a URL, expects a mojang player URL so the errors might not make sense if it is not
     * @param url The url to retrieve
     * @return The {@link JsonObject} of the result
     * @throws IOException with the text detailing the exception
     */
    private static @NotNull JsonObject retrieve(@NotNull String url) throws IOException {
        @Nullable final var cacheResult = URL_CACHE.getIfPresent(url);

        if (cacheResult != null) {
            return cacheResult;
        }

        final String response;
        try {
            // Retrieve from the rate-limited Mojang API
            response = URLUtils.getText(url);
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new RuntimeException(e);
        }

        // If our response is "", that means the url did not get a proper object from the url
        // So the username or UUID was invalid, and therefore we return null
        if (response.isEmpty()) {
            throw new IOException("The Mojang API is down");
        }

        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        if (jsonObject.has("errorMessage")) {
            throw new IOException(jsonObject.get("errorMessage").getAsString());
        }
        URL_CACHE.put(url, jsonObject);
        return jsonObject;
    }
}
