package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * This interface represents all scoreboards in Minecraft.
 */
public interface Scoreboard extends Viewable, PacketGroupingAudience {

    static Scoreboard create(String objectiveName, Position position) {
        return new ScoreboardImpl(objectiveName, position);
    }

    static Scoreboard createTransient(String objectiveName, Position position) {
        return new BaseScoreboard(objectiveName, position);
    }

    /**
     * Gets the objective name of the scoreboard.
     *
     * @return the objective name
     */
    String getObjectiveName();

    /**
     * Gets the display name of the scoreboard, which is displayed on the sidebar.
     * @return the component name
     */
    @Nullable Component getDisplayName();

    /**
     * Sets the {@link Scoreboard#getDisplayName() display name} of the scoreboard.
     * @param displayName the new display name.
     */
    void setDisplayName(Component displayName);

    /**
     * Gets the position where this scoreboard is shown.
     * @return the position
     */
    Position getPosition();

    /**
     * Sets the position where this scoreboard is shown.
     * Viewers who are already viewing another scoreboard in that position will be removed from its viewers.
     * @param position the new position
     */
    void setPosition(Position position);

    /**
     * Gets the display format of this scoreboard when in the player list.
     * @return the display type
     */
    ScoreboardObjectivePacket.Type getDisplayType();

    /**
     * Sets the {@link Scoreboard#getDisplayType() display type} of this scoreboard.
     * @param displayType the new display type
     */
    void setDisplayType(ScoreboardObjectivePacket.Type displayType);

    /**
     * Gets the default number format.
     * @return number format, or null if none specified
     */
    @Nullable NumberFormat getDefaultNumberFormat();

    /**
     * Sets the default number format.
     * @param numberFormat the new number format, or null to remove formatting
     */
    void setDefaultNumberFormat(@Nullable NumberFormat numberFormat);

    /**
     * Updates the score for an entity.
     * Any name can be used for the entity, and will be displayed on a sidebar.
     * The vanilla client uses players' usernames or entities' UUIDs.
     * @param entity the entity name
     * @param score the new score
     */
    void updateScore(String entity, int score);

    /**
     * Updates the score for an entity.
     * Any name can be used for the entity, and will be displayed on a sidebar.
     * The vanilla client uses players' usernames or entities' UUIDs.
     * @param entity the entity name
     * @param score the new score
     * @param numberFormat the new number format, or null to reset to default
     */
    void updateScore(String entity, int score, @Nullable NumberFormat numberFormat);

    /**
     * Updates the number format for an entity.
     * Any name can be used for the entity, and will be displayed on a sidebar.
     * The vanilla client uses players' usernames or entities' UUIDs.
     * @param entity the entity name
     * @param numberFormat the new number format, or null to reset to default
     */
    void updateNumberFormat(String entity, NumberFormat numberFormat);

    /**
     * Removes an entity from the scoreboard.
     * Any name can be used for the entity, and will be displayed on a sidebar.
     * The vanilla client uses players' usernames or entities' UUIDs.
     * @param entity the entity name
     */
    void removeScore(String entity);

    /**
     * Updates the score of a {@link Player}.
     * @param player the player
     * @param score the new score
     */
    default void updateScore(Player player, int score) {
        updateScore(player.getUsername(), score);
    }

    /**
     * Updates the score of a {@link Player}.
     * @param player the player
     * @param score the new score
     * @param numberFormat the new number format, or null to reset to default
     */
    default void updateScore(Player player, int score, NumberFormat numberFormat) {
        updateScore(player.getUsername(), score, numberFormat);
    }

    /**
     * Updates the number format for a {@link Player}.
     * @param player the player
     * @param numberFormat the new number format, or null to reset to default
     */
    default void updateNumberFormat(Player player, NumberFormat numberFormat) {
        updateNumberFormat(player.getUsername(), numberFormat);
    }

    /**
     * Removes a {@link Player} from the scoreboard.
     * @param player the player
     */
    default void removeScore(Player player) {
        removeScore(player.getUsername());
    }

    @Override
    default Collection<Player> getPlayers() {
        return this.getViewers();
    }

    /**
     * A position in which a client can render a scoreboard.
     */
    enum Position {
        /**
         * Scores are placed to the right of a player's name in the player list.
         * Can use {@link net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket.Type}
         * to display hearts instead of a number.
         */
        PLAYER_LIST,
        /**
         * Up to 15 scores are placed on the right side of the screen in descending order.
         * The display name is shown.
         */
        SIDEBAR,
        /**
         * Scores are placed as a line below players' name tags, along with the display name.
         */
        BELOW_NAME,
        TEAM_COLOR_0,
        TEAM_COLOR_1,
        TEAM_COLOR_2,
        TEAM_COLOR_3,
        TEAM_COLOR_4,
        TEAM_COLOR_5,
        TEAM_COLOR_6,
        TEAM_COLOR_7,
        TEAM_COLOR_8,
        TEAM_COLOR_9,
        TEAM_COLOR_10,
        TEAM_COLOR_11,
        TEAM_COLOR_12,
        TEAM_COLOR_13,
        TEAM_COLOR_14,
        TEAM_COLOR_15;

        public static Position forTeamColor(NamedTextColor color) {
            return values()[TEAM_COLOR_0.ordinal() + AdventurePacketConvertor.getNamedTextColorValue(color)];
        }

        public byte asByte() {
            return (byte) ordinal();
        }
    }
}
