package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.statistic.StatisticCategory;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record StatisticsPacket(@NotNull List<Statistic> statistics) implements ServerPacket {
    public StatisticsPacket {
        statistics = List.copyOf(statistics);
    }

    public StatisticsPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(Statistic::new));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(statistics);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.STATISTICS;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
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
