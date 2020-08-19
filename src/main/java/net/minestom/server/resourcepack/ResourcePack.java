package net.minestom.server.resourcepack;

import net.minestom.server.utils.validate.Check;

/**
 * Represent a resource pack which can be send to a player
 */
public class ResourcePack {

    private final String url;
    private final String hash;

    public ResourcePack(String url, String hash) {
        Check.notNull(url, "The resource pack url cannot be null");
        this.url = url;
        // Optional, set to empty if null
        this.hash = hash == null ? "" : hash;
    }

    /**
     * Get the resource pack URL
     *
     * @return the resource pack URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the resource pack hash
     * <p>
     * WARNING: if null or empty, the player will probably waste bandwidth by re-downloading
     * the resource pack
     *
     * @return the resource pack hash
     */
    public String getHash() {
        return hash;
    }
}
