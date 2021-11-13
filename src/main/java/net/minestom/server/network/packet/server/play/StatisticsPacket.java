package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.statistic.StatisticCategory;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record StatisticsPacket(List<Statistic> statistics) implements ServerPacket {
    public StatisticsPacket(BinaryReader reader) {
        this(reader.readVarIntList(Statistic::new));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(statistics.size());
        for (Statistic statistic : statistics) {
            statistic.write(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STATISTICS;
    }

    public record Statistic(StatisticCategory category, int statisticId, int value) implements Writeable {
        public Statistic(BinaryReader reader) {
            this(StatisticCategory.values()[reader.readVarInt()], reader.readVarInt(), reader.readVarInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(category.ordinal());
            writer.writeVarInt(statisticId);
            writer.writeVarInt(value);
        }
    }
}
