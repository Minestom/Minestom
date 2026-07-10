package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/// A score tracked by an [Objective] for a single score holder.
///
/// @param score        the score
/// @param displayName  the name displayed in place of the score holder name on a sidebar,
///                     or null to display the score holder name
/// @param numberFormat the number format for this entry,
///                     or null to use the objective's default
public record ScoreEntry(int score, @Nullable Component displayName, @Nullable NumberFormat numberFormat) {
    /// An entry with a score of 0 and no display overrides.
    public static final ScoreEntry DEFAULT = new ScoreEntry(0, null, null);

    /// Returns a copy of this entry with the specified score.
    ///
    /// @param score the new score
    /// @return a new entry
    public ScoreEntry withScore(int score) {
        return new ScoreEntry(score, displayName, numberFormat);
    }

    /// Returns a copy of this entry with the specified display name.
    ///
    /// @param displayName the new display name, or null to display the score holder name
    /// @return a new entry
    public ScoreEntry withDisplayName(@Nullable Component displayName) {
        return new ScoreEntry(score, displayName, numberFormat);
    }

    /// Returns a copy of this entry with the specified number format.
    ///
    /// @param numberFormat the new number format, or null to use the objective's default
    /// @return a new entry
    public ScoreEntry withNumberFormat(@Nullable NumberFormat numberFormat) {
        return new ScoreEntry(score, displayName, numberFormat);
    }
}
