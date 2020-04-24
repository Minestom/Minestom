package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.stat.StatisticCategory;

public class StatisticsPacket implements ServerPacket {

    public Statistic[] statistics;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(statistics.length);
        for (Statistic statistic : statistics) {
            statistic.write(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STATISTICS;
    }

    public static class Statistic {

        public StatisticCategory category;
        public int statisticId;
        public int value;

        private void write(PacketWriter writer) {
            writer.writeVarInt(category.ordinal());
            writer.writeVarInt(statisticId);
            writer.writeVarInt(value);
        }
    }

}
