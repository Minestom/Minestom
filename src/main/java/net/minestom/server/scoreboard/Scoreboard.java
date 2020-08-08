package net.minestom.server.scoreboard;

import net.minestom.server.Viewable;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;

/**
 * This interface represents all scoreboard of Minecraft
 */
public interface Scoreboard extends Viewable {

    /**
     * Creates a creation objective packet
     *
     * @param value The value for the objective
     * @param type  The type for the objective
     * @return the creation objective packet
     */
    default ScoreboardObjectivePacket getCreationObjectivePacket(String value, ScoreboardObjectivePacket.Type type) {
        final ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.getObjectiveName();
        packet.mode = 0; // Create Scoreboard
        packet.objectiveValue = ColoredText.of(value);
        packet.type = type;

        return packet;
    }

    /**
     * Creates the destruction objective packet
     *
     * @return the destruction objective packet
     */
    default ScoreboardObjectivePacket getDestructionObjectivePacket() {
        final ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.getObjectiveName();
        packet.mode = 1; // Destroy Scoreboard

        return packet;
    }

    /**
     * Creates the {@link DisplayScoreboardPacket}
     *
     * @param position The position of the scoreboard
     * @return the created display scoreboard packet
     */
    default DisplayScoreboardPacket getDisplayScoreboardPacket(byte position) {
        final DisplayScoreboardPacket packet = new DisplayScoreboardPacket();
        packet.position = position;
        packet.scoreName = this.getObjectiveName();

        return packet;
    }

    /**
     * Updates the score of a {@link Player}
     *
     * @param player The player
     * @param score  The new score
     */
    default void updateScore(Player player, int score) {
        final UpdateScorePacket packet = new UpdateScorePacket();
        packet.entityName = player.getUsername();
        packet.action = 0; // Create/Update score
        packet.objectiveName = this.getObjectiveName();
        packet.value = score;

        sendPacketsToViewers(packet);
    }

    /**
     * Gets the objective name of the scoreboard
     *
     * @return the objective name
     */
    String getObjectiveName();

}
