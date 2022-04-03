package net.minestom.server.statistic

import net.minestom.server.registry.ProtocolObject
import net.minestom.server.statistic.StatisticTypes
import net.minestom.server.statistic.StatisticType
import net.minestom.server.statistic.StatisticTypeImpl
import net.minestom.server.utils.NamespaceID
import net.minestom.server.statistic.StatisticCategory

enum class StatisticCategory {
    MINED, CRAFTED, USED, BROKEN, PICKED_UP, DROPPED, KILLED, KILLED_BY, CUSTOM
}