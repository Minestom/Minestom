package net.minestom.server.scoreboard;

import net.minestom.server.Viewable;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a scoreboard which rendered a tag below the name
 */
public class BelowNameScoreboard implements Viewable {

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();
    private final String objectiveName;

    private final ScoreboardObjectivePacket scoreboardObjectivePacket;
    private final ScoreboardObjectivePacket destructionObjectivePacket;
    private final DisplayScoreboardPacket displayScoreboardPacket;

    /**
     * Creates a new below name scoreboard
     *
     * @param name  The objective name of the scoreboard
     * @param value The value of the scoreboard
     */
    public BelowNameScoreboard(String name, String value) {
        this.objectiveName = name;

        this.scoreboardObjectivePacket = this.getCreationObjectivePacket(value);

        this.displayScoreboardPacket = new DisplayScoreboardPacket();
        this.displayScoreboardPacket.position = 2; // Below name
        this.displayScoreboardPacket.scoreName = this.objectiveName;

        this.destructionObjectivePacket = this.getDestructionObjectivePacket();
    }

    /**
     * Creates a creation objective packet for the tag below name
     *
     * @param value The value of the tag
     * @return the creation objective packet
     */
    private ScoreboardObjectivePacket getCreationObjectivePacket(String value) {
        ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.objectiveName;
        packet.mode = 0; // Create/Update
        packet.objectiveValue = ColoredText.of(value);
        packet.type = ScoreboardObjectivePacket.Type.INTEGER;

        return packet;
    }

    /**
     * Creates the destruction objective packet for the tag below name
     *
     * @return the destruction objective packet
     */
    private ScoreboardObjectivePacket getDestructionObjectivePacket() {
        ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.objectiveName;
        packet.mode = 1;
        packet.objectiveValue = ColoredText.of("");
        packet.type = ScoreboardObjectivePacket.Type.INTEGER;

        return packet;
    }

    /**
     * Updates the score of a {@link Player}
     *
     * @param player The player
     * @param score  The new score
     */
    public void updateScore(Player player, int score) {
        UpdateScorePacket packet = new UpdateScorePacket();
        packet.entityName = player.getUsername();
        packet.action = 0; //Create/Update
        packet.objectiveName = this.objectiveName;
        packet.value = score;

        // Sends to all viewers an update packet
        sendPacketToViewers(packet);
    }

    @Override
    public boolean addViewer(Player player) {
        boolean result = this.viewers.add(player);
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.scoreboardObjectivePacket);
            connection.sendPacket(this.displayScoreboardPacket);

            player.setBelowNameScoreboard(this);
        }

        return result;
    }

    @Override
    public boolean removeViewer(Player player) {
        boolean result = this.viewers.remove(player);
        PlayerConnection connection = player.getPlayerConnection();

        if (result) {
            connection.sendPacket(this.destructionObjectivePacket);
            player.setBelowNameScoreboard(null);
        }

        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }
}
