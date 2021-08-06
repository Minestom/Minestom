package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.statistic.StatisticCategory;
import net.minestom.server.utils.binary.*;
import net.minestom.server.utils.binary.Readable;
import org.jetbrains.annotations.NotNull;

public class StatisticsPacket implements ServerPacket {

    public Statistic[] statistics;

    public StatisticsPacket() {
        statistics = new Statistic[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(statistics.length);
        for (Statistic statistic : statistics) {
            statistic.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        int length = reader.readVarInt();
        statistics = new Statistic[length];
        for (int i = 0; i < length; i++) {
            statistics[i] = new Statistic();
            statistics[i].read(reader);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STATISTICS;
    }

    public static class Statistic implements Writeable, Readable {

        public StatisticCategory category;
        public int statisticId;
        public int value;

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(category.ordinal());
            writer.writeVarInt(statisticId);
            writer.writeVarInt(value);
        }

        @Override
        public void read(@NotNull BinaryBuffer reader) {
            category = StatisticCategory.values()[reader.readVarInt()];
            statisticId = reader.readVarInt();
            value = reader.readVarInt();
        }
    }

}
