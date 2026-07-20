package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/// An objective tracks a [ScoreEntry] per score holder name and replicates them to its viewers.
/// Score holder names are arbitrary strings; the vanilla server uses player usernames and entity UUIDs,
/// which [#scoreHolder(Entity)] mirrors.
///
/// Objectives are displayed by binding them to a [DisplaySlot] per player with
/// [Player#setDisplayedObjective(DisplaySlot, Objective)]. Viewers are tracked automatically from
/// those bindings and are cleaned up when a player disconnects.
///
/// For the common case of a sidebar with lines of text, prefer [Sidebar].
public sealed interface Objective extends PacketGroupingAudience permits ObjectiveImpl {
    /// Creates a new objective with its name as the display name.
    ///
    /// @param name the objective name; must be unique per client, as displaying two
    ///             objectives with the same name to one player is not supported
    /// @return a new objective
    static Objective create(String name) {
        return new ObjectiveImpl(name, Component.text(name));
    }

    /// Creates a new objective.
    ///
    /// @param name        the objective name; must be unique per client, as displaying two
    ///                     objectives with the same name to one player is not supported
    /// @param displayName the display name
    /// @return a new objective
    static Objective create(String name, Component displayName) {
        return new ObjectiveImpl(name, displayName);
    }

    /// Returns the score holder name the vanilla server would use for an entity:
    /// the username for players, the UUID otherwise.
    ///
    /// @param entity the entity
    /// @return the score holder name
    static String scoreHolder(Entity entity) {
        return entity instanceof Player player ? player.getUsername() : entity.getUuid().toString();
    }

    /// Gets the objective name, which identifies this objective on the client.
    ///
    /// @return the objective name
    String getName();

    /// Gets the display name, shown as the title in [DisplaySlot#SIDEBAR]
    /// and next to scores in [DisplaySlot#BELOW_NAME].
    ///
    /// @return the display name
    Component getDisplayName();

    /// Sets the [display name][#getDisplayName()].
    ///
    /// @param displayName the new display name
    void setDisplayName(Component displayName);

    /// Gets the render type used in [DisplaySlot#PLAYER_LIST].
    ///
    /// @return the render type
    RenderType getRenderType();

    /// Sets the [render type][#getRenderType()].
    ///
    /// @param renderType the new render type
    void setRenderType(RenderType renderType);

    /// Gets the number format applied to entries without their own format.
    ///
    /// @return the default number format, or null if none is set
    @Nullable NumberFormat getDefaultNumberFormat();

    /// Sets the [default number format][#getDefaultNumberFormat()].
    ///
    /// @param numberFormat the new number format, or null to remove it
    void setDefaultNumberFormat(@Nullable NumberFormat numberFormat);

    /// Gets the entry of a score holder.
    ///
    /// @param holder the score holder name
    /// @return the entry, or null if this objective has no entry for the holder
    @Nullable ScoreEntry getEntry(String holder);

    /// Gets all entries of this objective, keyed by score holder name.
    ///
    /// @return an unmodifiable view of all entries
    @UnmodifiableView Map<String, ScoreEntry> getEntries();

    /// Updates or creates the score of a score holder, keeping its other properties.
    ///
    /// @param holder the score holder name
    /// @param score  the new score
    void updateScore(String holder, int score);

    /// Updates or creates the display name of a score holder's entry, keeping its other properties.
    ///
    /// @param holder      the score holder name
    /// @param displayName the new display name, or null to display the score holder name
    void updateDisplayName(String holder, @Nullable Component displayName);

    /// Updates or creates the number format of a score holder's entry, keeping its other properties.
    ///
    /// @param holder       the score holder name
    /// @param numberFormat the new number format, or null to use the objective's default
    void updateNumberFormat(String holder, @Nullable NumberFormat numberFormat);

    /// Updates or creates the entry of a score holder.
    ///
    /// @param holder the score holder name
    /// @param entry  the new entry
    void updateEntry(String holder, ScoreEntry entry);

    /// Removes the entry of a score holder.
    ///
    /// @param holder the score holder name
    void removeEntry(String holder);

    /// Updates or creates the score of an entity, keeping its other properties.
    ///
    /// @param entity the entity
    /// @param score  the new score
    default void updateScore(Entity entity, int score) {
        updateScore(scoreHolder(entity), score);
    }

    /// Gets the entry of an entity.
    ///
    /// @param entity the entity
    /// @return the entry, or null if this objective has no entry for the entity
    default @Nullable ScoreEntry getEntry(Entity entity) {
        return getEntry(scoreHolder(entity));
    }

    /// Removes the entry of an entity.
    ///
    /// @param entity the entity
    default void removeEntry(Entity entity) {
        removeEntry(scoreHolder(entity));
    }

    /// Gets if a player currently displays this objective in any slot.
    ///
    /// @param player the player
    /// @return true if the player is a viewer
    boolean isViewer(Player player);

    /// Gets all players currently displaying this objective in at least one slot.
    ///
    /// @return an unmodifiable view of all viewers
    @UnmodifiableView Set<Player> getViewers();

    /// Called when a player starts displaying this objective in at least one slot.
    /// Sends the objective and its entries, and registers the player as a viewer.
    ///
    /// @param player the new viewer
    @ApiStatus.Internal
    void updateNewViewer(Player player);

    /// Called when a player stops displaying this objective in all slots.
    /// Removes the objective from the client, and unregisters the player as a viewer.
    ///
    /// @param player the old viewer
    @ApiStatus.Internal
    void updateOldViewer(Player player);

    @Override
    default Collection<? extends Player> getPlayers() {
        return getViewers();
    }
}
