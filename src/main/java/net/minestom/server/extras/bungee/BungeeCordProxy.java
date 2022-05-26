package net.minestom.server.extras.bungee;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;

/**
 * BungeeCord forwarding support. This does not count as a security feature, and you will still be required to manage your firewall.
 * <p>
 * Please consider using {@link net.minestom.server.extras.velocity.VelocityProxy} instead.
 */
public final class BungeeCordProxy {

    private static volatile boolean enabled;

    /**
     * Enables bungee IP forwarding.
     */
    public static void enable() {
        BungeeCordProxy.enabled = true;
    }

    /**
     * Gets if bungee IP forwarding is enabled.
     *
     * @return true if forwarding is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    public static PlayerSkin readSkin(@NotNull String json) {
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        String skinTexture = null;
        String skinSignature = null;

        for (JsonElement element : array) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonElement name = jsonObject.get("name");
            if (name == null || !name.getAsString().equals("textures")) continue;

            JsonElement value = jsonObject.get("value");
            JsonElement signature = jsonObject.get("signature");
            if (value == null || signature == null) continue;

            skinTexture = value.getAsString();
            skinSignature = signature.getAsString();
        }

        if (skinTexture != null && skinSignature != null) {
            return new PlayerSkin(skinTexture, skinSignature);
        } else {
            return null;
        }
    }
}
