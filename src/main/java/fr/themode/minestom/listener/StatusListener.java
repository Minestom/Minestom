package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientStatusPacket;
import fr.themode.minestom.net.packet.server.play.StatisticsPacket;
import fr.themode.minestom.stat.PlayerStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatusListener {

    public static void listener(ClientStatusPacket packet, Player player) {
        switch (packet.action) {
            case PERFORM_RESPAWN:
                player.respawn();
                break;
            case REQUEST_STATS:
                List<StatisticsPacket.Statistic> statisticList = new ArrayList<>();
                StatisticsPacket statisticsPacket = new StatisticsPacket();

                Map<PlayerStatistic, Integer> playerStatisticValueMap = player.getStatisticValueMap();
                for (Map.Entry<PlayerStatistic, Integer> entry : playerStatisticValueMap.entrySet()) {
                    PlayerStatistic playerStatistic = entry.getKey();
                    int value = entry.getValue();

                    StatisticsPacket.Statistic statistic = new StatisticsPacket.Statistic();
                    statistic.category = playerStatistic.getCategory();
                    statistic.statisticId = playerStatistic.getStatisticId();
                    statistic.value = value;

                    statisticList.add(statistic);
                }

                statisticsPacket.statistics = statisticList.toArray(new StatisticsPacket.Statistic[statisticList.size()]);

                player.getPlayerConnection().sendPacket(statisticsPacket);
                break;
        }
    }

}
