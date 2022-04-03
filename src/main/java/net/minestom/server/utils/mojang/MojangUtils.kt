package net.minestom.server.utils.mojang

import net.minestom.server.utils.url.URLUtils.getText
import net.minestom.server.MinecraftServer.Companion.exceptionManager
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minestom.server.utils.mojang.MojangUtils
import net.minestom.server.utils.url.URLUtils
import java.io.IOException
import net.minestom.server.MinecraftServer
import org.jetbrains.annotations.Blocking
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

/**
 * Utils class using mojang API.
 */
object MojangUtils {
    private const val FROM_UUID_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false"
    private const val FROM_USERNAME_URL = "https://api.mojang.com/users/profiles/minecraft/%s"
    private val URL_CACHE = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .softValues()
        .build<String, JsonObject>()

    @JvmStatic
    @Blocking
    fun fromUuid(uuid: String): JsonObject? {
        return retrieve(String.format(FROM_UUID_URL, uuid))
    }

    @JvmStatic
    @Blocking
    fun fromUsername(username: String): JsonObject? {
        return retrieve(String.format(FROM_USERNAME_URL, username))
    }

    private fun retrieve(url: String): JsonObject? {
        return URL_CACHE[url, { s: String? ->
            try {
                // Retrieve from the rate-limited Mojang API
                val response = getText(url)
                // If our response is "", that means the url did not get a proper object from the url
                // So the username or UUID was invalid, and therefore we return null
                if (response.isEmpty()) {
                    return@get null
                }
                return@get JsonParser.parseString(response).asJsonObject
            } catch (e: IOException) {
                exceptionManager.handleException(e)
                throw RuntimeException(e)
            }
        }]
    }
}