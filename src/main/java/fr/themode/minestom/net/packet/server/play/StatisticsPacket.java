package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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

    public enum StatisticCategory {
        MINED,
        CRAFTED,
        USED,
        BROKEN,
        PICKED_UP,
        DROPPED,
        KILLED,
        KILLED_BY,
        CUSTOM
    }

    public static class Statistic {

        public StatisticCategory category;
        public int statisticIdentifier;
        public int value;

        private void write(PacketWriter writer) {
            writer.writeVarInt(category.ordinal());
            writer.writeVarInt(statisticIdentifier);
            writer.writeVarInt(value);
        }
    }

}
