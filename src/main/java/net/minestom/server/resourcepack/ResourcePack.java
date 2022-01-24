package net.minestom.server.resourcepack;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a resource pack which can be sent with {@link Player#setResourcePack(ResourcePack)}.
 */
public class ResourcePack {

    private final String url;
    private final String hash;
    private final boolean forced;
    private final Component prompt;

    private ResourcePack(@NotNull String url, @Nullable String hash, boolean forced, @Nullable Component prompt) {
        this.url = url;
        // Optional, set to empty if null
        this.hash = hash == null ? "" : hash;
        this.forced = forced;
        this.prompt = prompt;
    }

    public static ResourcePack optional(@NotNull String url, @Nullable String hash, @Nullable Component prompt) {
        return new ResourcePack(url, hash, false, prompt);
    }

    public static ResourcePack optional(@NotNull String url, @Nullable String hash) {
        return optional(url, hash, null);
    }

    public static ResourcePack forced(@NotNull String url, @Nullable String hash, @Nullable Component prompt) {
        return new ResourcePack(url, hash, true, prompt);
    }

    public static ResourcePack forced(@NotNull String url, @Nullable String hash) {
        return forced(url, hash, null);
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
