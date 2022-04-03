package net.minestom.server.utils.identity

import net.kyori.adventure.text.Component
import net.minestom.server.utils.identity.NamedAndIdentified
import net.minestom.server.utils.identity.NamedAndIdentifiedImpl
import java.util.*

/**
 * An object with a [Component] name and a [UUID] identity.
 */
interface NamedAndIdentified {
    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    val name: Component

    /**
     * Gets the UUID of this object.
     *
     * @return the uuid
     */
    val uuid: UUID

    companion object {
        /**
         * Creates a [NamedAndIdentified] instance with an empty name and a random UUID.
         *
         * @return the named and identified instance
         */
        fun empty(): NamedAndIdentified {
            return of(Component.empty(), UUID.randomUUID())
        }

        /**
         * Creates a [NamedAndIdentified] instance with a given name and a random UUID.
         *
         * @param name the name
         * @return the named and identified instance
         */
        fun named(name: String): NamedAndIdentified {
            return of(name, UUID.randomUUID())
        }

        /**
         * Creates a [NamedAndIdentified] instance with a given name and a random UUID.
         *
         * @param name the name
         * @return the named and identified instance
         */
        fun named(name: Component): NamedAndIdentified {
            return of(name, UUID.randomUUID())
        }

        /**
         * Creates a [NamedAndIdentified] instance with an empty name and a given UUID.
         *
         * @param uuid the uuid
         * @return the named and identified instance
         */
        fun identified(uuid: UUID): NamedAndIdentified {
            return of(Component.empty(), uuid)
        }

        /**
         * Creates a [NamedAndIdentified] instance with a given name and UUID.
         *
         * @param name the name
         * @param uuid the uuid
         * @return the named and identified instance
         */
        fun of(name: String, uuid: UUID): NamedAndIdentified {
            return NamedAndIdentifiedImpl(name, uuid)
        }

        /**
         * Creates a [NamedAndIdentified] instance with a given name and UUID.
         *
         * @param name the name
         * @param uuid the uuid
         * @return the named and identified instance
         */
        @JvmStatic
        fun of(name: Component, uuid: UUID): NamedAndIdentified {
            return NamedAndIdentifiedImpl(name, uuid)
        }
    }
}