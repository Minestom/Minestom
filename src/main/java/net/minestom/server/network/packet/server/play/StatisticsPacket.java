package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.statistic.StatisticCategory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record StatisticsPacket(@NotNull List<Statistic> statistics) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 16384;

    public static final NetworkBuffer.Type<StatisticsPacket> SERIALIZER = NetworkBufferTemplate.template(
            Statistic.SERIALIZER.list(MAX_ENTRIES), StatisticsPacket::statistics,
            StatisticsPacket::new);

    public StatisticsPacket {
        statistics = List.copyOf(statistics);
    }

    public record Statistic(@NotNull StatisticCategory category, int statisticId, int value) {
        public static final NetworkBuffer.Type<Statistic> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.Enum(StatisticCategory.class), Statistic::category,
                VAR_INT, Statistic::statisticId,
                VAR_INT, Statistic::value,
                Statistic::new);
    }
}
