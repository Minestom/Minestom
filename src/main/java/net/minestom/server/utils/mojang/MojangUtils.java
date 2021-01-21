package net.minestom.server.utils.mojang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.url.URLUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Utils class using mojang API.
 */
public final class MojangUtils {

    @Nullable
    public static JsonObject fromUuid(@NotNull String uuid) {
        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
        try {
            final String response = URLUtils.getText(url);
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    @Nullable
    public static JsonObject fromUsername(@NotNull String username) {
        final String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        try {
            // Retrieve the mojang uuid from the name
            final String response = URLUtils.getText(url);
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

}
