package net.minestom.server.statistic

import net.minestom.server.registry.ProtocolObject
import net.minestom.server.statistic.StatisticTypes
import net.minestom.server.statistic.StatisticType
import net.minestom.server.statistic.StatisticTypeImpl
import net.minestom.server.utils.NamespaceID
import net.minestom.server.statistic.StatisticCategory

interface StatisticType : ProtocolObject, StatisticTypes {
    companion object {
        fun values(): Collection<StatisticType?> {
            return StatisticTypeImpl.Companion.values()
        }

        fun fromNamespaceId(namespaceID: String): StatisticType? {
            return StatisticTypeImpl.Companion.getSafe(namespaceID)
        }

        fun fromNamespaceId(namespaceID: NamespaceID): StatisticType? {
            return fromNamespaceId(namespaceID.asString())
        }

        fun fromId(id: Int): StatisticType? {
            return StatisticTypeImpl.Companion.getId(id)
        }
    }
}