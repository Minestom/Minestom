package net.minestom.server.scoreboard;

/// The way an [Objective]'s scores are rendered next to player names in the player list.
public enum RenderType {
    /// Scores are displayed as yellow numbers.
    INTEGER,
    /// Scores are displayed as rows of hearts, treating the score as health points
    /// with two points per heart.
    HEARTS
}
