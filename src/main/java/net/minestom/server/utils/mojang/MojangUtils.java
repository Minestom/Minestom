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

    @Blocking
    public static @Nullable JsonObject fromUuid(@NotNull String uuid) {
        final String url = String.format(FROM_UUID_URL, uuid);
        return retrieve(url, uuid);
    }

    @Blocking
    public static @Nullable JsonObject fromUsername(@NotNull String username) {
        final String url = String.format(FROM_USERNAME_URL, username);
        return retrieve(url, username);
    }

    private static @Nullable JsonObject retrieve(@NotNull String url, @NotNull String key) {
        return URL_CACHE.get(key, s -> {
            try {
                // Retrieve from the rate-limited Mojang API
                final String response = URLUtils.getText(url);
                return JsonParser.parseString(response).getAsJsonObject();
            } catch (IOException e) {
                MinecraftServer.getExceptionManager().handleException(e);
                throw new RuntimeException(e);
            }
        });
    }
}
