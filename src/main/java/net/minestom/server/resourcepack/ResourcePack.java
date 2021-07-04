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
    private final Component forcedMessage;

    /**
     * @deprecated use {@link ResourcePack#optional(String, String)}.
     */
    @Deprecated
    public ResourcePack(@NotNull String url, @Nullable String hash) {
        this(url, hash, false, null);
    }

    private ResourcePack(@NotNull String url, @Nullable String hash, boolean forced, Component forcedMessage) {
        this.url = url;
        // Optional, set to empty if null
        this.hash = hash == null ? "" : hash;
        this.forced = forced;
        this.forcedMessage = forcedMessage;
    }

    public static ResourcePack optional(@NotNull String url, @Nullable String hash) {
        return new ResourcePack(url, hash);
    }

    public static ResourcePack forced(@NotNull String url, @Nullable String hash, @Nullable Component forcedMessage) {
        return new ResourcePack(url, hash, true, forcedMessage);
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

    public @Nullable Component getForcedMessage() {
        return forcedMessage;
    }
}
