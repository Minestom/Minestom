package net.minestom.server.scoreboard;

import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.Player;

/// A slot in which the client can display an [Objective].
/// Objectives are bound to slots per player using [Player#setDisplayedObjective(DisplaySlot, Objective)].
///
/// The `TEAM_` slots behave like [#SIDEBAR], but are only shown to players
/// on a team with the matching color, taking priority over the regular sidebar slot.
/// They can be obtained from a color with [#forTeamColor(TeamColor)].
public enum DisplaySlot {
    /// Scores are placed to the right of player names in the player list.
    /// The objective's [RenderType] controls whether a number or hearts are displayed.
    PLAYER_LIST,
    /// Up to 15 entries are placed on the right side of the screen in descending score order,
    /// with the objective's display name shown as the title.
    SIDEBAR,
    /// Scores are placed below player name tags, next to the objective's display name.
    BELOW_NAME,
    /// The sidebar slot for teams colored [TeamColor#BLACK].
    TEAM_BLACK,
    /// The sidebar slot for teams colored [TeamColor#DARK_BLUE].
    TEAM_DARK_BLUE,
    /// The sidebar slot for teams colored [TeamColor#DARK_GREEN].
    TEAM_DARK_GREEN,
    /// The sidebar slot for teams colored [TeamColor#DARK_AQUA].
    TEAM_DARK_AQUA,
    /// The sidebar slot for teams colored [TeamColor#DARK_RED].
    TEAM_DARK_RED,
    /// The sidebar slot for teams colored [TeamColor#DARK_PURPLE].
    TEAM_DARK_PURPLE,
    /// The sidebar slot for teams colored [TeamColor#GOLD].
    TEAM_GOLD,
    /// The sidebar slot for teams colored [TeamColor#GRAY].
    TEAM_GRAY,
    /// The sidebar slot for teams colored [TeamColor#DARK_GRAY].
    TEAM_DARK_GRAY,
    /// The sidebar slot for teams colored [TeamColor#BLUE].
    TEAM_BLUE,
    /// The sidebar slot for teams colored [TeamColor#GREEN].
    TEAM_GREEN,
    /// The sidebar slot for teams colored [TeamColor#AQUA].
    TEAM_AQUA,
    /// The sidebar slot for teams colored [TeamColor#RED].
    TEAM_RED,
    /// The sidebar slot for teams colored [TeamColor#LIGHT_PURPLE].
    TEAM_LIGHT_PURPLE,
    /// The sidebar slot for teams colored [TeamColor#YELLOW].
    TEAM_YELLOW,
    /// The sidebar slot for teams colored [TeamColor#WHITE].
    TEAM_WHITE;

    /// Returns the team-specific sidebar slot for a team color.
    ///
    /// @param color the team color
    /// @return the corresponding slot
    public static DisplaySlot forTeamColor(TeamColor color) {
        return values()[TEAM_BLACK.ordinal() + color.ordinal()];
    }
}
