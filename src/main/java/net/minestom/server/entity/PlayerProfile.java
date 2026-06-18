package net.minestom.server.entity;

import net.minestom.server.network.player.GameProfile;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record PlayerProfile(
        @Nullable UUID uuid,
        @Nullable String name,
        @Nullable PlayerSkin skin
) {

    public PlayerProfile withSkin(PlayerSkin skin) {
        return new PlayerProfile(uuid, name, skin);
    }

    public CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(() -> {
            if (uuid != null) {
                return new PlayerProfile(uuid, name, PlayerSkin.fromUuid(uuid.toString()));
            }
            if (name != null) {
                return new PlayerProfile(uuid, name, PlayerSkin.fromUuid(name));
            }
            return this;
        });
    }

    public static CompletableFuture<PlayerProfile> fromUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            var jsonObject = MojangUtils.fromUsername(username);
            if (jsonObject == null) return new PlayerProfile(null, username, null);

            var uuid = jsonObject.get("id").getAsString();
            return new PlayerProfile(
                    UUID.fromString(uuid),
                    username,
                    PlayerSkin.fromUuid(uuid)
            );
        });
    }

    public static CompletableFuture<PlayerProfile> fromUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> new PlayerProfile(
                uuid, null, PlayerSkin.fromUuid(uuid.toString())
        ));
    }

    public static CompletableFuture<PlayerProfile> fromGameProfile(GameProfile gameProfile) {
        return CompletableFuture.supplyAsync(() -> new PlayerProfile(
                gameProfile.uuid(), gameProfile.name(), PlayerSkin.fromUsername(gameProfile.name())
        ));
    }
}
