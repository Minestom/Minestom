package net.minestom.server.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.utils.url.URLUtils;

import java.io.IOException;

/**
 * Contains all the data required to store a skin
 */
public class PlayerSkin {

    private final String textures;
    private final String signature;

    public PlayerSkin(String textures, String signature) {
        this.textures = textures;
        this.signature = signature;
    }

    /**
     * Get the skin textures value
     *
     * @return the textures value
     */
    public String getTextures() {
        return textures;
    }

    /**
     * Get the skin signature
     *
     * @return the skin signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Get a skin from a Mojang UUID
     *
     * @param uuid Mojang UUID
     * @return a player skin based on the UUID, null if not found
     */
    public static PlayerSkin fromUuid(String uuid) {
        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";

        try {
            final String response = URLUtils.getText(url);
            final JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            final JsonArray propertiesArray = jsonObject.get("properties").getAsJsonArray();

            for (JsonElement jsonElement : propertiesArray) {
                final JsonObject propertyObject = jsonElement.getAsJsonObject();
                final String name = propertyObject.get("name").getAsString();
                if (!name.equals("textures"))
                    continue;
                final String textureValue = propertyObject.get("value").getAsString();
                final String signatureValue = propertyObject.get("signature").getAsString();
                return new PlayerSkin(textureValue, signatureValue);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a skin from a Minecraft username
     *
     * @param username the Minecraft username
     * @return a skin based on a Minecraft username, null if not found
     */
    public static PlayerSkin fromUsername(String username) {
        final String url = "https://api.mojang.com/users/profiles/minecraft/" + username;

        try {
            final String response = URLUtils.getText(url);
            final JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            final String uuid = jsonObject.get("id").getAsString();
            return fromUuid(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
