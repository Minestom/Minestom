package net.minestom.server.attribute

import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.attribute.AttributeInstance
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import java.util.*
import java.util.function.IntFunction

/**
 * Represent an attribute modifier.
 */
class AttributeModifier
/**
 * Creates a new modifier.
 *
 * @param id        the id of this modifier
 * @param name      the name of this modifier
 * @param amount    the value of this modifier
 * @param operation the operation to apply this modifier with
 */(
    /**
     * Gets the id of this modifier.
     *
     * @return the id of this modifier
     */
    val id: UUID,
    /**
     * Gets the name of this modifier.
     *
     * @return the name of this modifier
     */
    val name: String,
    /**
     * Gets the value of this modifier.
     *
     * @return the value of this modifier
     */
    val amount: Float,
    /**
     * Gets the operation of this modifier.
     *
     * @return the operation of this modifier
     */
    val operation: AttributeOperation
) {

    /**
     * Creates a new modifier with a random id.
     *
     * @param name      the name of this modifier
     * @param amount    the value of this modifier
     * @param operation the operation to apply this modifier with
     */
    constructor(name: String, amount: Float, operation: AttributeOperation) : this(
        UUID.randomUUID(),
        name,
        amount,
        operation
    ) {
    }
}