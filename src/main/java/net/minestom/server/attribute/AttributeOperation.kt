package net.minestom.server.attribute

import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.attribute.AttributeInstance
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import java.util.function.IntFunction

enum class AttributeOperation(val id: Int) {
    ADDITION(0), MULTIPLY_BASE(1), MULTIPLY_TOTAL(2);

    companion object {
        private val VALUES = arrayOf(ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL)
        @JvmStatic
        fun fromId(id: Int): AttributeOperation? {
            return if (id >= 0 && id < VALUES.size) {
                VALUES[id]
            } else null
        }
    }
}