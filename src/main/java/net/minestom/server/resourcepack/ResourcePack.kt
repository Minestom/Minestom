package net.minestom.server.resourcepack

import net.kyori.adventure.text.Component
import net.minestom.server.resourcepack.ResourcePack

/**
 * Represents a resource pack which can be sent with [Player.setResourcePack].
 */
class ResourcePack private constructor(
    /**
     * Gets the resource pack URL.
     *
     * @return the resource pack URL
     */
    val url: String, hash: String?, forced: Boolean, prompt: Component?
) {
    /**
     * Gets the resource pack hash.
     *
     *
     * WARNING: if null or empty, the player will probably waste bandwidth by re-downloading
     * the resource pack.
     *
     * @return the resource pack hash, can be empty
     */
    val hash: String
    val isForced: Boolean
    val prompt: Component?

    init {
        // Optional, set to empty if null
        this.hash = hash ?: ""
        isForced = forced
        this.prompt = prompt
    }

    companion object {
        @JvmOverloads
        fun optional(url: String, hash: String?, prompt: Component? = null): ResourcePack {
            return ResourcePack(url, hash, false, prompt)
        }

        @JvmOverloads
        fun forced(url: String, hash: String?, prompt: Component? = null): ResourcePack {
            return ResourcePack(url, hash, true, prompt)
        }
    }
}