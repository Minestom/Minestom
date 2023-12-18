package net.minestom.server.resourcepack;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a resource pack which can be sent with {@link Player#setResourcePack(ResourcePack)}.
 */
public class ResourcePack {

    private final UUID id;
    private final String url;
    private final String hash;
    private final boolean forced;
    private final Component prompt;

    private ResourcePack(@NotNull UUID id, @NotNull String url, @Nullable String hash, boolean forced, @Nullable Component prompt) {
        this.id = id;
        this.url = url;
        // Optional, set to empty if null
        this.hash = hash == null ? "" : hash;
        this.forced = forced;
        this.prompt = prompt;
    }

    public static ResourcePack optional(@NotNull UUID id, @NotNull String url, @Nullable String hash, @Nullable Component prompt) {
        return new ResourcePack(id, url, hash, false, prompt);
    }

    public static ResourcePack optional(@NotNull UUID id, @NotNull String url, @Nullable String hash) {
        return optional(id, url, hash, null);
    }

    public static ResourcePack forced(@NotNull UUID id, @NotNull String url, @Nullable String hash, @Nullable Component prompt) {
        return new ResourcePack(id, url, hash, true, prompt);
    }

    public static ResourcePack forced(@NotNull UUID id, @NotNull String url, @Nullable String hash) {
        return forced(id, url, hash, null);
    }

    public @NotNull UUID getId() {
        return id;
    }

    /**
     * Gets the resource pack URL.
     *
     * @return the resource pack URL
     */
    public @NotNull String getUrl() {
        return url;
    }

    /**
     * Gets the resource pack hash.
     * <p>
     * WARNING: if null or empty, the player will probably waste bandwidth by re-downloading
     * the resource pack.
     *
     * @return the resource pack hash, can be empty
     */
    public @NotNull String getHash() {
        return hash;
    }

    public boolean isForced() {
        return forced;
    }

    public @Nullable Component getPrompt() {
        return prompt;
    }

}
