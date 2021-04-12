package net.minestom.server.stat;

import net.minestom.server.entity.Player;
import net.minestom.server.statistic.StatisticType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single statistic in the "statistics" game menu.
 * <p>
 * You can retrieve the statistics map with {@link Player#getStatisticValueMap()} and modify it with your own values.
 */
public class PlayerStatistic {

    private final StatisticCategory category;
    private final int statisticId;

    public PlayerStatistic(@NotNull StatisticCategory category, int statisticId) {
        this.category = category;
        this.statisticId = statisticId;
    }

    public PlayerStatistic(@NotNull StatisticType type) {
        this(StatisticCategory.CUSTOM, type.getNumericalId());
    }

    @NotNull
    public StatisticCategory getCategory() {
        return category;
    }

    public int getStatisticId() {
        return statisticId;
    }
}
