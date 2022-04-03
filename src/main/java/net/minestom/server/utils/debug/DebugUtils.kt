package net.minestom.server.utils.debug

import net.minestom.server.utils.PropertyUtils
import java.lang.StackTraceElement
import java.lang.StringBuilder
import net.minestom.server.utils.debug.DebugUtils
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory

/**
 * Utils class useful for debugging purpose.
 */
@ApiStatus.Internal
object DebugUtils {
    @JvmField
    var INSIDE_TEST = PropertyUtils.getBoolean("minestom.inside-test", false)
    val LOGGER = LoggerFactory.getLogger(DebugUtils::class.java)
    private val LINE_SEPARATOR = System.getProperty("line.separator")

    /**
     * Prints the current thread stack trace elements.
     */
    @Synchronized
    fun printStackTrace() {
        val elements = Thread.currentThread().stackTrace
        val stringBuilder = StringBuilder()
        stringBuilder.append("START STACKTRACE")
        stringBuilder.append(LINE_SEPARATOR)
        for (i in 0 until Int.MAX_VALUE) {
            if (i >= elements.size) break
            val element = elements[i]
            val line = element.className + "." + element.methodName + " (line:" + element.lineNumber + ")"
            stringBuilder.append(line)
            stringBuilder.append(LINE_SEPARATOR)
        }
        stringBuilder.append("END STACKTRACE")
        LOGGER.info(stringBuilder.toString())
    }
}