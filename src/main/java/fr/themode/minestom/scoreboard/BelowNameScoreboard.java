package fr.themode.minestom.scoreboard;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.DisplayScoreboardPacket;
import fr.themode.minestom.net.packet.server.play.ScoreboardObjectivePacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

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
        System.out.println("DEBUG: " + objectiveName);
        scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 0;
        scoreboardObjectivePacket.objectiveValue = "test:" + objectiveName;
        scoreboardObjectivePacket.type = 0;

        displayScoreboardPacket = new DisplayScoreboardPacket();
        displayScoreboardPacket.position = 2;
        displayScoreboardPacket.scoreName = objectiveName;
    }

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(scoreboardObjectivePacket);
        // TODO score
        playerConnection.sendPacket(displayScoreboardPacket);
    }

    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }
}
