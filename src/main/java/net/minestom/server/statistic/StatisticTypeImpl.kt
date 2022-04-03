package net.minestom.server.statistic

import net.minestom.server.registry.ProtocolObject
import net.minestom.server.registry.Registry
import net.minestom.server.statistic.StatisticTypes
import net.minestom.server.statistic.StatisticType
import net.minestom.server.statistic.StatisticTypeImpl
import net.minestom.server.utils.NamespaceID
import net.minestom.server.statistic.StatisticCategory

internal class StatisticTypeImpl : StatisticType {
    override fun toString(): String {
        return name()
    }

    companion object {
        private val CONTAINER = Registry.createContainer<StatisticType?>(
            Registry.Resource.STATISTICS
        ) { namespace: String?, properties: Registry.Properties ->
            StatisticTypeImpl(
                NamespaceID.from(
                    namespace!!
                ), properties.getInt("id")
            )
        }

        operator fun get(namespace: String): StatisticType? {
            return CONTAINER[namespace]
        }

        fun getSafe(namespace: String): StatisticType? {
            return CONTAINER.getSafe(namespace)
        }

        fun getId(id: Int): StatisticType? {
            return CONTAINER.getId(id)
        }

        fun values(): Collection<StatisticType?> {
            return CONTAINER.values()
        }
    }
}