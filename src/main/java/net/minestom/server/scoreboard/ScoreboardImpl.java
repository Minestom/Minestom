package net.minestom.server.scoreboard;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * A scoreboard that stores score and number format per entity.
 */
class ScoreboardImpl extends BaseScoreboard {

    private final Object2IntMap<String> scores = new Object2IntOpenHashMap<>();
    private final Map<String, NumberFormat> numberFormats = new HashMap<>();

    ScoreboardImpl(String objectiveName, Position position) {
        super(objectiveName, position);
    }

    @Override
    @ApiStatus.Internal
    public boolean addViewer(Player player) {
        boolean added = super.addViewer(player);
        if (!added) return false;

        scores.forEach((entity, score) -> player.sendPacket(
                new UpdateScorePacket(entity, objectiveName, score, getDisplayName(), numberFormats.get(entity))
        ));
        return true;
    }

    @Override
    public void updateScore(String entity, int score) {
        scores.put(entity, score);
        sendUpdate(entity, score, null, numberFormats.get(entity));
    }

    @Override
    public void removeScore(String entity) {
        scores.removeInt(entity);
        numberFormats.remove(entity);
        sendPacketToViewers(new ResetScorePacket(entity, objectiveName));
    }

}
