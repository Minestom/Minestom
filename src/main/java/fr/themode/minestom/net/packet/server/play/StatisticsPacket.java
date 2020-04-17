package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;
import fr.themode.minestom.stat.StatisticCategory;

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
