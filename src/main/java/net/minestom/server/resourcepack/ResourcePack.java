package net.minestom.server.resourcepack;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a resource pack which can be sent with {@link Player#setResourcePack(ResourcePack)}.
 */
public class ResourcePack {

    private final String url;
    private final String hash;

    public ResourcePack(@NotNull String url, @Nullable String hash) {
        this.url = url;
        // Optional, set to empty if null
        this.hash = hash == null ? "" : hash;
    }

    /**
     * Gets the resource pack URL.
     *
     * @return the resource pack URL
     */
    @NotNull
    public String getUrl() {
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
    @NotNull
    public String getHash() {
        return hash;
    }
}
