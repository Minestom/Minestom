package net.minestom.server.extras.bungee;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * BungeeCord forwarding support. Enabling BungeeGuard support with {@link #setBungeeGuardTokens(Set)} helps to secure the server,
 * but managing your firewall is still recommended.
 * <p>
 * Please consider using {@link net.minestom.server.extras.velocity.VelocityProxy} instead.
 */
public final class BungeeCordProxy {

    /**
     * Text sent if a player connects without any BungeeGuard token and BungeeGuard is enabled
     */
    public static final Component NO_BUNGEE_GUARD_TOKEN = Component.text("No BungeeGuard token provided", NamedTextColor.RED);

    private static final Component MULTIPLE_TOKENS = Component.text("Multiple BungeeGuard tokens", NamedTextColor.RED);

    private static final Component INVALID_TOKEN = Component.text("Invalid BungeeGuard token", NamedTextColor.RED);

    private static volatile boolean enabled;

    private static Set<String> bungeeGuardTokens = null;

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

    public static void setBungeeGuardTokens(@Nullable Set<String> tokens) {
        bungeeGuardTokens = tokens;
    }

    public static boolean isBungeeGuardEnabled() {
        return bungeeGuardTokens != null;
    }

    public static int getMaxHandshakeLength() {
        // BungeeGuard limits handshake length to 2500 characters, while vanilla limits it to 255
        return isEnabled() ? (isBungeeGuardEnabled() ? 2500 : Short.MAX_VALUE) : 255;
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

    public static @NotNull Pair<PlayerSkin, Component> readSkinBungeeGuard(@NotNull String json) {
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        boolean foundToken = false;
        Component message = NO_BUNGEE_GUARD_TOKEN;
        PlayerSkin skin = null;

        for (JsonElement element : array) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonElement name = jsonObject.get("name");
            if (name != null) {
                switch (name.getAsString()) {
                    case "textures" -> {
                        JsonElement value = jsonObject.get("value");
                        JsonElement signature = jsonObject.get("signature");
                        if (value == null || signature == null) continue;

                        skin = new PlayerSkin(value.getAsString(), signature.getAsString());
                    }
                    case "bungeeguard-token" -> {
                        if (foundToken) {
                            message = MULTIPLE_TOKENS;
                            break;
                        }

                        foundToken = true;
                        JsonElement value = jsonObject.get("value");
                        if (value == null) break;

                        if (!bungeeGuardTokens.contains(value.getAsString())) {
                            message = INVALID_TOKEN;
                            break;
                        }

                        message = null;
                    }
                }
            }
        }

        return Pair.of(skin, message);
    }

}
