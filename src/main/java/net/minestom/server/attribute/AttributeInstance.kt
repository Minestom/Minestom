package net.minestom.server.attribute

import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.attribute.AttributeInstance
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import java.util.*
import java.util.function.Consumer
import java.util.function.IntFunction

/**
 * Represents an instance of an attribute and its modifiers.
 */
class AttributeInstance(
    /**
     * Gets the attribute associated to this instance.
     *
     * @return the associated attribute
     */
    val attribute: Attribute, private val propertyChangeListener: Consumer<AttributeInstance>?
) {
    private val modifiers: MutableMap<UUID, AttributeModifier?> = HashMap()
    private var baseValue: Float

    /**
     * Gets the value of this instance calculated with modifiers applied.
     *
     * @return the attribute value
     */
    var value = 0.0f
        private set

    init {
        baseValue = attribute.defaultValue()
        refreshCachedValue()
    }

    /**
     * The base value of this instance without modifiers
     *
     * @return the instance base value
     * @see .setBaseValue
     */
    fun getBaseValue(): Float {
        return baseValue
    }

    /**
     * Sets the base value of this instance.
     *
     * @param baseValue the new base value
     * @see .getBaseValue
     */
    fun setBaseValue(baseValue: Float) {
        if (this.baseValue != baseValue) {
            this.baseValue = baseValue
            refreshCachedValue()
        }
    }

    /**
     * Add a modifier to this instance.
     *
     * @param modifier the modifier to add
     */
    fun addModifier(modifier: AttributeModifier) {
        if (modifiers.putIfAbsent(modifier.id, modifier) == null) {
            refreshCachedValue()
        }
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier the modifier to remove
     */
    fun removeModifier(modifier: AttributeModifier) {
        if (modifiers.remove(modifier.id) != null) {
            refreshCachedValue()
        }
    }

    /**
     * Get the modifiers applied to this instance.
     *
     * @return the modifiers.
     */
    fun getModifiers(): Collection<AttributeModifier?> {
        return modifiers.values
    }

    /**
     * Recalculate the value of this attribute instance using the modifiers.
     */
    private fun refreshCachedValue() {
        val modifiers = getModifiers()
        var base = getBaseValue()
        for (modifier in modifiers.stream()
            .filter { mod: AttributeModifier? -> mod.getOperation() == AttributeOperation.ADDITION }
            .toArray<AttributeModifier> { _Dummy_.__Array__() }) {
            base += modifier.amount
        }
        var result = base
        for (modifier in modifiers.stream()
            .filter { mod: AttributeModifier? -> mod.getOperation() == AttributeOperation.MULTIPLY_BASE }
            .toArray<AttributeModifier> { _Dummy_.__Array__() }) {
            result += base * modifier.amount
        }
        for (modifier in modifiers.stream()
            .filter { mod: AttributeModifier? -> mod.getOperation() == AttributeOperation.MULTIPLY_TOTAL }
            .toArray<AttributeModifier> { _Dummy_.__Array__() }) {
            result *= 1.0f + modifier.amount
        }
        value = Math.min(result, attribute.maxValue())

        // Signal entity
        propertyChangeListener?.accept(this)
    }
}