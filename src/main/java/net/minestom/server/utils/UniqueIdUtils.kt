package net.minestom.server.utils

import java.lang.StringBuilder
import net.minestom.server.utils.TickUtils
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.UniqueIdUtils
import org.jetbrains.annotations.ApiStatus
import java.util.regex.Pattern

/**
 * An utilities class for [UUID].
 */
@ApiStatus.Internal
object UniqueIdUtils {
    val UNIQUE_ID_PATTERN = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")

    /**
     * Checks whether the `input` string is an [UUID].
     *
     * @param input The input string to be checked
     * @return `true` if the input an unique identifier, otherwise `false`
     */
    fun isUniqueId(input: String): Boolean {
        return input.matches(UNIQUE_ID_PATTERN.pattern())
    }
}