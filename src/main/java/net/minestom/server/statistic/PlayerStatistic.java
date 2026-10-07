package net.minestom.server.statistic;

import net.minestom.server.entity.Player;

import java.util.Objects;

/**
 * Represents a single statistic in the "statistics" game menu.
 * <p>
 * You can retrieve the statistics map with {@link Player#getStatisticValueMap()} and modify it with your own values.
 */
public class PlayerStatistic {
    private final StatisticCategory category;
    private final int statisticId;

    public PlayerStatistic(StatisticCategory category, int statisticId) {
        this.category = category;
        this.statisticId = statisticId;
    }

    public PlayerStatistic(StatisticType type) {
        this(StatisticCategory.CUSTOM, type.id());
    }

    public StatisticCategory getCategory() {
        return category;
    }

    public int getStatisticId() {
        return statisticId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerStatistic that)) return false;
        return statisticId == that.statisticId && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, statisticId);
    }
}
