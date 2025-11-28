package net.minestom.server.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Blocking;
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
    public static @Nullable PlayerSkin fromUuid(String uuid) {
        final JsonObject jsonObject = MojangUtils.fromUuid(uuid);
        if (jsonObject == null) return null;
        try {
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
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets a skin from a Minecraft username.
     *
     * @param username the Minecraft username
     * @return a skin based on a Minecraft username, null if not found
     */
    @Blocking
    public static @Nullable PlayerSkin fromUsername(String username) {
        final JsonObject jsonObject = MojangUtils.fromUsername(username);
        if (jsonObject == null) return null;
        try {
            final String uuid = jsonObject.get("id").getAsString();
            // Retrieve the skin data from the mojang uuid
            return fromUuid(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public record Patch(
            @Nullable Key body,
            @Nullable Key cape,
            @Nullable Key elytra,
            @Nullable Boolean slim
    ) {
        public static final Patch EMPTY = new Patch(null, null, null, null);

        public static final NetworkBuffer.Type<Patch> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.KEY.optional(), Patch::body,
                NetworkBuffer.KEY.optional(), Patch::cape,
                NetworkBuffer.KEY.optional(), Patch::elytra,
                NetworkBuffer.BOOLEAN.optional(), Patch::slim,
                Patch::new);
        public static final StructCodec<Patch> CODEC = StructCodec.struct(
                "body", Codec.KEY.optional(), Patch::body,
                "cape", Codec.KEY.optional(), Patch::cape,
                "elytra", Codec.KEY.optional(), Patch::elytra,
                "slim", Codec.BOOLEAN.optional(), Patch::slim,
                Patch::new);

        public Patch(Key body) {
            this(body, null, null, null);
        }
    }

}
