package net.minestom.server.utils.validate

import org.jetbrains.annotations.Contract
import java.lang.NullPointerException
import java.text.MessageFormat
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*

/**
 * Convenient class to check for common exceptions.
 */
object Check {
    @Contract("null, _ -> fail")
    fun notNull(`object`: Any?, reason: String) {
        if (Objects.isNull(`object`)) {
            throw NullPointerException(reason)
        }
    }

    @Contract("null, _, _ -> fail")
    fun notNull(`object`: Any?, reason: String, vararg arguments: Any?) {
        if (Objects.isNull(`object`)) {
            throw NullPointerException(MessageFormat.format(reason, *arguments))
        }
    }

    @Contract("true, _ -> fail")
    fun argCondition(condition: Boolean, reason: String) {
        require(!condition) { reason }
    }

    @Contract("true, _, _ -> fail")
    fun argCondition(condition: Boolean, reason: String, vararg arguments: Any?) {
        require(!condition) { MessageFormat.format(reason, *arguments) }
    }

    @Contract("_ -> fail")
    fun fail(reason: String) {
        throw IllegalArgumentException(reason)
    }

    @Contract("true, _ -> fail")
    fun stateCondition(condition: Boolean, reason: String) {
        check(!condition) { reason }
    }

    @Contract("true, _, _ -> fail")
    fun stateCondition(condition: Boolean, reason: String, vararg arguments: Any?) {
        check(!condition) { MessageFormat.format(reason, *arguments) }
    }
}