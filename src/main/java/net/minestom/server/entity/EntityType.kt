package net.minestom.server.entity

import net.minestom.server.registry.ProtocolObject
import net.minestom.server.entity.EntityTypes
import net.minestom.server.registry.Registry.EntityEntry
import net.minestom.server.utils.NamespaceID
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.EntityTypeImpl
import org.jetbrains.annotations.Contract

interface EntityType : ProtocolObject, EntityTypes {
    /**
     * Returns the entity registry.
     *
     * @return the entity registry
     */
    @Contract(pure = true)
    fun registry(): EntityEntry
    override fun namespace(): NamespaceID {
        return registry().namespace()
    }

    override fun id(): Int {
        return registry().id()
    }

    fun width(): Double {
        return registry().width()
    }

    fun height(): Double {
        return registry().height()
    }

    companion object {
        fun values(): Collection<EntityType?> {
            return EntityTypeImpl.values()
        }

        fun fromNamespaceId(namespaceID: String): EntityType? {
            return EntityTypeImpl.getSafe(namespaceID)
        }

        fun fromNamespaceId(namespaceID: NamespaceID): EntityType? {
            return fromNamespaceId(namespaceID.asString())
        }

        fun fromId(id: Int): EntityType? {
            return EntityTypeImpl.getId(id)
        }
    }
}