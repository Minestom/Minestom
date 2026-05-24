package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/// This interface represents all scoreboards in Minecraft.
/// Construct a scoreboard using [Scoreboard#create(String)] and show it in a scoreboard slot
/// using [Scoreboard#addViewer(Player, Scoreboard.Position)].
public interface Scoreboard extends PacketGroupingAudience {

    /// Creates a new scoreboard that stores a map of strings to line entries.
    /// A line entry contains an integer score and optional overrides for display name and number format.
    /// @param objectiveName the objective name
    /// @return the scoreboard
    static Scoreboard create(String objectiveName) {
        return new ScoreboardImpl(objectiveName);
    }

    /// Creates a new scoreboard that stores a map of strings to line entries.
    /// A line entry contains an integer score and optional overrides for display name and number format.
    /// @param objectiveName the objective name
    /// @param displayName the scoreboard display name
    /// @return the scoreboard
    static Scoreboard create(String objectiveName, Component displayName) {
        return new ScoreboardImpl(objectiveName, displayName);
    }

    /// Gets the objective name of the scoreboard.
    ///
    /// @return the objective name
    String getObjectiveName();

    /// Gets the display name of the scoreboard, which is displayed on the sidebar.
    /// @return the component name
    Component getDisplayName();

    /// Sets the [`display name`][Scoreboard#getDisplayName()] of the scoreboard.
    /// @param displayName the new display name.
    void setDisplayName(Component displayName);

    /// Gets the [RenderType] of this scoreboard when in the player list.
    /// @return the render type
    RenderType getRenderType();

    /// Sets the [RenderType] of this scoreboard.
    /// @param renderType the new render type
    void setRenderType(RenderType renderType);

    /// Gets the default number format.
    /// @return number format, or null if none specified
    @Nullable NumberFormat getDefaultNumberFormat();

    /// Sets the default number format.
    /// @param numberFormat the new number format, or null to remove formatting
    void setDefaultNumberFormat(@Nullable NumberFormat numberFormat);

    /// Gets the score entry for the specified entry.
    /// @param entity the entry name
    /// @return the entry, or null if this scoreboard has no entry with that name
    @Nullable ScoreEntry getEntry(String entity);

    /// Gets all score entries stored in this scoreboard.
    /// @return an unmodifiable view of all score entries
    Map<String, ScoreEntry> getEntries();

    /// Updates or creates the score for an entity.
    /// Any name can be used for the entity, and will be displayed on a sidebar.
    /// The vanilla server uses players' usernames or entities' UUIDs.
    /// @param entity the entry name
    /// @param score the new score
    void updateScore(String entity, int score);

    /// Updates or creates the score for an entity.
    /// Any name can be used for the entity, and will be displayed on a sidebar.
    /// The vanilla server uses players' usernames or entities' UUIDs.
    /// @param entity the entry name
    /// @param displayName the name to display for the entity, or null to use the entry
    void updateDisplayName(String entity, @Nullable Component displayName);

    /// Updates or creates the score for an entity.
    /// Any name can be used for the entity, and will be displayed on a sidebar.
    /// The vanilla server uses players' usernames or entities' UUIDs.
    /// @param entity the entry name
    /// @param numberFormat the new number format, or null to reset to default
    void updateNumberFormat(String entity, @Nullable NumberFormat numberFormat);

    /// Updates or creates the score and associated properties for an entity.
    /// Any name can be used for the entity, and will be displayed on a sidebar.
    /// The vanilla server uses players' usernames or entities' UUIDs.
    /// @param entity the entry name
    /// @param entry the new entry
    void updateEntry(String entity, ScoreEntry entry);

    /// Updates or creates the score for an entity.
    /// Any name can be used for the entity, and will be displayed on a sidebar.
    /// The vanilla server uses players' usernames or entities' UUIDs.
    /// @param entity the entry name
    /// @param score the new score
    /// @param displayName the name to display for the entity, or null to use the entry
    /// @param numberFormat the new number format, or null to reset to default
    default void updateEntry(String entity, int score, @Nullable Component displayName, @Nullable NumberFormat numberFormat) {
        updateEntry(entity, new ScoreEntry(score, displayName, numberFormat));
    }

    /// Removes an entity's entry from the scoreboard.
    /// @param entity the entry name
    void removeEntry(String entity);

    /// Updates or creates the score of an [Entity].
    /// If a player is used, their username and display name are used.
    /// Otherwise, the entity's UUID and custom name are used.
    /// @param entity the entity
    /// @param score the new score
    /// @param numberFormat the new number format, or null to reset to default
    default void updateEntry(Entity entity, int score, @Nullable NumberFormat numberFormat) {
        if (entity instanceof Player player) {
            updateEntry(player.getUsername(), score, player.getDisplayName(), numberFormat);
        } else {
            updateEntry(entity.getUuid().toString(), score, entity.get(DataComponents.CUSTOM_NAME), numberFormat);
        }
    }

    /// Removes an [Entity] from the scoreboard.
    /// @param entity the entity
    default void removeEntry(Entity entity) {
        if (entity instanceof Player player) {
            removeEntry(player.getUsername());
        } else {
            removeEntry(entity.getUuid().toString());
        }
    }

    /// Adds a viewer to the scoreboard, with the scoreboard displayed in the specified slot.
    /// Note that conflicts with scoreboards in the same slot must be prevented by the user.
    /// Viewers must also be removed when they disconnect.
    /// @param player the viewer to add
    /// @return true if the player has been added, false otherwise
    boolean addViewer(Player player, Position position);

    /// Removes the scoreboard from the specified slot for the viewer.
    /// @param player the viewer to remove
    /// @return true if the player has been removed, false otherwise
    boolean removeViewer(Player player, Position position);

    /// Removes the scoreboard from all slots for the viewer.
    /// @param player the viewer to remove
    /// @return true if the player has been removed, false otherwise
    boolean removeViewer(Player player);

    /// Gets if a player is seeing this scoreboard.
    /// @param player the player to check
    /// @return true if {@code player} is a viewer, false otherwise
    boolean isViewer(Player player);

    /// Gets if a player is seeing this scoreboard in a specific position.
    /// @param player the player to check
    /// @return true if {@code player} is a viewer, false otherwise
    boolean isViewer(Player player, Position position);

    /// Gets an unmodifiable view of all viewers. Each viewer is mapped to a set of the positions they see the
    /// scoreboard in.
    @UnmodifiableView
    Map<Player, Set<Position>> getViewers();

    @Override
    default Collection<? extends Player> getPlayers() {
        return this.getViewers().keySet();
    }

    ///A position in which a client can render a scoreboard.
    enum Position {
        /// Scores are placed to the right of a player's name in the player list.
        /// Can use [RenderType]
        /// to display hearts instead of a number.
        PLAYER_LIST,
        /// Up to 15 scores are placed on the right side of the screen in descending order.
        /// The display name is shown.
        SIDEBAR,
        /// Scores are placed as a line below players' name tags, along with the display name.
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

        /// Returns the Position used for the sidebar when a player is on a team with a color.
        /// @param color the team color
        /// @return the corresponding Position
        public static Position forTeamColor(NamedTextColor color) {
            return values()[TEAM_COLOR_0.ordinal() + AdventurePacketConvertor.getNamedTextColorValue(color)];
        }

        public byte asByte() {
            return (byte) ordinal();
        }
    }

    /// The score render type when shown in the player list (integer or hearts)
    enum RenderType {
        INTEGER,
        HEARTS
    }
}