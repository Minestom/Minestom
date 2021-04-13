package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * This interface represents all scoreboard of Minecraft.
 */
public interface Scoreboard extends Viewable, PacketGroupingAudience {

    /**
     * Creates a creation objective packet.
     *
     * @param value The value for the objective
     * @param type  The type for the objective
     * @return the creation objective packet
     * @deprecated Use {@link #getCreationObjectivePacket(Component, ScoreboardObjectivePacket.Type)}
     */
    @Deprecated
    @NotNull
    default ScoreboardObjectivePacket getCreationObjectivePacket(String value, ScoreboardObjectivePacket.Type type) {
        return this.getCreationObjectivePacket(Component.text(value), type);
    }

    /**
     * Creates a creation objective packet.
     *
     * @param value The value for the objective
     * @param type  The type for the objective
     * @return the creation objective packet
     */
    @NotNull
    default ScoreboardObjectivePacket getCreationObjectivePacket(Component value, ScoreboardObjectivePacket.Type type) {
        final ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.getObjectiveName();
        packet.mode = 0; // Create Scoreboard
        packet.objectiveValue = value;
        packet.type = type;

        return packet;
    }

    /**
     * Creates the destruction objective packet.
     *
     * @return the destruction objective packet
     */
    @NotNull
    default ScoreboardObjectivePacket getDestructionObjectivePacket() {
        final ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
        packet.objectiveName = this.getObjectiveName();
        packet.mode = 1; // Destroy Scoreboard

        return packet;
    }

    /**
     * Creates the {@link DisplayScoreboardPacket}.
     *
     * @param position The position of the scoreboard
     * @return the created display scoreboard packet
     */
    @NotNull
    default DisplayScoreboardPacket getDisplayScoreboardPacket(byte position) {
        final DisplayScoreboardPacket packet = new DisplayScoreboardPacket();
        packet.position = position;
        packet.scoreName = this.getObjectiveName();

        return packet;
    }

    /**
     * Updates the score of a {@link Player}.
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
     * Gets the objective name of the scoreboard.
     *
     * @return the objective name
     */
    @NotNull
    String getObjectiveName();

    @Override
    @NotNull default Collection<Player> getPlayers() {
        return this.getViewers();
    }
}
