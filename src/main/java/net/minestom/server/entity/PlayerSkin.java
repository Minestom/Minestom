package net.minestom.server.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains all the data required to store a skin.
 * <p>
 * Can be applied to a player with {@link Player#setSkin(PlayerSkin)}
 * or in the linked event {@link net.minestom.server.event.player.PlayerSkinInitEvent}.
 */
public record PlayerSkin(String textures, String signature) {

    /**
     * Gets a skin from a Mojang UUID.
     *
     * @param uuid Mojang UUID
     * @return a player skin based on the UUID, null if not found
     */
    @Blocking
    public static @Nullable PlayerSkin fromUuid(@NotNull String uuid) {
        final JsonObject jsonObject = MojangUtils.fromUuid(uuid);
        if (jsonObject == null) return null;
        final JsonArray propertiesArray = jsonObject.get("properties").getAsJsonArray();
        for (JsonElement jsonElement : propertiesArray) {
            final JsonObject propertyObject = jsonElement.getAsJsonObject();
            final String name = propertyObject.get("name").getAsString();
            if (!name.equals("textures")) continue;
            final String textureValue = propertyObject.get("value").getAsString();
            final String signatureValue = propertyObject.get("signature").getAsString();
            return new PlayerSkin(textureValue, signatureValue);
        }
        return null;
    }

    /**
     * Gets a skin from a Minecraft username.
     *
     * @param username the Minecraft username
     * @return a skin based on a Minecraft username, null if not found
     */
    @Blocking
    public static @Nullable PlayerSkin fromUsername(@NotNull String username) {
        final JsonObject jsonObject = MojangUtils.fromUsername(username);
        if (jsonObject == null) return null;
        final String uuid = jsonObject.get("id").getAsString();
        // Retrieve the skin data from the mojang uuid
        return fromUuid(uuid);
    }

    /**
     * @deprecated use {@link #textures()}
     */
    @Deprecated
    public String getTextures() {
        return textures;
    }

    /**
     * @deprecated use {@link #signature()}
     */
    @Deprecated
    public String getSignature() {
        return signature;
    }
}
