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
    default @NotNull ScoreboardObjectivePacket getCreationObjectivePacket(Component value, ScoreboardObjectivePacket.Type type) {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 0, value, type);
    }

    /**
     * Creates the destruction objective packet.
     *
     * @return the destruction objective packet
     */
    default @NotNull ScoreboardObjectivePacket getDestructionObjectivePacket() {
        return new ScoreboardObjectivePacket(getObjectiveName(), (byte) 1, null, null);
    }

    /**
     * Creates the {@link DisplayScoreboardPacket}.
     *
     * @param position The position of the scoreboard
     * @return the created display scoreboard packet
     */
    default @NotNull DisplayScoreboardPacket getDisplayScoreboardPacket(byte position) {
        return new DisplayScoreboardPacket(position, getObjectiveName());
    }

    /**
     * Updates the score of a {@link Player}.
     *
     * @param player The player
     * @param score  The new score
     */
    default void updateScore(Player player, int score) {
        sendPacketsToViewers(new UpdateScorePacket(player.getUsername(), (byte) 0, getObjectiveName(), score));
    }

    /**
     * Gets the objective name of the scoreboard.
     *
     * @return the objective name
     */
    @NotNull String getObjectiveName();

    @Override
    default @NotNull Collection<Player> getPlayers() {
        return this.getViewers();
    }
}
