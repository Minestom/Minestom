package net.minestom.server.extras.bungee

import com.google.gson.JsonParser
import net.minestom.server.extras.bungee.BungeeCordProxy
import net.minestom.server.entity.PlayerSkin

/**
 * BungeeCord forwarding support. This does not count as a security feature and you will still be required to manage your firewall.
 *
 *
 * Please consider using [net.minestom.server.extras.velocity.VelocityProxy] instead.
 */
object BungeeCordProxy {
    /**
     * Gets if bungee IP forwarding is enabled.
     *
     * @return true if forwarding is enabled
     */
    @Volatile
    var isEnabled = false
        private set

    /**
     * Enables bungee IP forwarding.
     */
    fun enable() {
        isEnabled = true
    }

    @JvmStatic
    fun readSkin(json: String): PlayerSkin? {
        val array = JsonParser.parseString(json).asJsonArray
        var skinTexture: String? = null
        var skinSignature: String? = null
        for (element in array) {
            val jsonObject = element.asJsonObject
            val name = jsonObject["name"].asString
            val value = jsonObject["value"].asString
            val signature = jsonObject["signature"].asString
            if (name == "textures") {
                skinTexture = value
                skinSignature = signature
            }
        }
        return if (skinTexture != null && skinSignature != null) {
            PlayerSkin(skinTexture, skinSignature)
        } else {
            null
        }
    }
}