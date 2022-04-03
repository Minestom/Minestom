package net.minestom.server.statistic

import net.minestom.server.registry.ProtocolObject
import net.minestom.server.statistic.StatisticTypes
import net.minestom.server.statistic.StatisticType
import net.minestom.server.statistic.StatisticTypeImpl
import net.minestom.server.utils.NamespaceID
import net.minestom.server.statistic.StatisticCategory

/**
 * Represents a single statistic in the "statistics" game menu.
 *
 *
 * You can retrieve the statistics map with [Player.getStatisticValueMap] and modify it with your own values.
 */
class PlayerStatistic(val category: StatisticCategory, val statisticId: Int) {

    constructor(type: StatisticType) : this(StatisticCategory.CUSTOM, type.id()) {}
}