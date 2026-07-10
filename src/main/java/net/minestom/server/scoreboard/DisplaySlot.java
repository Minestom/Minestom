package net.minestom.server.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.entity.Player;

/// A slot in which the client can display an [Objective].
/// Objectives are bound to slots per player using [Player#setDisplayedObjective(DisplaySlot, Objective)].
///
/// The `TEAM_` slots behave like [#SIDEBAR], but are only shown to players
/// on a team with the matching color, taking priority over the regular sidebar slot.
/// They can be obtained from a color with [#forTeamColor(NamedTextColor)].
public enum DisplaySlot {
    /// Scores are placed to the right of player names in the player list.
    /// The objective's [RenderType] controls whether a number or hearts are displayed.
    PLAYER_LIST,
    /// Up to 15 entries are placed on the right side of the screen in descending score order,
    /// with the objective's display name shown as the title.
    SIDEBAR,
    /// Scores are placed below player name tags, next to the objective's display name.
    BELOW_NAME,
    /// The sidebar slot for teams colored [NamedTextColor#BLACK].
    TEAM_BLACK,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_BLUE].
    TEAM_DARK_BLUE,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_GREEN].
    TEAM_DARK_GREEN,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_AQUA].
    TEAM_DARK_AQUA,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_RED].
    TEAM_DARK_RED,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_PURPLE].
    TEAM_DARK_PURPLE,
    /// The sidebar slot for teams colored [NamedTextColor#GOLD].
    TEAM_GOLD,
    /// The sidebar slot for teams colored [NamedTextColor#GRAY].
    TEAM_GRAY,
    /// The sidebar slot for teams colored [NamedTextColor#DARK_GRAY].
    TEAM_DARK_GRAY,
    /// The sidebar slot for teams colored [NamedTextColor#BLUE].
    TEAM_BLUE,
    /// The sidebar slot for teams colored [NamedTextColor#GREEN].
    TEAM_GREEN,
    /// The sidebar slot for teams colored [NamedTextColor#AQUA].
    TEAM_AQUA,
    /// The sidebar slot for teams colored [NamedTextColor#RED].
    TEAM_RED,
    /// The sidebar slot for teams colored [NamedTextColor#LIGHT_PURPLE].
    TEAM_LIGHT_PURPLE,
    /// The sidebar slot for teams colored [NamedTextColor#YELLOW].
    TEAM_YELLOW,
    /// The sidebar slot for teams colored [NamedTextColor#WHITE].
    TEAM_WHITE;

    /// Returns the team-specific sidebar slot for a team color.
    ///
    /// @param color the team color
    /// @return the corresponding slot
    public static DisplaySlot forTeamColor(NamedTextColor color) {
        return values()[TEAM_BLACK.ordinal() + AdventurePacketConvertor.getNamedTextColorValue(color)];
    }
}
