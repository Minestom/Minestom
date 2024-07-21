package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.statistic.StatisticCategory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record StatisticsPacket(@NotNull List<Statistic> statistics) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 16384;

    public StatisticsPacket {
        statistics = List.copyOf(statistics);
    }

    public StatisticsPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(Statistic::new, MAX_ENTRIES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(statistics);
    }

    public record Statistic(@NotNull StatisticCategory category,
                            int statisticId, int value) implements NetworkBuffer.Writer {
        public Statistic(@NotNull NetworkBuffer reader) {
            this(reader.readEnum(StatisticCategory.class),
                    reader.read(VAR_INT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, category.ordinal());
            writer.write(VAR_INT, statisticId);
            writer.write(VAR_INT, value);
        }
    }
}
