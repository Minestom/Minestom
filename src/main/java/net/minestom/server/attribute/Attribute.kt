package net.minestom.server.attribute

import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.attribute.AttributeInstance
import net.minestom.server.attribute.AttributeModifier
import net.minestom.server.attribute.AttributeOperation
import java.util.function.IntFunction

/**
 * Represents a [living entity][net.minestom.server.entity.LivingEntity] attribute.
 */
class Attribute(: String?, : Float, : Float) {
    init {
        if ( > ){
            throw IllegalArgumentException("Default value cannot be greater than the maximum allowed")
        }
    }

    /**
     * Register this attribute.
     *
     * @return this attribute
     * @see .fromKey
     * @see .values
     */
    fun register(): Attribute {
        ATTRIBUTES[key] = this
        return this
    }

    companion object {
        private val ATTRIBUTES: MutableMap<String, Attribute> = ConcurrentHashMap()
        @JvmField
        val MAX_HEALTH = Attribute("generic.max_health", 20, 1024).register()
        @JvmField
        val FOLLOW_RANGE = Attribute("generic.follow_range", 32, 2048).register()
        @JvmField
        val KNOCKBACK_RESISTANCE = Attribute("generic.knockback_resistance", 0, 1).register()
        @JvmField
        val MOVEMENT_SPEED = Attribute("generic.movement_speed", 0.25f, 1024).register()
        val ATTACK_DAMAGE = Attribute("generic.attack_damage", 2, 2048).register()
        val ATTACK_SPEED = Attribute("generic.attack_speed", 4, 1024).register()
        val FLYING_SPEED = Attribute("generic.flying_speed", 0.4f, 1024).register()
        val ARMOR = Attribute("generic.armor", 0, 30).register()
        val ARMOR_TOUGHNESS = Attribute("generic.armor_toughness", 0, 20).register()
        val ATTACK_KNOCKBACK = Attribute("generic.attack_knockback", 0, 5).register()
        val LUCK = Attribute("generic.luck", 0, 1024).register()
        val HORSE_JUMP_STRENGTH = Attribute("horse.jump_strength", 0.7f, 2).register()
        val ZOMBIE_SPAWN_REINFORCEMENTS = Attribute("zombie.spawn_reinforcements", 0, 1)
            .register()

        /**
         * Retrieves an attribute by its key.
         *
         * @param key the key of the attribute
         * @return the attribute for the key or null if not any
         */
        @JvmStatic
        fun fromKey(key: String): Attribute? {
            return ATTRIBUTES[key]
        }

        /**
         * Retrieves all registered attributes.
         *
         * @return an array containing all registered attributes
         */
        fun values(): Collection<Attribute> {
            return ATTRIBUTES.values
        }
    }
}