package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jetbrains.annotations.Nullable;

/**
 * An entry in a scoreboard containing properties set per entity.
 * Entries are not tied to any particular scoreboard or entity name.
 *
 * @param score        the entry's score
 * @param displayName  the name displayed for this entry on a sidebar
 * @param numberFormat the number format for this entry
 */
public record ScoreEntry(int score, @Nullable Component displayName, @Nullable NumberFormat numberFormat) {
    // An entry with 0 score and no overrides.
    public static final ScoreEntry DEFAULT = new ScoreEntry(0, null, null);

    public UpdateScorePacket getUpdateScorePacket(String entity, String objective) {
        return new UpdateScorePacket(entity, objective, score, displayName, numberFormat);
    }

    /**
     * Returns a new entry with a specified score
     *
     * @param score the new score
     * @return a new entry
     */
    public ScoreEntry withScore(int score) {
        return new ScoreEntry(score, displayName, numberFormat);
    }

    /**
     * Returns a new entry with a specified display name
     *
     * @param displayName the new display name, or null to not override display name
     * @return a new entry
     */
    public ScoreEntry withDisplayName(@Nullable Component displayName) {
        return new ScoreEntry(score, displayName, numberFormat);
    }

    /**
     * Returns a new entry with a specified number format
     *
     * @param numberFormat the new number format, or null to not override
     * @return a new entry
     */
    public ScoreEntry withNumberFormat(@Nullable NumberFormat numberFormat) {
        return new ScoreEntry(score, displayName, numberFormat);
    }
}
