package net.minestom.server.scoreboard;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

// TODO fix score and objective refresh
public class BelowNameScoreboard implements Viewable {

    private static final AtomicInteger counter = new AtomicInteger();

    // WARNING: you shouldn't create scoreboards/teams with the same prefixes as those
    private static final String SCOREBOARD_PREFIX = "bn-";
    private static final String TEAM_PREFIX = "bnt-";

    private Set<Player> viewers = new CopyOnWriteArraySet<>();

    private String objectiveName;

    private ScoreboardObjectivePacket scoreboardObjectivePacket;
    private DisplayScoreboardPacket displayScoreboardPacket;

    public BelowNameScoreboard() {
        this.objectiveName = SCOREBOARD_PREFIX + counter.incrementAndGet();

        scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 0;
        scoreboardObjectivePacket.objectiveValue = "test:" + objectiveName;
        scoreboardObjectivePacket.type = 0;

        displayScoreboardPacket = new DisplayScoreboardPacket();
        displayScoreboardPacket.position = 2; // Below name
        displayScoreboardPacket.scoreName = objectiveName;
    }

    public void updateScore(Player player, int score) {
        UpdateScorePacket updateScorePacket = new UpdateScorePacket();
        updateScorePacket.entityName = player.getUsername();
        updateScorePacket.action = 0; // Create/update
        updateScorePacket.objectiveName = objectiveName;
        updateScorePacket.value = score;

        sendPacketToViewers(updateScorePacket);
    }

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(scoreboardObjectivePacket);
    }

    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public void displayScoreboard(Player player) {
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(displayScoreboardPacket);
    }
}
